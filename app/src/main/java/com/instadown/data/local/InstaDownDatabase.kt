package com.instadown.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instadown.data.local.dao.DownloadDao
import com.instadown.data.local.dao.InstagramAccountDao
import com.instadown.data.local.entity.DownloadEntity
import com.instadown.data.local.entity.InstagramAccountEntity
import com.instadown.data.local.converters.Converters

@Database(
    entities = [
        DownloadEntity::class,
        InstagramAccountEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InstaDownDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun instagramAccountDao(): InstagramAccountDao
}
