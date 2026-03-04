package com.instadown.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instagram_accounts")
data class InstagramAccountEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val fullName: String? = null,
    val profilePicUrl: String? = null,
    val sessionId: String? = null,
    val csrfToken: String? = null,
    val dsUserId: String? = null,
    val isActive: Boolean = false,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val rateLimitCount: Int = 0,
    val lastRateLimitAt: Long? = null
)
