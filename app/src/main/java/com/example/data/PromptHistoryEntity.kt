package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_history")
data class PromptHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val promptText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentCount: Int = 0,
    val generatedAppTitle: String? = null
)
