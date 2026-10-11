package com.example.personaldashboard.data

import android.content.Context
import androidx.core.content.edit
import com.example.personaldashboard.model.DashboardSpace
import com.example.personaldashboard.model.DashboardState
import com.example.personaldashboard.model.DashboardWidget
import com.example.personaldashboard.model.ExpenseCategory
import com.example.personaldashboard.model.ExpenseTransaction
import com.example.personaldashboard.model.MoneyFund
import com.example.personaldashboard.model.ThemePreset
import com.example.personaldashboard.model.TodoItem
import com.example.personaldashboard.model.WidgetKind
import com.example.personaldashboard.model.defaultDashboardState
import com.example.personaldashboard.model.defaultCategoryBudgets
import com.example.personaldashboard.model.defaultFunds
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class DashboardRepository(context: Context) {
    private val preferences = context.getSharedPreferences("mochi_dashboard", Context.MODE_PRIVATE)

    fun load(): DashboardState {
        val raw = preferences.getString(KEY_STATE, null) ?: return defaultDashboardState()
        return runCatching { migrate(decode(JSONObject(raw))) }.getOrElse { defaultDashboardState() }
    }

    private fun migrate(state: DashboardState): DashboardState {
        val firstSpace = state.spaces.firstOrNull() ?: return state
        val widgets = firstSpace.widgets.toMutableList()
        var changed = false
        if (state.spaces.none { space -> space.widgets.any { it.kind == WidgetKind.WEATHER } }) {
            val insertAt = (widgets.indexOfFirst { it.kind == WidgetKind.CLOCK } + 1).coerceAtLeast(0)
            widgets.add(insertAt.coerceAtMost(widgets.size), DashboardWidget(kind = WidgetKind.WEATHER))
            changed = true
        }
        if (state.spaces.none { space -> space.widgets.any { it.kind == WidgetKind.OVERVIEW } }) {
            val insertAt = (widgets.indexOfFirst { it.kind == WidgetKind.WEATHER } + 1).coerceAtLeast(0)
            widgets.add(insertAt.coerceAtMost(widgets.size), DashboardWidget(kind = WidgetKind.OVERVIEW))
            changed = true
        }
        if (!changed) return state
        return state.copy(
            spaces = state.spaces.map { if (it.id == firstSpace.id) it.copy(widgets = widgets) else it },
        ).also(::save)
    }

    fun save(state: DashboardState) {
        preferences.edit { putString(KEY_STATE, encode(state).toString()) }
    }

    fun exportJson(): String = encode(load()).toString(2)

    fun importJson(raw: String): DashboardState {
        val imported = decode(JSONObject(raw)).copy(hasCompletedOnboarding = true)
        save(imported)
        return imported
    }

    fun reset(): DashboardState = defaultDashboardState().also { fresh ->
        preferences.edit { clear() }
        save(fresh)
    }

    private fun encode(state: DashboardState): JSONObject = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("activeSpaceId", state.activeSpaceId)
        put("profileName", state.profileName)
        put("hasCompletedOnboarding", state.hasCompletedOnboarding)
        put("note", state.note)
        put("reduceMotion", state.reduceMotion)
        put("monthlyIncome", state.monthlyIncome)
        put("monthlyBudget", state.monthlyBudget)
        put("categoryBudgets", JSONObject().apply {
            state.categoryBudgets.forEach { (category, amount) -> put(category, amount) }
        })
        put("funds", JSONArray().apply {
            state.funds.forEach { fund ->
                put(JSONObject().apply {
                    put("id", fund.id)
                    put("name", fund.name)
                    put("emoji", fund.emoji)
                    put("balance", fund.balance)
                    put("isPrimary", fund.isPrimary)
                })
            }
        })
        put("transactions", JSONArray().apply {
            state.transactions.forEach { transaction ->
                put(JSONObject().apply {
                    put("id", transaction.id)
                    put("title", transaction.title)
                    put("amount", transaction.amount)
                    put("category", transaction.category.name)
                    put("createdAt", transaction.createdAt)
                    put("fundId", transaction.fundId)
                })
            }
        })
        put("todos", JSONArray().apply {
            state.todos.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("label", item.label)
                    put("done", item.done)
                })
            }
        })
        put("habits", JSONObject().apply {
            state.habitProgress.forEach { (name, progress) -> put(name, progress) }
        })
        put("spaces", JSONArray().apply {
            state.spaces.forEach { space ->
                put(JSONObject().apply {
                    put("id", space.id)
                    put("name", space.name)
                    put("emoji", space.emoji)
                    put("theme", space.theme.name)
                    put("widgets", JSONArray().apply {
                        space.widgets.forEach { widget ->
                            put(JSONObject().apply {
                                put("id", widget.id)
                                put("kind", widget.kind.name)
                            })
                        }
                    })
                })
            }
        })
    }

    private fun decode(json: JSONObject): DashboardState {
        val schemaVersion = json.optInt("schemaVersion", 0)
        val spaces = json.getJSONArray("spaces").mapObjects { spaceJson ->
            DashboardSpace(
                id = spaceJson.getString("id"),
                name = spaceJson.getString("name"),
                emoji = spaceJson.optString("emoji", "✨"),
                theme = enumValueOf<ThemePreset>(spaceJson.getString("theme")),
                widgets = spaceJson.getJSONArray("widgets").mapObjects { widgetJson ->
                    DashboardWidget(
                        id = widgetJson.getString("id"),
                        kind = enumValueOf<WidgetKind>(widgetJson.getString("kind")),
                    )
                },
            )
        }
        val todos = (json.optJSONArray("todos") ?: JSONArray()).mapObjects { item ->
            TodoItem(
                id = item.getString("id"),
                label = item.getString("label"),
                done = item.getBoolean("done"),
            )
        }
        val habitsJson = json.optJSONObject("habits") ?: JSONObject()
        val habits = habitsJson.keys().asSequence().associateWith { habitsJson.getInt(it) }
        val defaults = defaultDashboardState()
        val budgetJson = json.optJSONObject("categoryBudgets")
        val categoryBudgets = if (budgetJson == null) {
            defaultCategoryBudgets()
        } else {
            budgetJson.keys().asSequence().associateWith { budgetJson.getLong(it) }
        }
        val transactionsJson = json.optJSONArray("transactions")
        val transactions = if (transactionsJson == null) {
            defaults.transactions
        } else {
            transactionsJson.mapObjects { item ->
                ExpenseTransaction(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    amount = item.getLong("amount"),
                    category = enumValueOf<ExpenseCategory>(item.getString("category")),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    fundId = item.optString("fundId", com.example.personaldashboard.model.DEFAULT_FUND_ID),
                )
            }
        }
        val fundsJson = json.optJSONArray("funds")
        val funds = if (fundsJson == null) {
            val currentMonth = YearMonth.now()
            val zone = ZoneId.systemDefault()
            val currentMonthSpent = transactions.filter {
                YearMonth.from(Instant.ofEpochMilli(it.createdAt).atZone(zone)) == currentMonth
            }.sumOf { it.amount }
            val legacyBalance = (maxOf(
                json.optLong("monthlyIncome", defaults.monthlyIncome),
                json.optLong("monthlyBudget", defaults.monthlyBudget),
            ) - currentMonthSpent).coerceAtLeast(0)
            defaultFunds(legacyBalance)
        } else {
            fundsJson.mapObjects { item ->
                MoneyFund(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    emoji = item.optString("emoji", "👛"),
                    balance = item.optLong("balance", 0),
                    isPrimary = item.optBoolean("isPrimary", false),
                )
            }.ifEmpty { defaultFunds() }
        }
        val normalizedFunds = if (funds.any { it.isPrimary }) funds else funds.mapIndexed { index, fund ->
            fund.copy(isPrimary = index == 0)
        }
        val decoded = DashboardState(
            spaces = spaces.ifEmpty { defaultDashboardState().spaces },
            activeSpaceId = json.optString("activeSpaceId", spaces.firstOrNull()?.id.orEmpty()),
            todos = todos,
            note = json.optString("note", ""),
            habitProgress = habits,
            profileName = json.optString("profileName", ""),
            hasCompletedOnboarding = json.optBoolean("hasCompletedOnboarding", false),
            monthlyIncome = json.optLong("monthlyIncome", defaults.monthlyIncome),
            monthlyBudget = json.optLong("monthlyBudget", defaults.monthlyBudget),
            categoryBudgets = categoryBudgets,
            funds = normalizedFunds,
            transactions = transactions,
            reduceMotion = json.optBoolean("reduceMotion", false),
        )
        // Builds before the finance release always contained demo money and demo
        // transactions. Start that one-time migration clean instead of presenting
        // fictional values as the user's real finances.
        return if (schemaVersion < SCHEMA_VERSION) {
            decoded.copy(
                profileName = "",
                hasCompletedOnboarding = false,
                monthlyIncome = 0,
                monthlyBudget = 0,
                categoryBudgets = defaultCategoryBudgets(),
                transactions = emptyList(),
            )
        } else decoded
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { transform(getJSONObject(it)) }

    private companion object {
        const val KEY_STATE = "dashboard_state"
        const val SCHEMA_VERSION = 2
    }
}
