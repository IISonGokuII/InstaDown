package com.instadown.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.instadown.data.model.Quality
import com.instadown.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "instadown_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val DOWNLOAD_PATH = stringPreferencesKey("download_path")
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val BATTERY_THRESHOLD = intPreferencesKey("battery_threshold")
        val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
        val NOTIFICATION_SUCCESS = booleanPreferencesKey("notification_success")
        val NOTIFICATION_ERROR = booleanPreferencesKey("notification_error")
        val NOTIFICATION_PROGRESS = booleanPreferencesKey("notification_progress")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val DECOY_PIN_ENABLED = booleanPreferencesKey("decoy_pin_enabled")
        val HIDDEN_FOLDER_ENABLED = booleanPreferencesKey("hidden_folder_enabled")
        val NOMEDIA_ENABLED = booleanPreferencesKey("nomedia_enabled")
        val PARALLEL_DOWNLOADS = intPreferencesKey("parallel_downloads")
        val BANDWIDTH_LIMIT = longPreferencesKey("bandwidth_limit")
        val LAST_CLEANUP_TIME = longPreferencesKey("last_cleanup_time")
    }

    // Theme
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode.name
        }
    }

    // Download Quality
    val defaultQuality: Flow<Quality> = dataStore.data.map { prefs ->
        prefs[DEFAULT_QUALITY]?.let { Quality.valueOf(it) } ?: Quality.HD
    }

    suspend fun setDefaultQuality(quality: Quality) {
        dataStore.edit { prefs ->
            prefs[DEFAULT_QUALITY] = quality.name
        }
    }

    // WiFi Only
    val wifiOnly: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[WIFI_ONLY] ?: false
    }

    suspend fun setWifiOnly(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[WIFI_ONLY] = enabled
        }
    }

    // Battery Threshold
    val batteryThreshold: Flow<Int> = dataStore.data.map { prefs ->
        prefs[BATTERY_THRESHOLD] ?: 30
    }

    suspend fun setBatteryThreshold(threshold: Int) {
        dataStore.edit { prefs ->
            prefs[BATTERY_THRESHOLD] = threshold
        }
    }

    // Parallel Downloads
    val parallelDownloads: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PARALLEL_DOWNLOADS] ?: 3
    }

    suspend fun setParallelDownloads(count: Int) {
        dataStore.edit { prefs ->
            prefs[PARALLEL_DOWNLOADS] = count.coerceIn(1, 5)
        }
    }

    // Bandwidth Limit
    val bandwidthLimit: Flow<Long> = dataStore.data.map { prefs ->
        prefs[BANDWIDTH_LIMIT] ?: 0L
    }

    suspend fun setBandwidthLimit(bytesPerSecond: Long) {
        dataStore.edit { prefs ->
            prefs[BANDWIDTH_LIMIT] = bytesPerSecond
        }
    }

    // Notifications
    val notificationSuccess: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATION_SUCCESS] ?: true
    }

    suspend fun setNotificationSuccess(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATION_SUCCESS] = enabled
        }
    }

    val notificationError: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATION_ERROR] ?: true
    }

    suspend fun setNotificationError(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATION_ERROR] = enabled
        }
    }

    val notificationProgress: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATION_PROGRESS] ?: true
    }

    suspend fun setNotificationProgress(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATION_PROGRESS] = enabled
        }
    }

    // Security
    val appLockEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[APP_LOCK_ENABLED] ?: false
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[APP_LOCK_ENABLED] = enabled
        }
    }

    val biometricEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED] = enabled
        }
    }

    val hiddenFolderEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HIDDEN_FOLDER_ENABLED] ?: false
    }

    suspend fun setHiddenFolderEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[HIDDEN_FOLDER_ENABLED] = enabled
        }
    }

    val nomediaEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOMEDIA_ENABLED] ?: true
    }

    suspend fun setNomediaEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOMEDIA_ENABLED] = enabled
        }
    }

    // Auto Delete
    val autoDeleteDays: Flow<Int> = dataStore.data.map { prefs ->
        prefs[AUTO_DELETE_DAYS] ?: 0
    }

    suspend fun setAutoDeleteDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[AUTO_DELETE_DAYS] = days
        }
    }
}
