package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectData

data class GalleryTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val previewType: String,
    val icon: ImageVector,
    val iconColor: Color,
    val prompt: String
)

val GalleryTemplatesList = listOf(
    GalleryTemplate(
        id = "android_suite",
        title = "Android Task & Sprint Board",
        description = "Material 3 task board with Room database persistence, priority flags, and completion metrics.",
        category = "Android App",
        previewType = "ANDROID_APP",
        icon = Icons.Default.Android,
        iconColor = Color(0xFF3DDC84),
        prompt = "Build an Android app with Room database, Material 3 cards, task status toggling, and priority tags"
    ),
    GalleryTemplate(
        id = "drive_sync",
        title = "Cloud Drive Sync Manager",
        description = "Storage breakdown meter, folder navigator, and cloud file upload queue.",
        category = "Integration",
        previewType = "GOOGLE_DRIVE",
        icon = Icons.Default.Cloud,
        iconColor = Color(0xFF4285F4),
        prompt = "Build a responsive Google Drive file explorer with storage metrics, upload progress, and document preview"
    ),
    GalleryTemplate(
        id = "sheets_tracker",
        title = "Sheets Budget & Analytics",
        description = "Dynamic spreadsheet grid with real-time formula computation, row adding, and categorized totals.",
        category = "Productivity",
        previewType = "GOOGLE_SHEETS",
        icon = Icons.Default.TableChart,
        iconColor = Color(0xFF0F9D58),
        prompt = "Build a Google Sheets finance tracker with expense categorization and real-time calculation"
    ),
    GalleryTemplate(
        id = "gmail_smart",
        title = "Gmail Smart Inbox & Filter",
        description = "Intelligent email inbox with priority star tags, category tabs, and quick response drafts.",
        category = "Communication",
        previewType = "GMAIL",
        icon = Icons.Default.Email,
        iconColor = Color(0xFFEA4335),
        prompt = "Build a smart Gmail triage inbox with priority labels, quick reply drafts, and thread view"
    ),
    GalleryTemplate(
        id = "recipe_planner",
        title = "Recipe & Grocery Planner",
        description = "Smart ingredient checklist with nutrition breakdown and grocery store layout sort.",
        category = "Lifestyle",
        previewType = "ANDROID_APP",
        icon = Icons.Default.Restaurant,
        iconColor = Color(0xFFEC4899),
        prompt = "Build a recipe companion app with nutrition calculator and auto-generated grocery shopping lists"
    ),
    GalleryTemplate(
        id = "ecommerce_store",
        title = "E-Commerce Catalog & Cart",
        description = "Product showcase with multi-variant options, price calculations, and instant checkout preview.",
        category = "Commerce",
        previewType = "ANDROID_APP",
        icon = Icons.Default.ShoppingCart,
        iconColor = Color(0xFF8B5CF6),
        prompt = "Build a modern e-commerce product catalog with shopping cart, search filter, and checkout flow"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onRemixTemplate: (GalleryTemplate) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Gallery & Templates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Discover, remix, and build full-featured apps powered by Gemini models.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(GalleryTemplatesList, key = { it.id }) { template ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = template.iconColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = template.icon,
                                        contentDescription = null,
                                        tint = template.iconColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = template.category,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onRemixTemplate(template) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("remix_${template.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remix & Build", fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
