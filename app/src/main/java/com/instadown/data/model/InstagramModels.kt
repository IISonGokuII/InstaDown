package com.instadown.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class InstagramPost(
    val shortcode: String,
    val displayUrl: String,
    val caption: String? = null,
    val isVideo: Boolean = false,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val owner: InstagramUser? = null,
    val likes: Int = 0,
    val comments: Int = 0,
    val timestamp: Long? = null,
    val isCarousel: Boolean = false,
    val carouselMedia: List<CarouselMedia> = emptyList(),
    val hashtags: List<String> = emptyList()
)

@Serializable
data class CarouselMedia(
    val id: String,
    val displayUrl: String,
    val isVideo: Boolean = false,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null
)

@Serializable
data class InstagramUser(
    val id: String,
    val username: String,
    val fullName: String? = null,
    val profilePicUrl: String? = null,
    val profilePicUrlHd: String? = null,
    val isPrivate: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val biography: String? = null
)

@Serializable
data class InstagramStory(
    val id: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val thumbnailUrl: String? = null,
    val timestamp: Long,
    val expiryTime: Long,
    val owner: InstagramUser
)

@Serializable
data class InstagramHighlight(
    val id: String,
    val title: String,
    val coverUrl: String? = null,
    val items: List<InstagramStory> = emptyList(),
    val owner: InstagramUser
)

@Serializable
data class InstagramReel(
    val shortcode: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val caption: String? = null,
    val owner: InstagramUser? = null,
    val likes: Int = 0,
    val views: Int = 0,
    val timestamp: Long? = null,
    val audioInfo: AudioInfo? = null
)

@Serializable
data class AudioInfo(
    val title: String? = null,
    val artist: String? = null,
    val audioUrl: String? = null
)

@Serializable
data class DownloadItem(
    val id: String,
    val url: String,
    val mediaType: MediaType,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,
    val filePath: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val downloadedBytes: Long = 0,
    val thumbnailUrl: String? = null,
    val username: String? = null,
    val caption: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val quality: Quality = Quality.HD
)

enum class MediaType {
    POST, REEL, STORY, HIGHLIGHT, IGTV, PROFILE_PICTURE
}

enum class DownloadStatus {
    PENDING, QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}

enum class Quality {
    ORIGINAL, HD, SD
}

@Serializable
data class InstagramSession(
    val sessionId: String,
    val csrfToken: String? = null,
    val dsUserId: String? = null,
    val username: String? = null,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val code: ErrorCode,
    val message: String,
    val retryAfter: Int? = null
)

enum class ErrorCode {
    RATE_LIMITED,
    CHALLENGE_REQUIRED,
    LOGIN_REQUIRED,
    NOT_FOUND,
    PRIVATE_ACCOUNT,
    NETWORK_ERROR,
    PARSE_ERROR,
    UNKNOWN
}

@Serializable
data class GraphQlResponse(
    val data: JsonElement? = null,
    val status: String? = null
)
