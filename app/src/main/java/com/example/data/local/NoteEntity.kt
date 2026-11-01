package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val colorHex: Long = 0xFF4F46E5,
    val isPinned: Boolean = false
)
