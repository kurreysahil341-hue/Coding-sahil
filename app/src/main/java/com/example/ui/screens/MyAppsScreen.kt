package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedAppEntity
import com.example.model.AppProject
import com.example.model.ProjectLanguage
import com.example.model.getLanguageColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppsScreen(
    savedApps: List<SavedAppEntity>,
    onOpenApp: (SavedAppEntity) -> Unit,
    onRemixApp: (SavedAppEntity) -> Unit,
    onDeleteApp: (Long) -> Unit,
    onNewApp: () -> Unit,
    onBack: () -> Unit,
    onToggleSidebar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguageFilter by remember { mutableStateOf("All") }
    var viewMode by remember { mutableStateOf("list") } // "list" or "cards"

    // Convert SavedAppEntity to AppProject for functional logic
    val projectsList = remember(savedApps) {
        savedApps.map { entity ->
            val lang = when (entity.language.uppercase()) {
                "KOTLIN" -> ProjectLanguage.KOTLIN
                "TYPESCRIPT" -> ProjectLanguage.TYPESCRIPT
                "FLUTTER" -> ProjectLanguage.FLUTTER
                "ANDROID" -> ProjectLanguage.ANDROID
                else -> ProjectLanguage.OTHER
            }
            AppProject(
                id = entity.id.toString(),
                title = entity.title,
                description = entity.description.ifEmpty { entity.prompt },
                language = lang,
                lastModified = entity.lastModified,
                iconBadgeLetter = entity.iconBadgeLetter,
                iconType = entity.iconType,
                prompt = entity.prompt,
                codeSnippet = entity.codeSnippet,
                previewType = entity.previewType
            )
        }
    }

    val filteredProjects = projectsList.filter { project ->
        val matchesFilter = when (selectedLanguageFilter) {
            "All" -> true
            "Android" -> project.language == ProjectLanguage.ANDROID
            "Kotlin" -> project.language == ProjectLanguage.KOTLIN
            "Flutter" -> project.language == ProjectLanguage.FLUTTER
            "TypeScript" -> project.language == ProjectLanguage.TYPESCRIPT
            else -> true
        }
        val matchesSearch = project.title.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("My Apps", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${savedApps.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onToggleSidebar) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Switch view mode (List vs Cards)
                    IconButton(onClick = { viewMode = if (viewMode == "list") "cards" else "list" }) {
                        Icon(
                            imageVector = if (viewMode == "list") Icons.Default.GridView else Icons.Default.ViewAgenda,
                            contentDescription = "Toggle View Mode"
                        )
                    }

                    Button(
                        onClick = onNewApp,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New app", fontSize = 12.sp)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Language Chips
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Language Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Android", "Kotlin", "Flutter", "TypeScript").forEach { lang ->
                        FilterChip(
                            selected = (selectedLanguageFilter == lang),
                            onClick = { selectedLanguageFilter = lang },
                            label = { Text(lang, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching apps found" else "No saved apps yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Describe an idea on the home screen to build your first app!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        val originalEntity = savedApps.firstOrNull { it.id.toString() == project.id }
                            ?: SavedAppEntity(
                                id = project.id.toLongOrNull() ?: 0L,
                                title = project.title,
                                prompt = project.prompt,
                                description = project.description,
                                category = project.language.name,
                                previewType = project.previewType,
                                codeSnippet = project.codeSnippet
                            )

                        if (viewMode == "cards") {
                            // User's requested AppProjectCard layout
                            AppProjectCard(
                                project = project,
                                onProjectClick = { onOpenApp(originalEntity) }
                            )
                        } else {
                            // Google AI Studio Authentic Row layout (from user's screenshot)
                            StudioAppListRow(
                                project = project,
                                onProjectClick = { onOpenApp(originalEntity) },
                                onDelete = { onDeleteApp(originalEntity.id) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 1. Google AI Studio Row Component (Faithful to Screenshot)
// -----------------------------------------------------------------
@Composable
fun StudioAppListRow(
    project: AppProject,
    onProjectClick: (String) -> Unit,
    onDelete: () -> Unit = {}
) {
    val themeColor = getLanguageColor(project.language)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProjectClick(project.id) }
            .testTag("app_row_${project.id}"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Squircle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(themeColor, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (project.iconBadgeLetter != null) {
                        Text(
                            text = project.iconBadgeLetter,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    } else {
                        val iconVector = when (project.iconType) {
                            "code" -> Icons.Default.Code
                            "voice" -> Icons.Default.Mic
                            "book" -> Icons.Default.Book
                            else -> Icons.Default.Android
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App Title & Tag & Description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Badge Pill for Language / Framework
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE0F2FE),
                            border = BorderStroke(0.5.dp, Color(0xFFBAE6FD))
                        ) {
                            Text(
                                text = when (project.language) {
                                    ProjectLanguage.ANDROID -> "Android"
                                    ProjectLanguage.KOTLIN -> "Kotlin"
                                    ProjectLanguage.FLUTTER -> "Flutter"
                                    ProjectLanguage.TYPESCRIPT -> "TypeScript"
                                    else -> "Web"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = " • ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )

                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Far right date / last modified label (e.g. "6 d", "7 d", "Sep 28")
                Text(
                    text = project.lastModified,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

// -----------------------------------------------------------------
// 2. User's AppProjectCard Component
// -----------------------------------------------------------------
@Composable
fun AppProjectCard(
    project: AppProject,
    onProjectClick: (String) -> Unit
) {
    val themeColor = getLanguageColor(project.language)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onProjectClick(project.id) },
        colors = CardDefaults.cardColors(
            containerColor = themeColor.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dynamic Language Tag/Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(themeColor, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = project.language.name.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Modified: ${project.lastModified}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.Gray
                )
            }
        }
    }
}
