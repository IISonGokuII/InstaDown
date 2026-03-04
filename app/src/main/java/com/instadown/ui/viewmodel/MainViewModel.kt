package com.instadown.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instadown.data.model.MediaType
import com.instadown.data.repository.DownloadRepository
import com.instadown.data.repository.InstagramRepository
import com.instadown.domain.model.MainUiEffect
import com.instadown.domain.model.MainUiIntent
import com.instadown.domain.model.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val instagramRepository: InstagramRepository,
    private val downloadRepository: DownloadRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sharedUrl = MutableStateFlow<String?>(null)
    val sharedUrl: StateFlow<String?> = _sharedUrl.asStateFlow()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = combine(
        _uiState,
        downloadRepository.getAllDownloads()
    ) { state, downloads ->
        state.copy(
            activeDownloads = downloads.count { 
                it.status == com.instadown.data.model.DownloadStatus.DOWNLOADING 
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    private val _uiEffect = MutableSharedFlow<MainUiEffect>()
    val uiEffect: SharedFlow<MainUiEffect> = _uiEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            checkClipboard()
            loadRecentSearches()
            _isLoading.value = false
        }
    }

    fun setSharedUrl(url: String) {
        _sharedUrl.value = url
    }

    fun clearSharedUrl() {
        _sharedUrl.value = null
    }

    fun processIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.PasteUrl -> {
                _uiState.value = _uiState.value.copy(
                    url = intent.url,
                    isUrlValid = instagramRepository.validateInstagramUrl(intent.url)
                )
            }
            is MainUiIntent.SubmitUrl -> {
                if (instagramRepository.validateInstagramUrl(intent.url)) {
                    startDownload(intent.url)
                } else {
                    viewModelScope.launch {
                        _uiEffect.emit(MainUiEffect.ShowSnackbar("Invalid Instagram URL"))
                    }
                }
            }
            MainUiIntent.ClearUrl -> {
                _uiState.value = _uiState.value.copy(url = "", isUrlValid = false)
            }
            MainUiIntent.RefreshClipboard -> {
                viewModelScope.launch { checkClipboard() }
            }
            is MainUiIntent.DownloadContent -> {
                startDownload(intent.url)
            }
            is MainUiIntent.CancelDownload -> {
                // Cancel download
            }
            is MainUiIntent.RetryDownload -> {
                // Retry download
            }
            is MainUiIntent.DeleteDownload -> {
                viewModelScope.launch {
                    downloadRepository.deleteDownload(intent.id)
                }
            }
            MainUiIntent.LoadRecentSearches -> {
                loadRecentSearches()
            }
            is MainUiIntent.ToggleFavorite -> {
                toggleFavorite(intent.username)
            }
            MainUiIntent.NavigateToSettings -> {
                viewModelScope.launch {
                    _uiEffect.emit(MainUiEffect.NavigateTo("settings"))
                }
            }
            MainUiIntent.NavigateToGallery -> {
                viewModelScope.launch {
                    _uiEffect.emit(MainUiEffect.NavigateTo("gallery"))
                }
            }
            MainUiIntent.NavigateToDownloads -> {
                viewModelScope.launch {
                    _uiEffect.emit(MainUiEffect.NavigateTo("downloads"))
                }
            }
        }
    }

    private fun startDownload(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val shortcode = instagramRepository.extractShortcode(url)
                if (shortcode != null) {
                    // Determine media type from URL
                    val mediaType = when {
                        url.contains("/reel/") -> MediaType.REEL
                        url.contains("/tv/") -> MediaType.IGTV
                        else -> MediaType.POST
                    }
                    
                    downloadRepository.addDownload(
                        url = url,
                        mediaType = mediaType
                    )
                    
                    _uiEffect.emit(MainUiEffect.ShowSnackbar("Download started"))
                    addToRecentSearches(url)
                }
            } catch (e: Exception) {
                _uiEffect.emit(MainUiEffect.ShowSnackbar("Error: ${e.message}"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false, url = "")
            }
        }
    }

    private suspend fun checkClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        
        if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            val text = item.text?.toString()
            
            if (text != null && instagramRepository.validateInstagramUrl(text)) {
                _uiState.value = _uiState.value.copy(clipboardUrl = text)
            }
        }
    }

    private fun loadRecentSearches() {
        // Load from preferences
    }

    private fun addToRecentSearches(url: String) {
        // Save to preferences
    }

    private fun toggleFavorite(username: String) {
        // Toggle favorite
    }
}
