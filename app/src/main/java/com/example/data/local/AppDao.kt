package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps WHERE isEnabled = 1 ORDER BY isFeatured DESC, launchCount DESC, name ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY name ASC")
    fun getAllAppsAdmin(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isFavorite = 1 AND isEnabled = 1 ORDER BY name ASC")
    fun getFavoriteApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE lastOpenedTimestamp > 0 AND isEnabled = 1 ORDER BY lastOpenedTimestamp DESC LIMIT 10")
    fun getRecentlyUsedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isFeatured = 1 AND isEnabled = 1")
    fun getFeaturedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE id = :id LIMIT 1")
    suspend fun getAppById(id: String): AppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Query("UPDATE apps SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE apps SET isPinnedHome = :isPinned WHERE id = :id")
    suspend fun updatePinnedHome(id: String, isPinned: Boolean)

    @Query("UPDATE apps SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAppEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE apps SET statusName = :statusName WHERE id = :id")
    suspend fun updateAppStatus(id: String, statusName: String)

    @Query("UPDATE apps SET lastOpenedTimestamp = :timestamp, launchCount = launchCount + 1 WHERE id = :id")
    suspend fun recordAppLaunch(id: String, timestamp: Long)

    @Query("UPDATE apps SET url = :newUrl WHERE id = :id")
    suspend fun updateAppUrl(id: String, newUrl: String)

    @Delete
    suspend fun deleteApp(app: AppEntity)

    @Query("DELETE FROM apps WHERE id = :id")
    suspend fun deleteAppById(id: String)

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun getAppsCount(): Int

    @Query("DELETE FROM apps")
    suspend fun clearAllApps()
}
