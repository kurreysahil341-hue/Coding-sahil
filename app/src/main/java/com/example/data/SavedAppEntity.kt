package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_apps")
data class SavedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val description: String,
    val category: String,
    val previewType: String,
    val codeSnippet: String,
    val language: String = "ANDROID", // KOTLIN, TYPESCRIPT, FLUTTER, ANDROID, OTHER
    val lastModified: String = "Today",
    val iconBadgeLetter: String? = null,
    val iconType: String = "android", // "android", "letter", "voice", "code", "book"
    val iconColorHex: Long = 0xFF0F9D58,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
