package app.azracelik.harman.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val limitKey = longPreferencesKey("monthly_limit_minor")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")

    /** 0 = limit tanımlı değil. */
    val monthlyLimitMinor: Flow<Long> = context.dataStore.data.map { it[limitKey] ?: 0L }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[onboardingKey] ?: false }

    suspend fun setMonthlyLimit(minor: Long) {
        context.dataStore.edit { it[limitKey] = minor.coerceAtLeast(0) }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[onboardingKey] = true }
    }
}
