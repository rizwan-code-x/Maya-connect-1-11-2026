package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val details: String = "",
    val dueDate: String = "Today",
    val dueTime: String = "",
    val listCategory: String = "Personal", // "Personal", "Work", "Shopping", "Health"
    val priority: String = "Medium", // "Low", "Medium", "High"
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L
)
