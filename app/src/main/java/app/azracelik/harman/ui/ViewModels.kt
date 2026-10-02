package app.azracelik.harman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.azracelik.harman.HarmanApp
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.domain.BudgetRepository
import app.azracelik.harman.domain.MonthData
import app.azracelik.harman.domain.summarize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInUi(
    scope: kotlinx.coroutines.CoroutineScope,
    initial: T
): StateFlow<T> = stateIn(scope, SharingStarted.WhileSubscribed(5_000), initial)

val ViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer { AppViewModel(repo()) }
    initializer { HomeViewModel(repo()) }
    initializer { ReportViewModel(repo()) }
    initializer { GoalsViewModel(repo()) }
}

private fun CreationExtras.repo(): BudgetRepository =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HarmanApp).repository

/** Uygulama geneli: onboarding durumu + işlem ekle/düzenle/sil. */
class AppViewModel(private val repo: BudgetRepository) : ViewModel() {
    /** null = henüz yüklenmedi. */
    val onboardingDone: StateFlow<Boolean?> =
        repo.onboardingDone.map<Boolean, Boolean?> { it }.stateInUi(viewModelScope, null)

    val monthlyLimit: StateFlow<Long> = repo.monthlyLimitMinor.stateInUi(viewModelScope, 0L)

    fun finishOnboarding(limitMinor: Long?) = viewModelScope.launch {
        if (limitMinor != null) repo.setMonthlyLimit(limitMinor)
        repo.completeOnboarding()
    }

    fun save(transaction: Transaction) = viewModelScope.launch { repo.save(transaction) }
    fun delete(transaction: Transaction) = viewModelScope.launch { repo.delete(transaction) }
}

data class HomeUiState(
    val loaded: Boolean = false,
    val month: MonthData = MonthData(emptyList(), summarize(emptyList(), 0)),
    val goal: SavingsGoal? = null
)

class HomeViewModel(repo: BudgetRepository) : ViewModel() {
    val month: YearMonth = YearMonth.now()

    val state: StateFlow<HomeUiState> =
        combine(repo.month(month), repo.goal) { data, goal -> HomeUiState(true, data, goal) }
            .stateInUi(viewModelScope, HomeUiState())
}

data class ReportUiState(val month: YearMonth, val data: MonthData?)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(private val repo: BudgetRepository) : ViewModel() {
    private val selected = MutableStateFlow(YearMonth.now())

    val state: StateFlow<ReportUiState> = selected
        .flatMapLatest { m -> repo.month(m).map { ReportUiState(m, it) } }
        .stateInUi(viewModelScope, ReportUiState(YearMonth.now(), null))

    fun previous() = selected.update { it.minusMonths(1) }
    fun next() = selected.update { if (it < YearMonth.now()) it.plusMonths(1) else it }
    fun canGoNext(month: YearMonth) = month < YearMonth.now()
}

data class GoalsUiState(val limitMinor: Long = 0, val goal: SavingsGoal? = null)

class GoalsViewModel(private val repo: BudgetRepository) : ViewModel() {
    val state: StateFlow<GoalsUiState> =
        combine(repo.monthlyLimitMinor, repo.goal) { l, g -> GoalsUiState(l, g) }
            .stateInUi(viewModelScope, GoalsUiState())

    fun setLimit(minor: Long) = viewModelScope.launch { repo.setMonthlyLimit(minor) }
    fun saveGoal(name: String, targetMinor: Long) = viewModelScope.launch { repo.saveGoal(name, targetMinor) }
    fun addToGoal(deltaMinor: Long) = viewModelScope.launch { repo.addToGoal(deltaMinor) }
    fun deleteGoal() = viewModelScope.launch { repo.deleteGoal() }
}
