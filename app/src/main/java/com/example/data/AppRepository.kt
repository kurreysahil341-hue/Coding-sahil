package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allSavedApps: Flow<List<SavedAppEntity>> = appDao.getAllSavedApps()
    val allHistory: Flow<List<PromptHistoryEntity>> = appDao.getAllHistory()

    suspend fun getAppById(id: Long): SavedAppEntity? = appDao.getAppById(id)

    suspend fun insertApp(app: SavedAppEntity): Long = appDao.insertApp(app)

    suspend fun updateApp(app: SavedAppEntity) = appDao.updateApp(app)

    suspend fun deleteApp(id: Long) = appDao.deleteAppById(id)

    suspend fun insertHistory(history: PromptHistoryEntity): Long = appDao.insertHistory(history)

    suspend fun deleteHistory(id: Long) = appDao.deleteHistoryById(id)

    suspend fun clearHistory() = appDao.clearAllHistory()
}
