package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "ecosystem_links")
data class EcosystemLinkEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val iconKey: String = "REXLIFY",
    val badge: String = "Official",
    val isOfficial: Boolean = true,
    val orderIndex: Int = 0
)

@Dao
interface EcosystemLinkDao {
    @Query("SELECT * FROM ecosystem_links ORDER BY orderIndex ASC, name ASC")
    fun getAllLinks(): Flow<List<EcosystemLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<EcosystemLinkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: EcosystemLinkEntity)

    @Update
    suspend fun updateLink(link: EcosystemLinkEntity)

    @Query("DELETE FROM ecosystem_links WHERE id = :id")
    suspend fun deleteLinkById(id: String)
}
