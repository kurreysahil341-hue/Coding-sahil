package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GeminiSparkle(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_sparkle")
    val pulse by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sparkle_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * pulse)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Aura glow behind the sparkle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x334285F4),
                        Color(0x228B5CF6),
                        Color(0x00EC4899)
                    ),
                    center = Offset(cx, cy),
                    radius = w * 0.75f
                ),
                radius = w * 0.65f,
                center = Offset(cx, cy)
            )

            // Distinctive four-point star path (concave curves)
            val path = Path().apply {
                moveTo(cx, 0f)
                // Curve to right point
                cubicTo(
                    cx + w * 0.12f, cy - h * 0.12f,
                    cx + w * 0.12f, cy - h * 0.12f,
                    w, cy
                )
                // Curve to bottom point
                cubicTo(
                    cx + w * 0.12f, cy + h * 0.12f,
                    cx + w * 0.12f, cy + h * 0.12f,
                    cx, h
                )
                // Curve to left point
                cubicTo(
                    cx - w * 0.12f, cy + h * 0.12f,
                    cx - w * 0.12f, cy + h * 0.12f,
                    0f, cy
                )
                // Curve back to top point
                cubicTo(
                    cx - w * 0.12f, cy - h * 0.12f,
                    cx - w * 0.12f, cy - h * 0.12f,
                    cx, 0f
                )
                close()
            }

            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4285F4), // Blue
                        Color(0xFF8B5CF6), // Violet
                        Color(0xFFEC4899), // Pink
                        Color(0xFF38BDF8)  // Cyan
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )
        }
    }
}
