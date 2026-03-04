package com.instadown.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instadown.data.local.preferences.SettingsDataStore
import com.instadown.domain.model.SettingsUiState
import com.instadown.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.themeMode,
        settingsDataStore.defaultQuality,
        settingsDataStore.wifiOnly,
        settingsDataStore.batteryThreshold,
        settingsDataStore.parallelDownloads,
        settingsDataStore.bandwidthLimit,
        settingsDataStore.notificationSuccess,
        settingsDataStore.notificationError,
        settingsDataStore.notificationProgress,
        settingsDataStore.appLockEnabled,
        settingsDataStore.biometricEnabled,
        settingsDataStore.hiddenFolderEnabled,
        settingsDataStore.nomediaEnabled,
        settingsDataStore.autoDeleteDays
    ) { values ->
        SettingsUiState(
            themeMode = values[0] as ThemeMode,
            defaultQuality = values[1] as String,
            wifiOnly = values[2] as Boolean,
            batteryThreshold = values[3] as Int,
            parallelDownloads = values[4] as Int,
            bandwidthLimit = values[5] as Long,
            notificationSuccess = values[6] as Boolean,
            notificationError = values[7] as Boolean,
            notificationProgress = values[8] as Boolean,
            appLockEnabled = values[9] as Boolean,
            biometricEnabled = values[10] as Boolean,
            hiddenFolderEnabled = values[11] as Boolean,
            nomediaEnabled = values[12] as Boolean,
            autoDeleteDays = values[13] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setWifiOnly(enabled)
        }
    }

    fun setBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsDataStore.setBatteryThreshold(threshold)
        }
    }

    fun setParallelDownloads(count: Int) {
        viewModelScope.launch {
            settingsDataStore.setParallelDownloads(count)
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAppLockEnabled(enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setBiometricEnabled(enabled)
        }
    }

    fun setHiddenFolderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setHiddenFolderEnabled(enabled)
        }
    }

    fun setNomediaEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNomediaEnabled(enabled)
        }
    }

    fun setNotificationSuccess(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationSuccess(enabled)
        }
    }

    fun setNotificationError(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationError(enabled)
        }
    }

    fun setNotificationProgress(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationProgress(enabled)
        }
    }
}
