package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM saved_apps ORDER BY createdAt DESC")
    fun getAllSavedApps(): Flow<List<SavedAppEntity>>

    @Query("SELECT * FROM saved_apps WHERE id = :id")
    suspend fun getAppById(id: Long): SavedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: SavedAppEntity): Long

    @Update
    suspend fun updateApp(app: SavedAppEntity)

    @Query("DELETE FROM saved_apps WHERE id = :id")
    suspend fun deleteAppById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_apps")
    suspend fun getAppCount(): Int

    // Prompt History
    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PromptHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PromptHistoryEntity): Long

    @Query("DELETE FROM prompt_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM prompt_history")
    suspend fun clearAllHistory()
}
