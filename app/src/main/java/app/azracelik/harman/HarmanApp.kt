package app.azracelik.harman

import android.app.Application
import app.azracelik.harman.data.local.AppDatabase
import app.azracelik.harman.data.prefs.SettingsStore
import app.azracelik.harman.domain.BudgetRepository

class HarmanApp : Application() {
    /** Küçük uygulama için DI framework yerine elle bağımlılık kapsayıcısı. */
    val repository: BudgetRepository by lazy {
        val db = AppDatabase.create(this)
        BudgetRepository(db.transactionDao(), db.savingsGoalDao(), SettingsStore(this))
    }
}
