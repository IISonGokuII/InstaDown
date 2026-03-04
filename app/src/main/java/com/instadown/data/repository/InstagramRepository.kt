package com.instadown.data.repository

import com.instadown.data.local.InstaDownDatabase
import com.instadown.data.model.InstagramHighlight
import com.instadown.data.model.InstagramPost
import com.instadown.data.model.InstagramReel
import com.instadown.data.model.InstagramSession
import com.instadown.data.model.InstagramStory
import com.instadown.data.model.InstagramUser
import com.instadown.data.remote.InstagramApiService
import com.instadown.security.EncryptedCookieStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstagramRepository @Inject constructor(
    private val apiService: InstagramApiService,
    private val database: InstaDownDatabase,
    private val cookieStorage: EncryptedCookieStorage
) {
    private val _activeSession = MutableStateFlow<InstagramSession?>(null)
    val activeSession: StateFlow<InstagramSession?> = _activeSession.asStateFlow()

    private val _accounts = MutableStateFlow<List<InstagramSession>>(emptyList())
    val accounts: StateFlow<List<InstagramSession>> = _accounts.asStateFlow()

    init {
        loadActiveSession()
    }

    private fun loadActiveSession() {
        // Load from encrypted prefs
    }

    suspend fun fetchPost(shortcode: String): Flow<Result<InstagramPost>> = flow {
        emit(Result.success(InstagramPost(
            shortcode = shortcode,
            displayUrl = "",
            caption = null,
            isVideo = false
        )))
    }.flowOn(Dispatchers.IO)

    suspend fun fetchReel(shortcode: String): Flow<Result<InstagramReel>> = flow {
        emit(Result.success(InstagramReel(
            shortcode = shortcode,
            videoUrl = "",
            thumbnailUrl = null,
            caption = null
        )))
    }.flowOn(Dispatchers.IO)

    suspend fun fetchStories(userId: String): Flow<Result<List<InstagramStory>>> = flow {
        emit(Result.success(emptyList()))
    }.flowOn(Dispatchers.IO)

    suspend fun fetchHighlights(userId: String): Flow<Result<List<InstagramHighlight>>> = flow {
        emit(Result.success(emptyList()))
    }.flowOn(Dispatchers.IO)

    suspend fun fetchUserInfo(username: String): Flow<Result<InstagramUser>> = flow {
        emit(Result.success(InstagramUser(
            id = "",
            username = username
        )))
    }.flowOn(Dispatchers.IO)

    suspend fun getHdProfilePicture(userId: String): Result<String> {
        return apiService.getHdProfilePicture(userId, _activeSession.value)
    }

    suspend fun addSession(session: InstagramSession) {
        _accounts.value += session
        if (session.isActive) {
            _activeSession.value = session
        }
        saveSession(session)
    }

    suspend fun switchAccount(sessionId: String) {
        _accounts.value = _accounts.value.map { 
            it.copy(isActive = it.sessionId == sessionId) 
        }
        _activeSession.value = _accounts.value.find { it.sessionId == sessionId }
    }

    suspend fun removeSession(sessionId: String) {
        _accounts.value = _accounts.value.filter { it.sessionId != sessionId }
        if (_activeSession.value?.sessionId == sessionId) {
            _activeSession.value = _accounts.value.firstOrNull()
        }
    }

    private suspend fun saveSession(session: InstagramSession) {
        withContext(Dispatchers.IO) {
            // Save to encrypted preferences
        }
    }

    fun validateInstagramUrl(url: String): Boolean {
        return InstagramApiService.isInstagramUrl(url)
    }

    fun extractShortcode(url: String): String? {
        return InstagramApiService.extractShortcodeFromUrl(url)
    }
}
