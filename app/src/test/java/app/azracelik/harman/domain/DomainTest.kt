package app.azracelik.harman.domain

import app.azracelik.harman.data.model.Category
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainTest {

    private fun tx(amount: Long, type: TransactionType, cat: Category) =
        Transaction(amountMinor = amount, type = type, category = cat, epochDay = 0)

    @Test
    fun parseMoney_handlesTurkishAndPlainFormats() {
        assertEquals(125_000L, parseMoneyToMinor("1.250"))
        assertEquals(125_050L, parseMoneyToMinor("1.250,5"))
        assertEquals(125_050L, parseMoneyToMinor("1250.50"))
        assertEquals(1_200L, parseMoneyToMinor("12"))
        assertEquals(1_250L, parseMoneyToMinor("12,5"))
    }

    @Test
    fun parseMoney_rejectsInvalid() {
        assertNull(parseMoneyToMinor(""))
        assertNull(parseMoneyToMinor("abc"))
        assertNull(parseMoneyToMinor("0"))
        assertNull(parseMoneyToMinor("-5"))
    }

    @Test
    fun minorToInput_roundTrips() {
        assertEquals("1250", minorToInput(125_000))
        assertEquals("12,5", minorToInput(1_250))
        assertEquals("12,05", minorToInput(1_205))
        assertEquals(1_205L, parseMoneyToMinor(minorToInput(1_205)))
    }

    @Test
    fun summarize_totalsAndCategoryShares() {
        val s = summarize(
            listOf(
                tx(100_000, TransactionType.INCOME, Category.SALARY),
                tx(30_000, TransactionType.EXPENSE, Category.MARKET),
                tx(10_000, TransactionType.EXPENSE, Category.MARKET),
                tx(10_000, TransactionType.EXPENSE, Category.BILLS)
            ),
            limitMinor = 100_000
        )
        assertEquals(100_000L, s.incomeMinor)
        assertEquals(50_000L, s.expenseMinor)
        assertEquals(50_000L, s.balanceMinor)
        assertEquals(50_000L, s.remainingMinor)
        assertEquals(Category.MARKET, s.expenseByCategory.first().category)
        assertEquals(0.8f, s.expenseByCategory.first().share, 0.001f)
    }

    @Test
    fun budgetLevel_thresholds() {
        fun level(expense: Long, limit: Long) =
            summarize(listOf(tx(expense, TransactionType.EXPENSE, Category.MARKET)), limit).level

        assertEquals(BudgetLevel.NO_LIMIT, level(100, 0))
        assertEquals(BudgetLevel.OK, level(7_900, 10_000))
        assertEquals(BudgetLevel.WARNING, level(8_000, 10_000))
        assertEquals(BudgetLevel.OVER, level(10_000, 10_000))
    }

    @Test
    fun usedFraction_isClamped() {
        val s = summarize(listOf(tx(20_000, TransactionType.EXPENSE, Category.MARKET)), 10_000)
        assertEquals(1f, s.usedFraction, 0f)
        assertEquals(-10_000L, s.remainingMinor)
    }
}
