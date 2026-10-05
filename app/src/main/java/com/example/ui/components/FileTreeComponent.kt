package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val fileExtension: String = ""
)

// Project Directory Tree modeled after the real Android Studio / Google AI Studio workspace
val DefaultProjectFileTree = listOf(
    FileNode(
        name = "app",
        path = "app",
        isDirectory = true,
        children = listOf(
            FileNode(
                name = "src",
                path = "app/src",
                isDirectory = true,
                children = listOf(
                    FileNode(
                        name = "androidTest",
                        path = "app/src/androidTest",
                        isDirectory = true,
                        children = emptyList()
                    ),
                    FileNode(
                        name = "main",
                        path = "app/src/main",
                        isDirectory = true,
                        children = listOf(
                            FileNode(
                                name = "java",
                                path = "app/src/main/java",
                                isDirectory = true,
                                children = listOf(
                                    FileNode(
                                        name = "com",
                                        path = "app/src/main/java/com",
                                        isDirectory = true,
                                        children = listOf(
                                            FileNode(
                                                name = "example",
                                                path = "app/src/main/java/com/example",
                                                isDirectory = true,
                                                children = listOf(
                                                    FileNode(name = "data", path = "app/src/main/java/com/example/data", isDirectory = true),
                                                    FileNode(name = "model", path = "app/src/main/java/com/example/model", isDirectory = true),
                                                    FileNode(name = "ui", path = "app/src/main/java/com/example/ui", isDirectory = true),
                                                    FileNode(name = "viewmodel", path = "app/src/main/java/com/example/viewmodel", isDirectory = true),
                                                    FileNode(name = "MainActivity.kt", path = "app/src/main/java/com/example/MainActivity.kt", isDirectory = false, fileExtension = "kt")
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            FileNode(name = "res", path = "app/src/main/res", isDirectory = true),
                            FileNode(name = "AndroidManifest.xml", path = "app/src/main/AndroidManifest.xml", isDirectory = false, fileExtension = "xml")
                        )
                    ),
                    FileNode(name = "test", path = "app/src/test", isDirectory = true)
                )
            ),
            FileNode(name = ".gitignore", path = "app/.gitignore", isDirectory = false, fileExtension = "gitignore"),
            FileNode(name = "build.gradle.kts", path = "app/build.gradle.kts", isDirectory = false, fileExtension = "kts"),
            FileNode(name = "proguard-rules.pro", path = "app/proguard-rules.pro", isDirectory = false, fileExtension = "pro")
        )
    ),
    FileNode(name = "gradle", path = "gradle", isDirectory = true),
    FileNode(name = "src", path = "src", isDirectory = true),
    FileNode(name = ".env.example", path = ".env.example", isDirectory = false, fileExtension = "env"),
    FileNode(name = ".gitignore", path = ".gitignore", isDirectory = false, fileExtension = "gitignore"),
    FileNode(name = "build.gradle.kts", path = "build.gradle.kts", isDirectory = false, fileExtension = "kts"),
    FileNode(name = "gradle.properties", path = "gradle.properties", isDirectory = false, fileExtension = "properties"),
    FileNode(name = "metadata.json", path = "metadata.json", isDirectory = false, fileExtension = "json"),
    FileNode(name = "settings.gradle.kts", path = "settings.gradle.kts", isDirectory = false, fileExtension = "kts")
)

private fun simpleName(name: String) = emptyList<FileNode>()

@Composable
fun FileTreeComponent(
    selectedFilePath: String,
    onSelectFile: (String, String) -> Unit,
    onCloseTree: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    // Map of expanded directory paths
    val expandedPaths = remember {
        mutableStateMapOf(
            "app" to true,
            "app/src" to true,
            "app/src/main" to true,
            "app/src/main/java" to true,
            "app/src/main/java/com" to true,
            "app/src/main/java/com/example" to true
        )
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(220.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: "File explorer" + Search, Add, Close icons (matches screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "File explo...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showSearchField = !showSearchField },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search files",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* Add file */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add file",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onCloseTree,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close file tree",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (showSearchField) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter files...", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Scrollable Tree Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                DefaultProjectFileTree.forEach { node ->
                    FileTreeNodeItem(
                        node = node,
                        depth = 0,
                        expandedPaths = expandedPaths,
                        selectedFilePath = selectedFilePath,
                        searchQuery = searchQuery,
                        onToggleDirectory = { path ->
                            expandedPaths[path] = !(expandedPaths[path] ?: false)
                        },
                        onSelectFile = onSelectFile
                    )
                }
            }
        }
    }
}

@Composable
fun FileTreeNodeItem(
    node: FileNode,
    depth: Int,
    expandedPaths: Map<String, Boolean>,
    selectedFilePath: String,
    searchQuery: String,
    onToggleDirectory: (String) -> Unit,
    onSelectFile: (String, String) -> Unit
) {
    val isExpanded = expandedPaths[node.path] ?: false
    val isSelected = selectedFilePath == node.path || selectedFilePath.endsWith(node.name)

    // Filter check
    val matchesSearch = searchQuery.isEmpty() || node.name.contains(searchQuery, ignoreCase = true)

    if (matchesSearch || node.isDirectory) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else Color.Transparent
                )
                .clickable {
                    if (node.isDirectory) {
                        onToggleDirectory(node.path)
                    } else {
                        onSelectFile(node.path, node.name)
                    }
                }
                .padding(start = (depth * 12 + 6).dp, top = 3.dp, bottom = 3.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder arrow / icon
            if (node.isDirectory) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(15.dp)
                )
            } else {
                val fileColor = when (node.fileExtension) {
                    "kt" -> Color(0xFF7F52FF)
                    "xml" -> Color(0xFF0F9D58)
                    "json" -> Color(0xFFEAB308)
                    "kts" -> Color(0xFF3B82F6)
                    else -> Color(0xFF64748B)
                }
                Icon(
                    imageVector = when (node.fileExtension) {
                        "kt", "kts", "xml" -> Icons.Default.Code
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = fileColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = node.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!node.isDirectory && isSelected) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    if (node.isDirectory && isExpanded) {
        node.children.forEach { child ->
            FileTreeNodeItem(
                node = child,
                depth = depth + 1,
                expandedPaths = expandedPaths,
                selectedFilePath = selectedFilePath,
                searchQuery = searchQuery,
                onToggleDirectory = onToggleDirectory,
                onSelectFile = onSelectFile
            )
        }
    }
}
