package com.instadown.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.instadown.data.local.InstaDownDatabase
import com.instadown.data.local.preferences.SettingsDataStore
import com.instadown.data.remote.InstagramApiService
import com.instadown.data.repository.DownloadRepository
import com.instadown.data.repository.InstagramRepository
import com.instadown.download.DownloadEngine
import com.instadown.security.EncryptedCookieStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    @Provides
    @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        masterKey: MasterKey
    ): EncryptedSharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "instadown_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    @Provides
    @Singleton
    fun provideCookieStorage(
        @ApplicationContext context: Context,
        encryptedPrefs: EncryptedSharedPreferences
    ): EncryptedCookieStorage {
        return EncryptedCookieStorage(context, encryptedPrefs)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        okHttpClient: OkHttpClient,
        cookieStorage: EncryptedCookieStorage,
        json: Json
    ): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient
            }
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpCookies) {
                storage = cookieStorage
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60000
                connectTimeoutMillis = 30000
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InstaDownDatabase {
        return Room.databaseBuilder(
            context,
            InstaDownDatabase::class.java,
            "instadown.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideInstagramApiService(httpClient: HttpClient): InstagramApiService {
        return InstagramApiService(httpClient)
    }

    @Provides
    @Singleton
    fun provideInstagramRepository(
        apiService: InstagramApiService,
        database: InstaDownDatabase,
        cookieStorage: EncryptedCookieStorage
    ): InstagramRepository {
        return InstagramRepository(apiService, database, cookieStorage)
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        database: InstaDownDatabase,
        @ApplicationContext context: Context
    ): DownloadRepository {
        return DownloadRepository(database, context)
    }

    @Provides
    @Singleton
    fun provideDownloadEngine(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
        downloadRepository: DownloadRepository
    ): DownloadEngine {
        return DownloadEngine(context, httpClient, downloadRepository)
    }
}
