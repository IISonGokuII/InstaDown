package com.instadown.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instadown.data.local.entity.InstagramAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstagramAccountDao {
    
    @Query("SELECT * FROM instagram_accounts ORDER BY lastUsedAt DESC")
    fun getAllAccounts(): Flow<List<InstagramAccountEntity>>
    
    @Query("SELECT * FROM instagram_accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAccount(): InstagramAccountEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: InstagramAccountEntity)
    
    @Update
    suspend fun updateAccount(account: InstagramAccountEntity)
    
    @Delete
    suspend fun deleteAccount(account: InstagramAccountEntity)
}
