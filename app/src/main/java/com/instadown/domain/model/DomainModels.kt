package com.instadown.domain.model

enum class ThemeMode {
    LIGHT, DARK, AMOLED, SYSTEM
}

enum class AppLockType {
    NONE, PIN, BIOMETRIC
}

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: ErrorCode? = null) : UiState<Nothing>()
}

enum class ErrorCode {
    RATE_LIMITED,
    CHALLENGE_REQUIRED,
    LOGIN_REQUIRED,
    NOT_FOUND,
    PRIVATE_ACCOUNT,
    NETWORK_ERROR,
    PARSE_ERROR,
    INVALID_URL,
    DOWNLOAD_FAILED,
    INSUFFICIENT_STORAGE,
    UNKNOWN
}

sealed class MainUiEffect {
    data class ShowSnackbar(val message: String, val action: String? = null) : MainUiEffect()
    data class NavigateTo(val route: String) : MainUiEffect()
    data class ShareContent(val url: String) : MainUiEffect()
    data class ShowToast(val message: String) : MainUiEffect()
}

sealed class MainUiIntent {
    data class PasteUrl(val url: String) : MainUiIntent()
    data class SubmitUrl(val url: String) : MainUiIntent()
    data object ClearUrl : MainUiIntent()
    data object RefreshClipboard : MainUiIntent()
    data class DownloadContent(val url: String) : MainUiIntent()
    data class CancelDownload(val id: String) : MainUiIntent()
    data class RetryDownload(val id: String) : MainUiIntent()
    data class DeleteDownload(val id: String) : MainUiIntent()
    data object LoadRecentSearches : MainUiIntent()
    data class ToggleFavorite(val username: String) : MainUiIntent()
    data object NavigateToSettings : MainUiIntent()
    data object NavigateToGallery : MainUiIntent()
    data object NavigateToDownloads : MainUiIntent()
}

data class MainUiState(
    val url: String = "",
    val isUrlValid: Boolean = false,
    val isLoading: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val activeDownloads: Int = 0,
    val clipboardUrl: String? = null,
    val pastedFromClipboard: Boolean = false
)

data class GalleryUiState(
    val isLoading: Boolean = false,
    val groupedMedia: Map<String, List<GalleryItem>> = emptyMap(),
    val selectedItems: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

data class GalleryItem(
    val id: String,
    val filePath: String,
    val thumbnailPath: String?,
    val mediaType: String,
    val username: String,
    val timestamp: Long,
    val fileSize: Long
)

data class DownloadManagerUiState(
    val activeDownloads: List<ActiveDownload> = emptyList(),
    val queuedDownloads: List<QueuedDownload> = emptyList(),
    val completedDownloads: List<CompletedDownload> = emptyList(),
    val totalDownloaded: String = "0 MB",
    val successRate: Int = 0,
    val averageSpeed: String = "0 MB/s"
)

data class ActiveDownload(
    val id: String,
    val username: String?,
    val thumbnailUrl: String?,
    val progress: Int,
    val speed: String,
    val eta: String
)

data class QueuedDownload(
    val id: String,
    val username: String?,
    val thumbnailUrl: String?,
    val position: Int
)

data class CompletedDownload(
    val id: String,
    val username: String?,
    val thumbnailUrl: String?,
    val filePath: String,
    val fileSize: String,
    val completedAt: Long,
    val isSuccessful: Boolean
)

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultQuality: String = "HD",
    val wifiOnly: Boolean = false,
    val batteryThreshold: Int = 30,
    val parallelDownloads: Int = 3,
    val bandwidthLimit: Long = 0,
    val notificationSuccess: Boolean = true,
    val notificationError: Boolean = true,
    val notificationProgress: Boolean = true,
    val appLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val hiddenFolderEnabled: Boolean = false,
    val nomediaEnabled: Boolean = true,
    val autoDeleteDays: Int = 0,
    val downloadPath: String = "",
    val loggedInAccounts: Int = 0
)
