package com.example.personaldashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.personaldashboard.data.DashboardRepository
import com.example.personaldashboard.model.DashboardSpace
import com.example.personaldashboard.model.DashboardState
import com.example.personaldashboard.model.DashboardWidget
import com.example.personaldashboard.model.ExpenseCategory
import com.example.personaldashboard.model.ExpenseTransaction
import com.example.personaldashboard.model.MoneyFund
import com.example.personaldashboard.model.ThemePreset
import com.example.personaldashboard.model.TodoItem
import com.example.personaldashboard.model.WidgetKind
import com.example.personaldashboard.model.defaultCategoryBudgets
import com.example.personaldashboard.model.defaultFunds

class DashboardController(private val repository: DashboardRepository) {
    var state by mutableStateOf(repository.load())
        private set

    fun selectSpace(id: String) = update { it.copy(activeSpaceId = id) }

    fun setTheme(theme: ThemePreset) = updateActiveSpace { it.copy(theme = theme) }

    fun addWidget(kind: WidgetKind) = updateActiveSpace { space ->
        space.copy(widgets = space.widgets + DashboardWidget(kind = kind))
    }

    fun removeWidget(id: String) = updateActiveSpace { space ->
        space.copy(widgets = space.widgets.filterNot { it.id == id })
    }

    fun moveWidget(id: String, delta: Int) = updateActiveSpace { space ->
        val widgets = space.widgets.toMutableList()
        val from = widgets.indexOfFirst { it.id == id }
        val to = (from + delta).coerceIn(0, widgets.lastIndex)
        if (from >= 0 && from != to) {
            val item = widgets.removeAt(from)
            widgets.add(to, item)
        }
        space.copy(widgets = widgets)
    }

    fun toggleTodo(id: String) = update { current ->
        current.copy(todos = current.todos.map { if (it.id == id) it.copy(done = !it.done) else it })
    }

    fun addTodo(label: String) {
        if (label.isBlank()) return
        update { it.copy(todos = it.todos + TodoItem(label = label.trim())) }
    }

    fun updateNote(note: String) = update { it.copy(note = note) }

    fun advanceHabit(name: String) = update { current ->
        val next = ((current.habitProgress[name] ?: 0) + 1) % 5
        current.copy(habitProgress = current.habitProgress + (name to next))
    }

    fun setReduceMotion(enabled: Boolean) = update { it.copy(reduceMotion = enabled) }

    fun completeOnboarding(name: String, income: Long, budget: Long) {
        if (budget <= 0) return
        update {
            it.copy(
                profileName = name.trim(),
                hasCompletedOnboarding = true,
                monthlyIncome = income.coerceAtLeast(0),
                monthlyBudget = budget,
                categoryBudgets = defaultCategoryBudgets(budget),
                funds = defaultFunds(income.coerceAtLeast(budget)),
            )
        }
    }

    fun updateProfile(name: String) = update { it.copy(profileName = name.trim()) }

    fun addExpense(
        title: String,
        amount: Long,
        category: ExpenseCategory,
        createdAt: Long = System.currentTimeMillis(),
        fundId: String? = null,
    ) {
        if (title.isBlank() || amount <= 0) return
        update { current ->
            val selectedFundId = fundId?.takeIf { id -> current.funds.any { it.id == id } } ?: current.primaryFund.id
            current.copy(
                transactions = (current.transactions + ExpenseTransaction(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    createdAt = createdAt,
                    fundId = selectedFundId,
                )).sortedByDescending { it.createdAt },
                funds = current.funds.map { fund ->
                    if (fund.id == selectedFundId) fund.copy(balance = fund.balance - amount) else fund
                },
            )
        }
    }

    fun updateExpense(
        id: String,
        title: String,
        amount: Long,
        category: ExpenseCategory,
        createdAt: Long,
        fundId: String? = null,
    ) {
        if (title.isBlank() || amount <= 0) return
        update { current ->
            val previous = current.transactions.firstOrNull { it.id == id } ?: return@update current
            val selectedFundId = fundId?.takeIf { selected -> current.funds.any { it.id == selected } } ?: previous.fundId
            current.copy(
                transactions = current.transactions.map { transaction ->
                if (transaction.id == id) transaction.copy(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    createdAt = createdAt,
                    fundId = selectedFundId,
                ) else transaction
                }.sortedByDescending { it.createdAt },
                funds = current.funds.map { fund ->
                    var balance = fund.balance
                    if (fund.id == previous.fundId) balance += previous.amount
                    if (fund.id == selectedFundId) balance -= amount
                    fund.copy(balance = balance)
                },
            )
        }
    }

    fun removeExpense(id: String) = update { current ->
        val removed = current.transactions.firstOrNull { it.id == id }
        current.copy(
            transactions = current.transactions.filterNot { it.id == id },
            funds = if (removed == null) current.funds else current.funds.map { fund ->
                if (fund.id == removed.fundId) fund.copy(balance = fund.balance + removed.amount) else fund
            },
        )
    }

    fun addFund(name: String, emoji: String, balance: Long, makePrimary: Boolean) {
        if (name.isBlank() || balance < 0) return
        update { current ->
            val fund = MoneyFund(
                name = name.trim(),
                emoji = emoji.ifBlank { "👛" },
                balance = balance,
                isPrimary = makePrimary || current.funds.isEmpty(),
            )
            current.copy(
                funds = current.funds.map { if (fund.isPrimary) it.copy(isPrimary = false) else it } + fund,
            )
        }
    }

    fun updateFund(id: String, name: String, emoji: String, balance: Long, makePrimary: Boolean) {
        if (name.isBlank()) return
        update { current ->
            current.copy(funds = current.funds.map { fund ->
                when {
                    fund.id == id -> fund.copy(
                        name = name.trim(),
                        emoji = emoji.ifBlank { "👛" },
                        balance = balance,
                        isPrimary = makePrimary || fund.isPrimary,
                    )
                    makePrimary -> fund.copy(isPrimary = false)
                    else -> fund
                }
            })
        }
    }

    fun updateFinancialPlan(income: Long, budget: Long) {
        if (budget > 0) update { current ->
            current.copy(monthlyIncome = income.coerceAtLeast(0), monthlyBudget = budget)
        }
    }

    fun updateCategoryBudget(category: ExpenseCategory, amount: Long) = update { current ->
        current.copy(categoryBudgets = current.categoryBudgets + (category.name to amount.coerceAtLeast(0)))
    }

    fun exportData(): String = repository.exportJson()

    fun importData(raw: String): Boolean = runCatching {
        state = repository.importJson(raw)
        true
    }.getOrDefault(false)

    fun resetAllData() {
        state = repository.reset()
    }

    fun addSpace() {
        val count = state.spaces.size + 1
        val space = DashboardSpace(
            name = "My Space $count",
            emoji = "🌟",
            theme = ThemePreset.MINIMAL,
            widgets = listOf(
                DashboardWidget(kind = WidgetKind.CLOCK),
                DashboardWidget(kind = WidgetKind.TODO),
            ),
        )
        update { it.copy(spaces = it.spaces + space, activeSpaceId = space.id) }
    }

    private fun updateActiveSpace(transform: (DashboardSpace) -> DashboardSpace) = update { current ->
        current.copy(spaces = current.spaces.map { space ->
            if (space.id == current.activeSpaceId) transform(space) else space
        })
    }

    private fun update(transform: (DashboardState) -> DashboardState) {
        state = transform(state)
        repository.save(state)
    }
}
