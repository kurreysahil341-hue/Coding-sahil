package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed class GeminiApiResult {
    data class Success(val generatedText: String, val promptTokens: Int = 0, val candidatesTokens: Int = 0) : GeminiApiResult()
    data class Error(val errorMessage: String, val statusCode: Int? = null) : GeminiApiResult()
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateContent(
        apiKey: String,
        prompt: String,
        modelName: String = "gemini-2.5-flash",
        imageUri: Uri? = null,
        context: Context? = null
    ): GeminiApiResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext GeminiApiResult.Error("API Key is missing. Please enter your Gemini API Key in the settings.")
        }

        try {
            val endpoint = "$BASE_URL/$modelName:generateContent?key=${apiKey.trim()}"

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // 1. Add Image part if attached
            if (imageUri != null && context != null) {
                val imageBase64 = getBase64FromUri(context, imageUri)
                if (imageBase64 != null) {
                    val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                    val inlineData = JSONObject().apply {
                        put("mimeType", mimeType)
                        put("data", imageBase64)
                    }
                    partsArray.put(JSONObject().put("inlineData", inlineData))
                }
            }

            // 2. Add System instructions / context for Android Jetpack Compose coding
            val enhancedPrompt = if (prompt.isNotBlank()) prompt else "Build Android Jetpack Compose layout for this UI design mockup."
            partsArray.put(JSONObject().put("text", enhancedPrompt))

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                // generationConfig
                val genConfig = JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 4096)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "API Error HTTP ${response.code}"
                } catch (e: Exception) {
                    "API Error HTTP ${response.code}: $responseBody"
                }
                return@withContext GeminiApiResult.Error(errorMsg, response.code)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiApiResult.Error("No content returned from Gemini model")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val stringBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    stringBuilder.append(part.getString("text"))
                }
            }

            val usageMetadata = jsonResponse.optJSONObject("usageMetadata")
            val promptTokens = usageMetadata?.optInt("promptTokenCount", 0) ?: 0
            val candidatesTokens = usageMetadata?.optInt("candidatesTokenCount", 0) ?: 0

            GeminiApiResult.Success(
                generatedText = stringBuilder.toString(),
                promptTokens = promptTokens,
                candidatesTokens = candidatesTokens
            )
        } catch (e: Exception) {
            GeminiApiResult.Error("Network error: ${e.localizedMessage ?: e.message}")
        }
    }

    private fun getBase64FromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
