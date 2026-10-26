package app.azracelik.harman.domain

import app.azracelik.harman.data.local.MonthLimitDao
import app.azracelik.harman.data.local.SavingsGoalDao
import app.azracelik.harman.data.local.TransactionDao
import app.azracelik.harman.data.model.MonthLimit
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.data.model.toIndex
import app.azracelik.harman.data.prefs.SettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth

data class MonthData(val transactions: List<Transaction>, val summary: MonthSummary)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val goalDao: SavingsGoalDao,
    private val limitDao: MonthLimitDao,
    private val settings: SettingsStore
) {

    /** Bugünün ayı; gece yarısı ay değişince yeniden yayınlanır. */
    val currentMonth: Flow<YearMonth> = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(YearMonth.from(now))
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            delay(Duration.between(now, nextMidnight).toMillis() + 1_000)
        }
    }.distinctUntilChanged()

    /** Bu ay geçerli limit. */
    val monthlyLimitMinor: Flow<Long> = currentMonth.flatMapLatest { limitFor(it) }
    val onboardingDone: Flow<Boolean> = settings.onboardingDone
    val themeMode: Flow<Int> = settings.themeMode
    val goals: Flow<List<SavingsGoal>> = goalDao.observeAll()

    /** [month] için geçerli limit: o aya veya öncesine ait son kayıt, yoksa eski genel limit. */
    private fun limitFor(month: YearMonth): Flow<Long> =
        combine(limitDao.observeLatestUpTo(month.toIndex()), settings.monthlyLimitMinor) { entry, legacy ->
            entry?.limitMinor ?: legacy
        }

    fun month(month: YearMonth): Flow<MonthData> =
        combine(
            transactionDao.observeBetween(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay()),
            limitFor(month)
        ) { list, limit -> MonthData(list, summarize(list, limit)) }

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)
    suspend fun save(transaction: Transaction) = transactionDao.upsert(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    /** Limiti bu aydan itibaren günceller; önceki aylar etkilenmez. */
    suspend fun setMonthlyLimit(minor: Long) =
        limitDao.upsert(MonthLimit(YearMonth.now().toIndex(), minor.coerceAtLeast(0)))
    suspend fun completeOnboarding() = settings.completeOnboarding()
    suspend fun resetOnboarding() = settings.resetOnboarding()
    suspend fun setThemeMode(mode: Int) = settings.setThemeMode(mode)

    suspend fun clearAllData() {
        transactionDao.clear()
        goalDao.clear()
        limitDao.clear()
        settings.clearAll()
    }

    suspend fun saveGoal(id: Long = 0L, name: String, targetMinor: Long) {
        val existing = if (id > 0L) goalDao.getById(id) else null
        val saved = existing?.savedMinor ?: 0L
        goalDao.upsert(SavingsGoal(id = id, name = name, targetMinor = targetMinor, savedMinor = saved))
    }

    /** Birikime ekler (pozitif) veya çıkarır (negatif); 0'ın altına inmez. */
    suspend fun addToGoal(goalId: Long, deltaMinor: Long) {
        val current = goalDao.getById(goalId) ?: return
        goalDao.upsert(current.copy(savedMinor = (current.savedMinor + deltaMinor).coerceAtLeast(0)))
    }

    suspend fun deleteGoal(goal: SavingsGoal) = goalDao.delete(goal)
}
