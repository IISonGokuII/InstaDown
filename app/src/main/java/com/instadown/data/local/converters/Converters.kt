package com.instadown.data.local.converters

import androidx.room.TypeConverter

class Converters {
    // Room doesn't need converters for String types
    // These are placeholders for future complex types
    
    @TypeConverter
    fun fromLongList(value: String?): List<Long>? {
        return value?.split(",")?.mapNotNull { it.toLongOrNull() }
    }

    @TypeConverter
    fun toLongList(list: List<Long>?): String? {
        return list?.joinToString(",")
    }
}
