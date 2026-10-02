package app.azracelik.harman.domain

import app.azracelik.harman.data.local.SavingsGoalDao
import app.azracelik.harman.data.local.TransactionDao
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.data.prefs.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

data class MonthData(val transactions: List<Transaction>, val summary: MonthSummary)

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val goalDao: SavingsGoalDao,
    private val settings: SettingsStore
) {
    val monthlyLimitMinor: Flow<Long> = settings.monthlyLimitMinor
    val onboardingDone: Flow<Boolean> = settings.onboardingDone
    val goal: Flow<SavingsGoal?> = goalDao.observe()

    fun month(month: YearMonth): Flow<MonthData> =
        combine(
            transactionDao.observeBetween(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay()),
            settings.monthlyLimitMinor
        ) { list, limit -> MonthData(list, summarize(list, limit)) }

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)
    suspend fun save(transaction: Transaction) = transactionDao.upsert(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun setMonthlyLimit(minor: Long) = settings.setMonthlyLimit(minor)
    suspend fun completeOnboarding() = settings.completeOnboarding()

    suspend fun saveGoal(name: String, targetMinor: Long) {
        val saved = goalDao.get()?.savedMinor ?: 0L
        goalDao.upsert(SavingsGoal(name = name, targetMinor = targetMinor, savedMinor = saved))
    }

    /** Birikime ekler (pozitif) veya çıkarır (negatif); 0'ın altına inmez. */
    suspend fun addToGoal(deltaMinor: Long) {
        val current = goalDao.get() ?: return
        goalDao.upsert(current.copy(savedMinor = (current.savedMinor + deltaMinor).coerceAtLeast(0)))
    }

    suspend fun deleteGoal() = goalDao.clear()
}
