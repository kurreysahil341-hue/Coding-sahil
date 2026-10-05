package com.example.model

enum class GeminiModel(
    val id: String,
    val displayName: String,
    val badgeLabel: String,
    val speedInfo: String,
    val description: String,
    val executionSeconds: Int
) {
    GEMINI_PRO(
        id = "gemini-pro",
        displayName = "Gemini Pro",
        badgeLabel = "Gemini Pro",
        speedInfo = "Ran for 120s",
        description = "Balanced reasoning & reliable Kotlin syntax generation.",
        executionSeconds = 120
    ),
    GEMINI_3_5(
        id = "gemini-3.5",
        displayName = "Gemini 3.5",
        badgeLabel = "Gemini 3.5 Flash",
        speedInfo = "Ran for 180s",
        description = "High-speed multi-file architecture with optimized latency.",
        executionSeconds = 180
    ),
    GEMINI_3_8(
        id = "gemini-3.8",
        displayName = "Gemini 3.8",
        badgeLabel = "Gemini 3.8 Flash",
        speedInfo = "Ran for 240s",
        description = "Flagship model with deep Compose AST reasoning & Room integration.",
        executionSeconds = 240
    );

    companion object {
        val DEFAULT = GEMINI_3_8
    }
}
