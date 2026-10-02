package app.azracelik.harman.domain

import app.azracelik.harman.data.model.Category
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.data.model.TransactionType

enum class BudgetLevel { NO_LIMIT, OK, WARNING, OVER }

data class CategoryTotal(val category: Category, val totalMinor: Long, val share: Float)

data class MonthSummary(
    val incomeMinor: Long,
    val expenseMinor: Long,
    val limitMinor: Long,
    val expenseByCategory: List<CategoryTotal>
) {
    val balanceMinor: Long get() = incomeMinor - expenseMinor
    val hasLimit: Boolean get() = limitMinor > 0

    /** Limite göre kalan tutar; limit aşıldıysa negatif. */
    val remainingMinor: Long get() = limitMinor - expenseMinor

    /** 0..1 aralığına kırpılmış kullanım oranı (halka için). */
    val usedFraction: Float
        get() = if (!hasLimit) 0f else (expenseMinor.toFloat() / limitMinor).coerceIn(0f, 1f)

    val level: BudgetLevel
        get() = when {
            !hasLimit -> BudgetLevel.NO_LIMIT
            expenseMinor >= limitMinor -> BudgetLevel.OVER
            expenseMinor * 100 >= limitMinor * WARNING_PERCENT -> BudgetLevel.WARNING
            else -> BudgetLevel.OK
        }

    companion object {
        const val WARNING_PERCENT = 80
    }
}

fun summarize(transactions: List<Transaction>, limitMinor: Long): MonthSummary {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val expense = expenses.sumOf { it.amountMinor }
    val byCategory = expenses
        .groupBy { it.category }
        .map { (category, list) ->
            val total = list.sumOf { it.amountMinor }
            CategoryTotal(category, total, if (expense > 0) total.toFloat() / expense else 0f)
        }
        .sortedByDescending { it.totalMinor }
    return MonthSummary(income, expense, limitMinor, byCategory)
}
