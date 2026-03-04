package com.instadown.data.remote

import com.instadown.data.model.AudioInfo
import com.instadown.data.model.CarouselMedia
import com.instadown.data.model.GraphQlResponse
import com.instadown.data.model.InstagramHighlight
import com.instadown.data.model.InstagramPost
import com.instadown.data.model.InstagramReel
import com.instadown.data.model.InstagramSession
import com.instadown.data.model.InstagramStory
import com.instadown.data.model.InstagramUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstagramApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://www.instagram.com"
    private val graphQlUrl = "$baseUrl/graphql/query"
    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    )

    private fun getRandomUserAgent(): String = userAgents.random()
    private suspend fun randomDelay() = delay((1000L..3000L).random())

    suspend fun getPostInfo(shortcode: String, session: InstagramSession? = null): Result<InstagramPost> {
        return try {
            randomDelay()
            
            val queryHash = "b3055c01b4b222b8a47dc12b090e4e64"
            val variables = """{"shortcode":"$shortcode","child_comment_count":3,"fetch_comment_count":40,"parent_comment_count":24,"has_threaded_comments":true}"""
            
            val response = httpClient.get(graphQlUrl) {
                parameter("query_hash", queryHash)
                parameter("variables", variables)
                header(HttpHeaders.UserAgent, getRandomUserAgent())
                header(HttpHeaders.Referrer, "$baseUrl/p/$shortcode/")
                session?.let {
                    header(HttpHeaders.Cookie, "sessionid=${it.sessionId}")
                }
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val json = response.body<GraphQlResponse>()
                    val post = parsePostFromGraphQl(json)
                    Result.success(post)
                }
                HttpStatusCode.TooManyRequests -> {
                    Result.failure(Exception("Rate limited"))
                }
                else -> {
                    Result.failure(Exception("HTTP ${response.status}"))
                }
            }
        } catch (e: ClientRequestException) {
            Result.failure(e)
        } catch (e: ServerResponseException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReelInfo(shortcode: String, session: InstagramSession? = null): Result<InstagramReel> {
        return try {
            randomDelay()
            
            val response = httpClient.get("$baseUrl/reel/$shortcode/") {
                header(HttpHeaders.UserAgent, getRandomUserAgent())
                session?.let {
                    header(HttpHeaders.Cookie, "sessionid=${it.sessionId}")
                }
            }

            val html = response.bodyAsText()
            val reel = parseReelFromHtml(html, shortcode)
            Result.success(reel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStories(userId: String, session: InstagramSession): Result<List<InstagramStory>> {
        return try {
            randomDelay()
            
            val queryHash = "f5dc1457da7a4d3f88762dae127e0238"
            val variables = """{"reel_ids":["$userId"],"tag_names":[],"location_ids":[],"highlight_reel_ids":[],"precomposed_overlay":false,"show_story_viewer_list":true,"story_viewer_fetch_count":50,"story_viewer_cursor":"","stories_video_dash_manifest":false}"""
            
            val response = httpClient.get(graphQlUrl) {
                parameter("query_hash", queryHash)
                parameter("variables", variables)
                header(HttpHeaders.UserAgent, getRandomUserAgent())
                header(HttpHeaders.Cookie, "sessionid=${session.sessionId}")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val stories = parseStoriesFromResponse(response.body())
                    Result.success(stories)
                }
                else -> Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserHighlights(userId: String, session: InstagramSession): Result<List<InstagramHighlight>> {
        return try {
            randomDelay()
            
            val queryHash = "a88ec8c1f3b2a590ae5f0f11e3e8d1c0"
            val variables = """{"user_id":"$userId","include_chaining":false,"include_reel":false,"include_suggested_users":false,"include_logged_out_extras":false,"include_highlight_reels":true}"""
            
            val response = httpClient.get(graphQlUrl) {
                parameter("query_hash", queryHash)
                parameter("variables", variables)
                header(HttpHeaders.UserAgent, getRandomUserAgent())
                header(HttpHeaders.Cookie, "sessionid=${session.sessionId}")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val highlights = parseHighlightsFromResponse(response.body())
                    Result.success(highlights)
                }
                else -> Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(username: String, session: InstagramSession? = null): Result<InstagramUser> {
        return try {
            randomDelay()
            
            val response = httpClient.get("$baseUrl/$username/") {
                header(HttpHeaders.UserAgent, getRandomUserAgent())
                session?.let {
                    header(HttpHeaders.Cookie, "sessionid=${it.sessionId}")
                }
            }

            val html = response.bodyAsText()
            val user = parseUserFromHtml(html, username)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHdProfilePicture(userId: String, session: InstagramSession? = null): Result<String> {
        return try {
            randomDelay()
            
            val queryHash = "c9100bf9110dd6361671f113dd02e7d6"
            val variables = """{"user_id":"$userId","include_chaining":false,"include_reel":false,"include_suggested_users":false,"include_logged_out_extras":false,"include_highlight_reels":false}"""
            
            val response = httpClient.get(graphQlUrl) {
                parameter("query_hash", queryHash)
                parameter("variables", variables)
                header(HttpHeaders.UserAgent, getRandomUserAgent())
                session?.let {
                    header(HttpHeaders.Cookie, "sessionid=${it.sessionId}")
                }
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val json = response.body<GraphQlResponse>()
                    val hdUrl = parseHdProfilePicFromGraphQl(json)
                    Result.success(hdUrl)
                }
                else -> Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Private parsing methods
    private fun parsePostFromGraphQl(json: GraphQlResponse): InstagramPost {
        // Implementation would parse the actual GraphQL response
        return InstagramPost(
            shortcode = "",
            displayUrl = "",
            caption = null
        )
    }

    private fun parseReelFromHtml(html: String, shortcode: String): InstagramReel {
        // Parse embedded JSON from HTML
        return InstagramReel(
            shortcode = shortcode,
            videoUrl = "",
            thumbnailUrl = null,
            caption = null
        )
    }

    private fun parseStoriesFromResponse(json: GraphQlResponse): List<InstagramStory> {
        return emptyList()
    }

    private fun parseHighlightsFromResponse(json: GraphQlResponse): List<InstagramHighlight> {
        return emptyList()
    }

    private fun parseUserFromHtml(html: String, username: String): InstagramUser {
        return InstagramUser(
            id = "",
            username = username
        )
    }

    private fun parseHdProfilePicFromGraphQl(json: GraphQlResponse): String {
        return ""
    }

    companion object {
        fun extractShortcodeFromUrl(url: String): String? {
            val patterns = listOf(
                Regex("instagram\\.com/p/([\\w-]+)"),
                Regex("instagram\\.com/reel/([\\w-]+)"),
                Regex("instagram\\.com/tv/([\\w-]+)")
            )
            
            return patterns.firstNotNullOfOrNull { pattern ->
                pattern.find(url)?.groupValues?.get(1)
            }
        }

        fun isInstagramUrl(url: String): Boolean {
            return url.contains("instagram.com") && 
                   (url.contains("/p/") || url.contains("/reel/") || url.contains("/tv/"))
        }
    }
}
