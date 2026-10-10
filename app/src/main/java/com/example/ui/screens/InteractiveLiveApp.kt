package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectData

@Composable
fun InteractiveLiveApp(
    project: ProjectData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val detected = com.example.util.AppCodeSynthesizer.detectPreviewType(project.prompt.ifEmpty { project.title })
        val effectiveType = if (project.previewType in listOf("GOOGLE_DRIVE", "GOOGLE_SHEETS", "GMAIL")) {
            project.previewType
        } else {
            detected
        }

        when (effectiveType) {
            "LOGIN" -> InteractiveLoginSandbox(appTitle = project.title)
            "CALCULATOR" -> InteractiveCalculatorSandbox(appTitle = project.title)
            "WEATHER" -> InteractiveWeatherSandbox(appTitle = project.title)
            "COUNTER" -> InteractiveCounterSandbox(appTitle = project.title)
            "GOOGLE_DRIVE" -> InteractiveDriveApp()
            "GOOGLE_SHEETS" -> InteractiveSheetsApp()
            "GMAIL" -> InteractiveGmailApp()
            else -> InteractiveTaskApp(appTitle = project.title)
        }
    }
}

// -------------------------------------------------------------
// 1. Interactive Android Task App
// -------------------------------------------------------------
data class LiveTask(val id: Long, val title: String, val category: String, val isDone: Boolean = false)

@Composable
fun InteractiveTaskApp(appTitle: String) {
    val tasks = remember {
        mutableStateListOf(
            LiveTask(1, "Configure Room Database with KSP", "Database", true),
            LiveTask(2, "Design Jetpack Compose Two-Panel UI", "UI/UX", true),
            LiveTask(3, "Integrate Gemini 2.5 Flash streaming", "AI API", false),
            LiveTask(4, "Add dark theme and dynamic color tokens", "Design", false),
            LiveTask(5, "Write unit & Robolectric test coverage", "Testing", false)
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTasks = tasks.filter {
        val matchesFilter = when (selectedFilter) {
            "Active" -> !it.isDone
            "Completed" -> it.isDone
            else -> true
        }
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    val completedCount = tasks.count { it.isDone }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // App Bar inside sandbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = appTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$completedCount of ${tasks.size} tasks done (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("app_add_task_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF10B981)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search & Filter Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search items...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Active", "Completed").forEach { filter ->
                FilterChip(
                    selected = (selectedFilter == filter),
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) }
                )
            }
        }

        // Tasks List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredTasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isDone,
                            onCheckedChange = { checked ->
                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index != -1) {
                                    tasks[index] = task.copy(isDone = checked)
                                }
                            }
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { tasks.removeAll { it.id == task.id } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Item") },
            text = {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("Task description") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            tasks.add(
                                LiveTask(
                                    id = System.currentTimeMillis(),
                                    title = newTaskTitle.trim(),
                                    category = "Feature",
                                    isDone = false
                                )
                            )
                            newTaskTitle = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 2. Interactive Google Drive Explorer
// -------------------------------------------------------------
data class LiveDriveFile(val id: String, val name: String, val size: String, val date: String, val isFolder: Boolean = false)

@Composable
fun InteractiveDriveApp() {
    val files = remember {
        mutableStateListOf(
            LiveDriveFile("1", "Architecture_Blueprint.pdf", "2.4 MB", "Today", false),
            LiveDriveFile("2", "Component_Mockups.fig", "18.1 MB", "Yesterday", false),
            LiveDriveFile("3", "Project Source Code", "4 items", "Sep 28", true),
            LiveDriveFile("4", "Release_Notes_v1.0.md", "14 KB", "Sep 25", false),
            LiveDriveFile("5", "Assets & Icons", "12 items", "Sep 20", true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, tint = Color(0xFF4285F4))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Drive Sync", fontWeight = FontWeight.Bold)
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable {
                    files.add(0, LiveDriveFile(System.currentTimeMillis().toString(), "New_Upload_${files.size + 1}.png", "1.2 MB", "Just now", false))
                }
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Storage indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Storage Usage", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("42.5 GB / 100 GB", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.425f },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4285F4)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(files) { file ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isFolder) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (file.isFolder) Color(0xFFF59E0B) else Color(0xFF4285F4),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(text = "${file.size} • ${file.date}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Interactive Google Sheets Spreadsheet
// -------------------------------------------------------------
data class LiveSheetRow(val id: Int, val item: String, val category: String, val cost: Double)

@Composable
fun InteractiveSheetsApp() {
    val rows = remember {
        mutableStateListOf(
            LiveSheetRow(1, "Gemini Cloud API", "Inference", 145.0),
            LiveSheetRow(2, "Firebase Database", "Storage", 28.5),
            LiveSheetRow(3, "Domain & CDN", "Network", 15.0),
            LiveSheetRow(4, "Android App Licenses", "Dev Tools", 25.0)
        )
    }

    val total = rows.sumOf { it.cost }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF0F9D58))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Sheets Calculator", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    rows.add(LiveSheetRow(rows.size + 1, "Cloud Worker #${rows.size + 1}", "Service", 35.0))
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Row", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Spreadsheet Header
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(10.dp)) {
                Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Item Name", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Category", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Cost ($)", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rows) { row ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${row.id}", modifier = Modifier.width(30.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = row.item, modifier = Modifier.weight(1.5f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = row.category, modifier = Modifier.weight(1f), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "$${row.cost}", modifier = Modifier.width(70.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Total Row
        Surface(
            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SUM TOTAL (Formula =SUM(D:D))", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("$$total", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F9D58))
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Interactive Gmail Triage App
// -------------------------------------------------------------
data class LiveEmail(val id: String, val sender: String, val subject: String, val snippet: String, val time: String, val isStarred: Boolean = false)

@Composable
fun InteractiveGmailApp() {
    val emails = remember {
        mutableStateListOf(
            LiveEmail("1", "Google AI Studio", "Your Gemini 2.5 Flash model is active", "Welcome to Google AI Studio Build. Your prompt workspace is live...", "10:14 AM", true),
            LiveEmail("2", "Android Developers", "Jetpack Compose 1.8 release notes", "New performance enhancements in lazy layouts and adaptive windows...", "9:30 AM", false),
            LiveEmail("3", "Firebase Team", "Firestore database rules deployed", "Security rules check completed with 0 errors...", "Yesterday", false)
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color(0xFFEA4335))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gmail Smart Inbox", fontWeight = FontWeight.Bold)
            }

            Text("${emails.size} messages", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(emails) { email ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.Top) {
                        IconButton(
                            onClick = {
                                val idx = emails.indexOfFirst { it.id == email.id }
                                if (idx != -1) emails[idx] = email.copy(isStarred = !email.isStarred)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (email.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (email.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(email.sender, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(email.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(email.subject, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(email.snippet, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Login Sandbox
// -------------------------------------------------------------
@Composable
fun InteractiveLoginSandbox(appTitle: String) {
    var email by remember { mutableStateOf("sahil@example.com") }
    var password by remember { mutableStateOf("••••••••") }
    var isLoggedIn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoggedIn) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✓ Authenticated", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { isLoggedIn = false }) {
                        Text("Sign Out")
                    }
                }
            }
        } else {
            Text(
                text = appTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Live Interactive Authentication Sandbox",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { isLoggedIn = true },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Sign In (Interactive)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Calculator Sandbox
// -------------------------------------------------------------
@Composable
fun InteractiveCalculatorSandbox(appTitle: String) {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }

    fun onNumber(n: String) {
        display = if (display == "0") n else display + n
    }

    fun onOp(op: String) {
        operand1 = display.toDoubleOrNull()
        pendingOp = op
        display = "0"
    }

    fun onEqual() {
        val op1 = operand1 ?: return
        val op2 = display.toDoubleOrNull() ?: return
        val res = when (pendingOp) {
            "+" -> op1 + op2
            "-" -> op1 - op2
            "×" -> op1 * op2
            "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
            else -> op2
        }
        display = if (res.isNaN()) "Error" else if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
        operand1 = null
        pendingOp = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(
                text = display,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val buttons = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { btn ->
                    val weight = if (btn == "0") 2f else 1f
                    Button(
                        onClick = {
                            when (btn) {
                                "C" -> { display = "0"; operand1 = null; pendingOp = null }
                                "+", "-", "×", "÷" -> onOp(btn)
                                "=" -> onEqual()
                                else -> onNumber(btn)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(weight).height(48.dp),
                        colors = if (btn in listOf("+", "-", "×", "÷", "=")) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        }
                    ) {
                        Text(btn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Weather Sandbox
// -------------------------------------------------------------
@Composable
fun InteractiveWeatherSandbox(appTitle: String) {
    var city by remember { mutableStateOf("New Delhi") }
    var temperature by remember { mutableIntStateOf(29) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(city, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Sunny • Real-time Sandbox", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${temperature}°C", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                Text("Feels like ${temperature + 2}°C", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { temperature-- }) { Text("-1°C") }
            Button(onClick = { temperature++ }) { Text("+1°C") }
        }
    }
}

// -------------------------------------------------------------
// Interactive Counter Sandbox
// -------------------------------------------------------------
@Composable
fun InteractiveCounterSandbox(appTitle: String) {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(appTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(130.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { count-- }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrement")
            }
            OutlinedButton(onClick = { count = 0 }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
            }
            Button(onClick = { count++ }) {
                Icon(Icons.Default.Add, contentDescription = "Increment")
            }
        }
    }
}

