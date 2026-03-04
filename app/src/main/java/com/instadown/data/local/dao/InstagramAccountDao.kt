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
    
    @Query("SELECT * FROM instagram_accounts WHERE id = :id")
    suspend fun getAccountById(id: String): InstagramAccountEntity?
    
    @Query("SELECT * FROM instagram_accounts WHERE username = :username")
    suspend fun getAccountByUsername(username: String): InstagramAccountEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: InstagramAccountEntity)
    
    @Update
    suspend fun updateAccount(account: InstagramAccountEntity)
    
    @Query("UPDATE instagram_accounts SET isActive = 0")
    suspend fun deactivateAllAccounts()
    
    @Query("UPDATE instagram_accounts SET isActive = 1 WHERE id = :id")
    suspend fun setActiveAccount(id: String)
    
    @Query("UPDATE instagram_accounts SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)
    
    @Query("UPDATE instagram_accounts SET rateLimitCount = rateLimitCount + 1, lastRateLimitAt = :timestamp WHERE id = :id")
    suspend fun incrementRateLimit(id: String, timestamp: Long)
    
    @Query("UPDATE instagram_accounts SET rateLimitCount = 0 WHERE id = :id")
    suspend fun resetRateLimit(id: String)
    
    @Delete
    suspend fun deleteAccount(account: InstagramAccountEntity)
    
    @Query("DELETE FROM instagram_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: String)
    
    @Query("SELECT COUNT(*) FROM instagram_accounts")
    suspend fun getAccountCount(): Int
    
    @Query("SELECT * FROM instagram_accounts WHERE isLoggedIn = 1 ORDER BY lastUsedAt DESC")
    fun getLoggedInAccounts(): Flow<List<InstagramAccountEntity>>
}
