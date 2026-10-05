package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SuggestionChipItem

val SampleSuggestionChips = listOf(
    SuggestionChipItem(
        id = "android",
        label = "Build an Android app",
        category = "android",
        promptTemplate = "Build an Android app with Room local database, clean Material 3 UI, and offline task tracking"
    ),
    SuggestionChipItem(
        id = "drive",
        label = "Google Drive",
        category = "drive",
        promptTemplate = "Build a responsive Google Drive file sync dashboard with cloud folder hierarchy and preview"
    ),
    SuggestionChipItem(
        id = "sheets",
        label = "Google Sheets",
        category = "sheets",
        promptTemplate = "Create a Google Sheets financial spreadsheet with live calculated rows and category filtering"
    ),
    SuggestionChipItem(
        id = "gmail",
        label = "Gmail",
        category = "gmail",
        promptTemplate = "Build a smart Gmail triage inbox with priority labels, quick reply drafts, and thread view"
    ),
    SuggestionChipItem(
        id = "firebase",
        label = "Firebase Auth & Sync",
        category = "firebase",
        promptTemplate = "Build an app integrated with Firebase Authentication and cloud real-time data sync"
    ),
    SuggestionChipItem(
        id = "ecommerce",
        label = "E-Commerce Catalog",
        category = "store",
        promptTemplate = "Build a modern e-commerce product catalog with shopping cart, search filter, and checkout flow"
    )
)

@Composable
fun QuickSuggestionChips(
    chips: List<SuggestionChipItem> = SampleSuggestionChips,
    onChipClick: (SuggestionChipItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEach { chip ->
            SuggestionChip(
                item = chip,
                onClick = { onChipClick(chip) },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("suggestion_chip_${chip.id}")
            )
        }

        // Chevron icon at the end to indicate more items
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
                .size(32.dp)
                .clickable { /* scroll */ }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "More suggestions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    item: SuggestionChipItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = getChipIconAndColor(item.category)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getChipIconAndColor(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "android" -> Pair(Icons.Default.Android, Color(0xFF3DDC84))
        "drive" -> Pair(Icons.Default.CloudQueue, Color(0xFF4285F4))
        "sheets" -> Pair(Icons.Default.TableChart, Color(0xFF0F9D58))
        "gmail" -> Pair(Icons.Default.Email, Color(0xFFEA4335))
        "firebase" -> Pair(Icons.Default.Wifi, Color(0xFFFFCA28))
        else -> Pair(Icons.Default.ShoppingCart, Color(0xFF8B5CF6))
    }
}
