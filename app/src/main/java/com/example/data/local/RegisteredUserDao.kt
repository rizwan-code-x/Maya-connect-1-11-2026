package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegisteredUserDao {
    @Query("SELECT * FROM registered_users ORDER BY lastActiveAt DESC")
    fun getAllUsers(): Flow<List<RegisteredUserEntity>>

    @Query("SELECT * FROM registered_users WHERE userId = :id")
    suspend fun getUserById(id: String): RegisteredUserEntity?

    @Query("SELECT COUNT(*) FROM registered_users")
    suspend fun getUsersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: RegisteredUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<RegisteredUserEntity>)

    @Query("UPDATE registered_users SET status = :status WHERE userId = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("UPDATE registered_users SET warningCount = warningCount + 1, status = 'WARNING' WHERE userId = :userId")
    suspend fun issueWarningToUser(userId: String)

    @Query("UPDATE registered_users SET role = :newRole WHERE userId = :userId")
    suspend fun updateUserRole(userId: String, newRole: String)

    @Query("DELETE FROM registered_users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM registered_users")
    suspend fun clearAllUsers()
}
