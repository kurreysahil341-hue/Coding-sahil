package com.example.model

import androidx.compose.ui.graphics.Color

enum class Screen {
    HOME,
    WORKSPACE,
    MY_APPS,
    HISTORY,
    PLAYGROUND,
    GALLERY
}

enum class ProjectLanguage {
    KOTLIN,
    TYPESCRIPT,
    FLUTTER,
    ANDROID,
    OTHER
}

// भाषा के हिसाब से Dynamic Color सेट करना
fun getLanguageColor(language: ProjectLanguage): Color {
    return when (language) {
        ProjectLanguage.KOTLIN -> Color(0xFF7F52FF)     // Purple
        ProjectLanguage.TYPESCRIPT -> Color(0xFF3178C6) // Blue / Cyan
        ProjectLanguage.FLUTTER -> Color(0xFF02569B)    // Teal / Flutter Blue
        ProjectLanguage.ANDROID -> Color(0xFF0F9D58)    // Android Green
        ProjectLanguage.OTHER -> Color(0xFFE11D48)      // Rose / Red / Other
    }
}

data class AppProject(
    val id: String,
    val title: String,
    val description: String,
    val language: ProjectLanguage,
    val lastModified: String,
    val iconBadgeLetter: String? = null,
    val iconType: String = "android",
    val prompt: String = "",
    val codeSnippet: String = "",
    val previewType: String = "ANDROID_APP"
)

enum class BuildStep(val stepNumber: Int, val title: String, val detail: String) {
    ANALYZING(1, "Analyzing intent & requirements", "Parsing prompt semantics and technical dependencies..."),
    ARCHITECTING(2, "Designing component architecture", "Structuring Room entities, ViewModel flows, and M3 design tokens..."),
    CODING(3, "Writing Jetpack Compose UI", "Synthesizing reactive UI, state management, and modern animations..."),
    COMPILING(4, "Compiling interactive preview", "Assembling live preview sandbox & syntax highlighting..."),
    READY(5, "Build ready!", "Your live app is ready to test and inspect.")
}

enum class WorkspaceTab(val label: String) {
    PREVIEW("Live App Preview"),
    CODE("Kotlin Source Code"),
    ARCHITECTURE("Architecture & Schema")
}

data class AttachedFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val size: String = "142 KB",
    val isImage: Boolean = true
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectData(
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val category: String,
    val previewType: String, // "ANDROID_APP", "GOOGLE_DRIVE", "GOOGLE_SHEETS", "GMAIL", "EXPENSE_TRACKER", "GENERIC"
    val code: String,
    val language: ProjectLanguage = ProjectLanguage.ANDROID,
    val createdAt: Long = System.currentTimeMillis()
)

data class SuggestionChipItem(
    val id: String,
    val label: String,
    val category: String,
    val promptTemplate: String
)
