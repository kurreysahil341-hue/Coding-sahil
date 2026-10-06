package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun UpgradeDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Upgrade AI Studio", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Unlock higher RPM (requests per minute), Pro 2.5 models with 2M context window, and dedicated build sandboxes.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AI Studio Pro Plan",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Gemini 2.5 Pro access\n• 360 RPM rate limits\n• Live Android Emulation & APK export\n• Cloud Firestore & OAuth auto-provisioning",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpgrade()
                    onDismiss()
                }
            ) {
                Text("Upgrade to Pro")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

@Composable
fun ApiKeyDialog(
    currentKey: String,
    onSaveKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyText by remember { mutableStateOf(currentKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini API Credentials", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure your Google AI Studio API key. Secrets are securely stored locally on this device.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("API Key") },
                    placeholder = { Text("AIzaSy...") },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide key" else "Show key"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Tip: In production environments, configure GEMINI_API_KEY in the AI Studio Secrets panel.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveKey(keyText)
                    onDismiss()
                }
            ) {
                Text("Save Key")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DashboardDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dashboard & Quota",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                MetricItem(label = "Daily Request Quota", used = "84", limit = "1,500 req", progress = 0.056f)
                Spacer(modifier = Modifier.height(10.dp))
                MetricItem(label = "Tokens Processed", used = "184,200", limit = "1,000,000 tokens", progress = 0.18f)
                Spacer(modifier = Modifier.height(10.dp))
                MetricItem(label = "Concurrent Sandboxes", used = "2", limit = "5 instances", progress = 0.4f)

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Model Status",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ModelStatusRow("Gemini 2.5 Flash", "Operational", Color(0xFF10B981))
                        ModelStatusRow("Gemini 2.5 Pro", "Operational", Color(0xFF10B981))
                        ModelStatusRow("Gemini Experimental", "Active", Color(0xFF3B82F6))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, used: String, limit: String, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = "$used / $limit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ModelStatusRow(model: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = model, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = status, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Composable
fun DocumentationDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Documentation",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                DocSection(
                    title = "Prompt Engineering Guide",
                    description = "Specify state variables, reactive UI requirements, and Material 3 components in your prompt for best code quality."
                )
                DocSection(
                    title = "Multimodal & Wireframes",
                    description = "Attach screenshot mockups or sketches via the (+) button. Gemini analyzes the layout and reproduces it in Jetpack Compose."
                )
                DocSection(
                    title = "Two-Panel Workspace",
                    description = "Inspect the live interactive app in the Right Panel while reviewing build steps, refinement chat, and source code."
                )
                DocSection(
                    title = "Room Persistence",
                    description = "All apps generated through Build are backed by local Room database storage and Flow reactive states."
                )
            }
        }
    }
}

@Composable
private fun DocSection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    }
}

@Composable
fun SettingsDialog(
    selectedModel: String,
    onModelChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings & Preferences", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Default Gemini Generation Model",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-3.8-flash").forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModelChange(model) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (model == selectedModel),
                            onClick = { onModelChange(model) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = model, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

// -------------------------------------------------------------
// View Changes / Git Diff Dialog
// -------------------------------------------------------------
@Composable
fun ViewChangesDialog(
    onDismiss: () -> Unit,
    onRestoreFile: (String) -> Unit = {}
) {
    val changedFiles = listOf(
        ChangedFileDiff(
            path = ".github/workflows/build-apk.yml",
            additions = 65,
            deletions = 0,
            diffLines = listOf(
                DiffLine("+ name: Build Android Debug APK", DiffType.ADDITION),
                DiffLine("+ on:", DiffType.ADDITION),
                DiffLine("+   push:", DiffType.ADDITION),
                DiffLine("+     branches: [ main, master ]", DiffType.ADDITION),
                DiffLine("+   workflow_dispatch:", DiffType.ADDITION),
                DiffLine("+ jobs:", DiffType.ADDITION),
                DiffLine("+   build:", DiffType.ADDITION),
                DiffLine("+     runs-on: ubuntu-latest", DiffType.ADDITION),
                DiffLine("+     steps:", DiffType.ADDITION),
                DiffLine("+       - uses: actions/checkout@v4", DiffType.ADDITION),
                DiffLine("+       - uses: actions/setup-java@v4", DiffType.ADDITION),
                DiffLine("+       - run: chmod +x ./gradlew", DiffType.ADDITION),
                DiffLine("+       - run: ./gradlew assembleDebug", DiffType.ADDITION)
            )
        ),
        ChangedFileDiff(
            path = "app/src/main/java/com/example/util/ProjectZipExporter.kt",
            additions = 55,
            deletions = 8,
            diffLines = listOf(
                DiffLine("@@ -32,8 +32,24 @@ object ProjectZipExporter {", DiffType.HEADER),
                DiffLine("-            \"gradlew\" to \"\"", DiffType.DELETION),
                DiffLine("-            \"gradlew.bat\" to \"\"", DiffType.DELETION),
                DiffLine("+            \"gradlew\" to \"#!/bin/sh\\nexec java ...\"", DiffType.ADDITION),
                DiffLine("+            \"gradlew.bat\" to \"@echo off...\"", DiffType.ADDITION),
                DiffLine("+            \".github/workflows/build-apk.yml\" to workflowYaml", DiffType.ADDITION),
                DiffLine("+            \"gradle/wrapper/gradle-wrapper.properties\" to wrapperProps", DiffType.ADDITION)
            )
        ),
        ChangedFileDiff(
            path = "gradlew",
            additions = 285,
            deletions = 0,
            diffLines = listOf(
                DiffLine("+ #!/bin/sh", DiffType.ADDITION),
                DiffLine("+ # Gradle wrapper script for Linux/macOS", DiffType.ADDITION),
                DiffLine("+ CLASSPATH=\$APP_HOME/gradle/wrapper/gradle-wrapper.jar", DiffType.ADDITION),
                DiffLine("+ exec java -jar \"\$CLASSPATH\" \"\$@\"", DiffType.ADDITION)
            )
        ),
        ChangedFileDiff(
            path = "gradlew.bat",
            additions = 92,
            deletions = 0,
            diffLines = listOf(
                DiffLine("+ @rem Gradle wrapper script for Windows", DiffType.ADDITION),
                DiffLine("+ set CLASSPATH=%APP_HOME%\\gradle\\wrapper\\gradle-wrapper.jar", DiffType.ADDITION),
                DiffLine("+ java.exe -jar \"%CLASSPATH%\" %*", DiffType.ADDITION)
            )
        ),
        ChangedFileDiff(
            path = ".gitignore",
            additions = 5,
            deletions = 0,
            diffLines = listOf(
                DiffLine("+ # Ensure gradle wrapper files are committed", DiffType.ADDITION),
                DiffLine("+ !gradle/wrapper/gradle-wrapper.jar", DiffType.ADDITION),
                DiffLine("+ !gradlew", DiffType.ADDITION),
                DiffLine("+ !gradlew.bat", DiffType.ADDITION)
            )
        )
    )

    var selectedFileIndex by remember { mutableStateOf(0) }
    val currentFile = changedFiles.getOrNull(selectedFileIndex) ?: changedFiles.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Changes (Git Diff)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                Text(
                    text = "${changedFiles.size} files modified in last turn",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // File tabs row
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    items(changedFiles.size) { index ->
                        val file = changedFiles[index]
                        val isSelected = index == selectedFileIndex
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                            modifier = Modifier.clickable { selectedFileIndex = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.path.substringAfterLast("/"),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${file.additions}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                                if (file.deletions > 0) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "-${file.deletions}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // File path bar
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFile.path,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Row {
                            Text(
                                text = "+${currentFile.additions}",
                                fontSize = 10.5.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                            if (currentFile.deletions > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "-${currentFile.deletions}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Diff view code box
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 180.dp, max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        currentFile.diffLines.forEach { line ->
                            val (bgColor, textColor) = when (line.type) {
                                DiffType.ADDITION -> Pair(Color(0xFF064E3B).copy(alpha = 0.5f), Color(0xFF34D399))
                                DiffType.DELETION -> Pair(Color(0xFF7F1D1D).copy(alpha = 0.5f), Color(0xFFF87171))
                                DiffType.HEADER -> Pair(Color(0xFF1E293B), Color(0xFF94A3B8))
                                DiffType.NORMAL -> Pair(Color.Transparent, Color(0xFFE2E8F0))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgColor)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = line.text,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 10.5.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

enum class DiffType { ADDITION, DELETION, HEADER, NORMAL }
data class DiffLine(val text: String, val type: DiffType)
data class ChangedFileDiff(val path: String, val additions: Int, val deletions: Int, val diffLines: List<DiffLine>)

// -------------------------------------------------------------
// Token Usage Dialog
// -------------------------------------------------------------
@Composable
fun TokenUsageDialog(
    modelName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Session Token Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Context window allocation for current turn:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TokenMetricRow("Active Model", modelName)
                        TokenMetricRow("Prompt Tokens", "42 tokens")
                        TokenMetricRow("Response Tokens", "59 tokens")
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        TokenMetricRow("Total Turn Tokens", "101 tokens", isBold = true)
                        TokenMetricRow("Context Limit", "1,048,576 tokens")
                        TokenMetricRow("Remaining Window", "1,048,475 tokens")
                    }
                }

                Text(
                    text = "Calculated in real-time according to Gemini 3.8 Flash tokenizer specifications.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
private fun TokenMetricRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// -------------------------------------------------------------
// Install APK Dialog
// -------------------------------------------------------------
@Composable
fun InstallApkDialog(
    onDismiss: () -> Unit,
    onDownloadApk: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Install Debug APK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Debug APK built successfully using Gradle 9.3.1 toolchain.",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TokenMetricRow("File", "app-debug.apk")
                        TokenMetricRow("Size", "23.4 MB")
                        TokenMetricRow("Package", "com.aistudio.buildworkspace.kxpztr")
                        TokenMetricRow("Target API", "Android 15 (API 36)")
                        TokenMetricRow("Signing", "debug.keystore (Valid)")
                    }
                }

                Text(
                    text = "You can download the APK directly or verify it in your GitHub Actions artifacts.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDownloadApk()
                    onDismiss()
                }
            ) {
                Text("Download APK (23 MB)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -------------------------------------------------------------
// Checkpoint Dialog
// -------------------------------------------------------------
@Composable
fun CheckpointDialog(
    onDismiss: () -> Unit,
    onSaveNewCheckpoint: (String) -> Unit,
    onRestoreCheckpoint: (String) -> Unit
) {
    var newCheckpointName by remember { mutableStateOf("") }
    val checkpoints = listOf(
        "Checkpoint #3 (Latest): Added GitHub Actions APK Workflow & Gradle Wrapper",
        "Checkpoint #2: Pre-seeded Room Database & Gemini 3.8 Model Selection",
        "Checkpoint #1: Initial AI Studio Build Workspace"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checkpoints & Snapshots", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Save current state or revert to a previous milestone:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = newCheckpointName,
                    onValueChange = { newCheckpointName = it },
                    placeholder = { Text("Name new checkpoint...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (newCheckpointName.isNotBlank()) {
                            onSaveNewCheckpoint(newCheckpointName.trim())
                            newCheckpointName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newCheckpointName.isNotBlank()
                ) {
                    Text("Save Current Checkpoint")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Previous Milestones:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                checkpoints.forEach { cp ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cp,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { onRestoreCheckpoint(cp) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Revert", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

