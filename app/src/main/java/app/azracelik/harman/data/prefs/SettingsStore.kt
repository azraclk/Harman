package app.azracelik.harman.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val limitKey = longPreferencesKey("monthly_limit_minor")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val themeKey = intPreferencesKey("theme_mode")

    /** 0 = limit tanımlı değil. */
    val monthlyLimitMinor: Flow<Long> = context.dataStore.data.map { it[limitKey] ?: 0L }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[onboardingKey] ?: false }
    /** 0 = Sistem, 1 = Açık, 2 = Koyu */
    val themeMode: Flow<Int> = context.dataStore.data.map { it[themeKey] ?: 0 }

    suspend fun setMonthlyLimit(minor: Long) {
        context.dataStore.edit { it[limitKey] = minor.coerceAtLeast(0) }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[onboardingKey] = true }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { it[onboardingKey] = false }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[themeKey] = mode }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
