package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Authentic Google AI Studio palette
val GoogleBlue = Color(0xFF1A73E8)
val GoogleBlueLight = Color(0xFF8AB4F8)
val GoogleBlueDark = Color(0xFF1557B0)

val GeminiPurple = Color(0xFF7C3AED)
val GeminiPurpleLight = Color(0xFFA855F7)

val GeminiCyan = Color(0xFF0EA5E9)
val GeminiCyanLight = Color(0xFF38BDF8)

val GeminiRose = Color(0xFFEC4899)
val GeminiAmber = Color(0xFFF59E0B)

val GreenSuccess = Color(0xFF10B981)
val AndroidGreen = Color(0xFF3DDC84)

// Neutral Light Theme
val StudioLightBg = Color(0xFFFFFFFF)
val StudioLightSurface = Color(0xFFFFFFFF)
val StudioLightSidebar = Color(0xFFF9FAFB)
val StudioLightBorder = Color(0xFFE5E7EB)
val StudioLightTextPrimary = Color(0xFF111827)
val StudioLightTextSecondary = Color(0xFF4B5563)
val StudioLightTextMuted = Color(0xFF9CA3AF)

// Neutral Dark Theme
val StudioDarkBg = Color(0xFF111318)
val StudioDarkSurface = Color(0xFF181B20)
val StudioDarkSidebar = Color(0xFF13151A)
val StudioDarkBorder = Color(0xFF282D36)
val StudioDarkTextPrimary = Color(0xFFF3F4F6)
val StudioDarkTextSecondary = Color(0xFF9CA3AF)
val StudioDarkTextMuted = Color(0xFF6B7280)

// Gemini Multi-color Gradient Brush
val GeminiGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4285F4), // Blue
        Color(0xFF9333EA), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4)  // Cyan
    )
)

val GeminiAuraBrush = Brush.sweepGradient(
    colors = listOf(
        Color(0xFF4285F4),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF06B6D4),
        Color(0xFF4285F4)
    )
)
