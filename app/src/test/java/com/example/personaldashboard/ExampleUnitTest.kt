package com.example.personaldashboard

import com.example.personaldashboard.model.ExpenseCategory
import com.example.personaldashboard.model.ExpenseTransaction
import com.example.personaldashboard.model.defaultDashboardState
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun freshWalletContainsNoDemoFinancialData() {
        val state = defaultDashboardState()

        assertFalse(state.hasCompletedOnboarding)
        assertEquals(0, state.transactions.size)
        assertEquals(0L, state.monthlyIncome)
        assertEquals(0L, state.monthlyBudget)
        assertEquals(1, state.funds.size)
        assertTrue(state.primaryFund.isPrimary)
        assertEquals(0L, state.totalFundBalance)
    }

    @Test
    fun monthlySpendingExcludesOlderTransactions() {
        val zone = ZoneId.systemDefault()
        val thisMonth = YearMonth.now().atDay(2).atStartOfDay(zone).toInstant().toEpochMilli()
        val previousMonth = YearMonth.now().minusMonths(1).atDay(2).atStartOfDay(zone).toInstant().toEpochMilli()
        val state = defaultDashboardState().copy(
            transactions = listOf(
                ExpenseTransaction(title = "Trong tháng", amount = 40_000, category = ExpenseCategory.FOOD, createdAt = thisMonth),
                ExpenseTransaction(title = "Tháng trước", amount = 90_000, category = ExpenseCategory.FOOD, createdAt = previousMonth),
            ),
        )

        assertEquals(40_000L, state.spentThisMonth)
    }
}
