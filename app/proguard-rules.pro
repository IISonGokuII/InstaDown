# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep InstaDown classes
-keep class com.instadown.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepattributes Signature, Exception, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# Keep Instagram response models
-keep class com.instadown.data.model.** { *; }
-keepclassmembers class com.instadown.data.model.** { <fields>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* <fields>;
    @javax.inject.* <init>(...);
}

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# EncryptedSharedPreferences
-keep class androidx.security.** { *; }
-dontwarn com.google.errorprone.annotations.**

# Coil
-keep class coil.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# General
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
