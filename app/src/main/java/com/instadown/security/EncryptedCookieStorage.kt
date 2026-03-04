package com.instadown.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import io.ktor.util.date.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.collections.set

class EncryptedCookieStorage(
    context: Context,
    private val encryptedPrefs: EncryptedSharedPreferences
) : io.ktor.client.plugins.cookies.CookiesStorage {

    private val mutex = Mutex()
    private val container: MutableMap<String, MutableList<CookieWrapper>> = mutableMapOf()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val COOKIE_KEY = "instagram_cookies"
    }

    init {
        loadCookies()
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        mutex.withLock {
            val domain = requestUrl.host
            val cookies = container.getOrPut(domain) { mutableListOf() }
            
            // Remove existing cookie with same name
            cookies.removeAll { it.name == cookie.name }
            
            // Add new cookie
            cookies.add(CookieWrapper.fromCookie(cookie, domain))
            
            saveCookies()
        }
    }

    override fun close() {
        // Nothing to close
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        return mutex.withLock {
            val domain = requestUrl.host
            val cookies = container[domain] ?: return emptyList()
            
            val now = GMTDate().timestamp
            cookies.removeAll { it.expiresAt != null && it.expiresAt < now }
            
            cookies.map { it.toCookie() }
        }
    }

    suspend fun clearCookies() {
        mutex.withLock {
            container.clear()
            encryptedPrefs.edit().remove(COOKIE_KEY).apply()
        }
    }

    suspend fun getSessionId(): String? {
        return mutex.withLock {
            container.values.flatten().find { it.name == "sessionid" }?.value
        }
    }

    suspend fun setSessionId(sessionId: String) {
        mutex.withLock {
            val domain = "instagram.com"
            val cookies = container.getOrPut(domain) { mutableListOf() }
            
            cookies.removeAll { it.name == "sessionid" }
            
            cookies.add(
                CookieWrapper(
                    name = "sessionid",
                    value = sessionId,
                    domain = domain,
                    path = "/",
                    expiresAt = GMTDate().plus(GMTDate.DAY * 30).timestamp,
                    secure = true,
                    httpOnly = true
                )
            )
            
            saveCookies()
        }
    }

    private fun saveCookies() {
        val cookiesJson = json.encodeToString(container)
        encryptedPrefs.edit().putString(COOKIE_KEY, cookiesJson).apply()
    }

    private fun loadCookies() {
        val cookiesJson = encryptedPrefs.getString(COOKIE_KEY, null)
        if (cookiesJson != null) {
            try {
                val loaded = json.decodeFromString<Map<String, List<CookieWrapper>>>(cookiesJson)
                container.putAll(loaded.mapValues { it.value.toMutableList() })
            } catch (e: Exception) {
                // Invalid cookies, clear them
                encryptedPrefs.edit().remove(COOKIE_KEY).apply()
            }
        }
    }

    @Serializable
    data class CookieWrapper(
        val name: String,
        val value: String,
        val domain: String,
        val path: String = "/",
        val expiresAt: Long? = null,
        val secure: Boolean = true,
        val httpOnly: Boolean = false
    ) {
        fun toCookie(): Cookie {
            return Cookie(
                name = name,
                value = value,
                domain = domain,
                path = path,
                expires = expiresAt?.let { GMTDate(it) },
                secure = secure,
                httpOnly = httpOnly
            )
        }

        companion object {
            fun fromCookie(cookie: Cookie, defaultDomain: String): CookieWrapper {
                return CookieWrapper(
                    name = cookie.name,
                    value = cookie.value,
                    domain = cookie.domain ?: defaultDomain,
                    path = cookie.path ?: "/",
                    expiresAt = cookie.expires?.timestamp,
                    secure = cookie.secure,
                    httpOnly = cookie.httpOnly
                )
            }
        }
    }
}
