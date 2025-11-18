package com.tys.gymapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore singleton - MUST be at top level
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_preferences"
)

/**
 * ThemeManager - Quản lý theme settings (Dark/Light mode)
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    /**
     * Lấy Dark Mode setting dưới dạng Flow
     */
    val isDarkMode: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false // Default: Light mode
    }

    /**
     * Toggle Dark Mode
     */
    suspend fun toggleDarkMode() {
        context.themeDataStore.edit { preferences ->
            val currentMode = preferences[DARK_MODE_KEY] ?: false
            preferences[DARK_MODE_KEY] = !currentMode
        }
    }

    /**
     * Set Dark Mode explicitly
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}