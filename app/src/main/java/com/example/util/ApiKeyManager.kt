package com.example.util

import android.content.Context
import android.content.SharedPreferences

object ApiKeyManager {
    private const val PREFS_NAME = "ai_studio_settings"
    private const val KEY_GEMINI_API = "gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GEMINI_API, "") ?: ""
    }

    fun saveApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API, key.trim()).apply()
    }

    fun hasApiKey(context: Context): Boolean {
        val key = getApiKey(context)
        return key.isNotBlank() && key.startsWith("AIza")
    }

    fun clearApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_GEMINI_API).apply()
    }
}
