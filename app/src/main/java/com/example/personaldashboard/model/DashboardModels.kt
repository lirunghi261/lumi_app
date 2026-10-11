package com.example.personaldashboard.model

import java.util.UUID
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

enum class WidgetKind(
    val title: String,
    val subtitle: String,
    val emoji: String,
) {
    CLOCK("Today", "Clock and date", "☀️"),
    WEATHER("Seaside Town", "A tiny weather window", "☁️"),
    OVERVIEW("Today's quest", "Your day at a glance", "🗺️"),
    TODO("Todo", "Tiny wins for today", "✅"),
    NOTE("Quick note", "Capture a thought", "📝"),
    FOCUS("Focus", "A cozy Pomodoro", "🍅"),
    HABIT("Habits", "Grow a little daily", "🌱"),
    COUNTDOWN("Countdown", "Something to look forward to", "🌙"),
}

enum class ThemePreset(
    val title: String,
    val description: String,
    val emoji: String,
) {
    COZY("Cozy Village", "Warm skies, flowers and sleepy cats", "🏡"),
    COSMIC("Cosmic Voyage", "Deep space, planets and starlight", "🪐"),
    SAKURA("Sakura Days", "Soft anime spring afternoons", "🌸"),
    MINIMAL("Minimal Pixel", "Quiet, clean and extra readable", "▫️"),
}

data class DashboardWidget(
    val id: String = UUID.randomUUID().toString(),
    val kind: WidgetKind,
)

data class DashboardSpace(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val theme: ThemePreset,
    val widgets: List<DashboardWidget>,
)

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val done: Boolean = false,
)

enum class ExpenseCategory(val title: String, val emoji: String) {
    FOOD("Ăn uống", "🍜"),
    TRANSPORT("Di chuyển", "🛵"),
    SHOPPING("Mua sắm", "🛍️"),
    FUN("Giải trí", "🎮"),
    BILLS("Hóa đơn", "🧾"),
    OTHER("Khác", "✨"),
}

const val DEFAULT_FUND_ID = "fund-main"

data class MoneyFund(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val balance: Long,
    val isPrimary: Boolean = false,
)

data class ExpenseTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val category: ExpenseCategory,
    val createdAt: Long = System.currentTimeMillis(),
    val fundId: String = DEFAULT_FUND_ID,
)

data class DashboardState(
    val spaces: List<DashboardSpace>,
    val activeSpaceId: String,
    val todos: List<TodoItem>,
    val note: String,
    val habitProgress: Map<String, Int>,
    val profileName: String = "",
    val hasCompletedOnboarding: Boolean = false,
    val monthlyIncome: Long = 0,
    val monthlyBudget: Long = 0,
    val categoryBudgets: Map<String, Long> = defaultCategoryBudgets(),
    val funds: List<MoneyFund> = defaultFunds(),
    val transactions: List<ExpenseTransaction> = emptyList(),
    val reduceMotion: Boolean = false,
) {
    val activeSpace: DashboardSpace
        get() = spaces.firstOrNull { it.id == activeSpaceId } ?: spaces.first()

    val transactionsThisMonth: List<ExpenseTransaction>
        get() {
            val currentMonth = YearMonth.now()
            val zone = ZoneId.systemDefault()
            return transactions.filter {
                YearMonth.from(Instant.ofEpochMilli(it.createdAt).atZone(zone)) == currentMonth
            }
        }

    val spentThisMonth: Long
        get() = transactionsThisMonth.sumOf { it.amount }

    val primaryFund: MoneyFund
        get() = funds.firstOrNull { it.isPrimary } ?: funds.firstOrNull() ?: defaultFunds().first()

    val totalFundBalance: Long
        get() = funds.sumOf { it.balance }

    fun fundFor(id: String): MoneyFund = funds.firstOrNull { it.id == id } ?: primaryFund
}

fun defaultFunds(balance: Long = 0): List<MoneyFund> = listOf(
    MoneyFund(
        id = DEFAULT_FUND_ID,
        name = "Quỹ chính",
        emoji = "💳",
        balance = balance,
        isPrimary = true,
    ),
)

fun defaultCategoryBudgets(total: Long = 0): Map<String, Long> = mapOf(
    ExpenseCategory.FOOD.name to (total * 35 / 100),
    ExpenseCategory.TRANSPORT.name to (total * 15 / 100),
    ExpenseCategory.SHOPPING.name to (total * 15 / 100),
    ExpenseCategory.FUN.name to (total * 10 / 100),
    ExpenseCategory.BILLS.name to (total * 20 / 100),
    ExpenseCategory.OTHER.name to (total * 5 / 100),
)

fun defaultDashboardState(): DashboardState {
    val home = DashboardSpace(
        name = "Personal",
        emoji = "✨",
        theme = ThemePreset.COZY,
        widgets = listOf(
            DashboardWidget(kind = WidgetKind.CLOCK),
            DashboardWidget(kind = WidgetKind.WEATHER),
            DashboardWidget(kind = WidgetKind.OVERVIEW),
            DashboardWidget(kind = WidgetKind.TODO),
            DashboardWidget(kind = WidgetKind.NOTE),
            DashboardWidget(kind = WidgetKind.FOCUS),
            DashboardWidget(kind = WidgetKind.HABIT),
            DashboardWidget(kind = WidgetKind.COUNTDOWN),
        ),
    )
    return DashboardState(
        spaces = listOf(home),
        activeSpaceId = home.id,
        todos = emptyList(),
        note = "",
        habitProgress = emptyMap(),
        funds = defaultFunds(),
        transactions = emptyList(),
    )
}
