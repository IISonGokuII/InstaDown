package com.instadown.data.local.converters

import androidx.room.TypeConverter
import com.instadown.data.model.DownloadStatus
import com.instadown.data.model.MediaType
import com.instadown.data.model.Quality

class Converters {
    
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name
    
    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
    
    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name
    
    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
    
    @TypeConverter
    fun fromQuality(value: Quality): String = value.name
    
    @TypeConverter
    fun toQuality(value: String): Quality = Quality.valueOf(value)
}
