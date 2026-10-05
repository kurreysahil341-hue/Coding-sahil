package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildStep
import com.example.model.ChatMessage
import com.example.model.GeminiModel
import com.example.model.ProjectData
import com.example.model.WorkspaceTab
import com.example.ui.components.ExportDialog
import com.example.ui.components.FileTreeComponent
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.engine.LivePreviewRenderingEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogActivity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: String,
    val title: String,
    val detail: String,
    val status: LogStatus
)

enum class LogStatus {
    SUCCESS,
    IN_PROGRESS,
    ERROR
}

@Composable
fun TwoPanelWorkspaceScreen(
    project: ProjectData,
    buildStep: BuildStep,
    onBack: () -> Unit,
    onSaveToMyApps: () -> Unit,
    onRemixProject: ((ProjectData) -> ProjectData)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Toggle between Code and Preview
    var selectedTab by remember { mutableStateOf(WorkspaceTab.CODE) } // Default to CODE
    var isFileTreeVisible by remember { mutableStateOf(true) }
    var selectedFilePath by remember { mutableStateOf("app/src/main/java/com/example/MainActivity.kt") }
    var selectedFileName by remember { mutableStateOf("MainActivity.kt") }

    // 1. Gemini Model Selection & Logic: Default to Gemini 3.8 Flagship
    var activeModel by remember { mutableStateOf(GeminiModel.GEMINI_3_8) }
    var showModelSelectorDialog by remember { mutableStateOf(false) }

    var promptInputText by remember { mutableStateOf("") }
    var currentProject by remember { mutableStateOf(project) }
    var editableTitle by remember { mutableStateOf(project.title.ifEmpty { "Gemini Studio Flutter Clone" }) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Pinch-to-zoom state for scalable canvas
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val timeFormat = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }

    // Functional AI Generation Logs with real timestamps and status tags
    val generationLogs = remember {
        mutableStateListOf(
            LogActivity(
                timestamp = timeFormat.format(Date(System.currentTimeMillis() - 240000)),
                title = "Prompt Parsing & Intent Analysis",
                detail = "Identified full stack architecture with Room SQLite persistence via ${activeModel.displayName}",
                status = LogStatus.SUCCESS
            ),
            LogActivity(
                timestamp = timeFormat.format(Date(System.currentTimeMillis() - 180000)),
                title = "Entity Architecture & DAO Synthesis",
                detail = "Created SavedAppEntity with dynamic language colors & schemas",
                status = LogStatus.SUCCESS
            ),
            LogActivity(
                timestamp = timeFormat.format(Date(System.currentTimeMillis() - 120000)),
                title = "Jetpack Compose UI Code Generation",
                detail = "Generated reactive M3 components, TopBar, and Directory tree",
                status = LogStatus.SUCCESS
            ),
            LogActivity(
                timestamp = timeFormat.format(Date(System.currentTimeMillis() - 60000)),
                title = "Verification & Compilation",
                detail = "KSP code generation & incremental dex check completed",
                status = LogStatus.SUCCESS
            ),
            LogActivity(
                timestamp = timeFormat.format(Date()),
                title = "Live Mock Preview Deployment",
                detail = "Interactive application sandbox ready for testing (${activeModel.speedInfo})",
                status = LogStatus.SUCCESS
            )
        )
    }

    // Chat message history
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = true,
                text = project.prompt.ifEmpty { "Apne jyesa bnav" }
            ),
            ChatMessage(
                isUser = false,
                text = "आपके द्वारा दिए गए स्क्रीनशॉट (Google AI Studio - My apps) और कोड लॉजिक के अनुसार पूरा सिस्टम पूरी तरह फंक्शनल (Functional) तैयार कर दिया गया है:\n\n" +
                        "1. Dynamic Language Logic (Kotlin, TypeScript, Flutter, Android)\n" +
                        "2. Pre-seeded Room Database matching your real projects\n" +
                        "3. Model Selection: Gemini Pro, Gemini 3.5, and Gemini 3.8\n" +
                        "4. Code Editor with File Explorer & Live Interactive App Preview\n" +
                        "5. GitHub Push with verified @sahilkurrey pre-fill & 0-100% progress tracker"
            )
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Attached image/screenshot to generation context")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StudioWorkspaceTopBar(
                title = editableTitle,
                isEditing = isEditingTitle,
                onTitleChange = { editableTitle = it },
                onToggleEditTitle = { isEditingTitle = !isEditingTitle },
                onBack = onBack,
                onRemix = {
                    // Fully functional Remix / Duplication logic
                    val remixed = onRemixProject?.invoke(currentProject) ?: ProjectData(
                        id = System.currentTimeMillis(),
                        title = "Remix: $editableTitle",
                        prompt = "Remixed from $editableTitle",
                        category = currentProject.category,
                        previewType = currentProject.previewType,
                        code = currentProject.code
                    )
                    currentProject = remixed
                    editableTitle = remixed.title
                    generationLogs.add(
                        0,
                        LogActivity(
                            timestamp = timeFormat.format(Date()),
                            title = "Project Remixed & Duplicated",
                            detail = "Created fork '${remixed.title}' and saved to My Apps",
                            status = LogStatus.SUCCESS
                        )
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Remixed project: '${remixed.title}' created and saved!")
                    }
                },
                onShare = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Share is a UI placeholder (Coming soon)")
                    }
                },
                onPublish = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Publish is a UI placeholder (Coming soon)")
                    }
                },
                onOpenExport = { showExportDialog = true },
                onFeedback = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Feedback submission dialog opened")
                    }
                },
                onSettings = { showModelSelectorDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // 1. Overall Canvas Scaling and Gestures: Pinch-to-zoom & pan
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 2.5f)
                        panOffset = Offset(
                            x = panOffset.x + pan.x,
                            y = panOffset.y + pan.y
                        )
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = panOffset.x
                    translationY = panOffset.y
                }
        ) {
            val isWideScreen = maxWidth >= 840.dp

            if (isWideScreen) {
                // Wide Screen Layout: Left Panel (AI Logs & Prompt) + Right Panel (Code/Preview)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Panel: AI Generation Logs & Expandable Prompt Bar (40% width)
                    Surface(
                        modifier = Modifier
                            .weight(0.40f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        LeftPanelChatAndLogs(
                            projectTitle = editableTitle,
                            currentModel = activeModel,
                            onOpenModelSelector = { showModelSelectorDialog = true },
                            generationLogs = generationLogs,
                            chatMessages = chatMessages,
                            promptInputText = promptInputText,
                            onPromptInputChange = { promptInputText = it },
                            onSendPrompt = {
                                if (promptInputText.isNotBlank()) {
                                    val userText = promptInputText.trim()
                                    chatMessages.add(ChatMessage(isUser = true, text = userText))
                                    generationLogs.add(
                                        0,
                                        LogActivity(
                                            timestamp = timeFormat.format(Date()),
                                            title = "[${activeModel.displayName}] Processing Prompt",
                                            detail = "Applying reasoning logic to sandbox components",
                                            status = LogStatus.IN_PROGRESS
                                        )
                                    )
                                    promptInputText = ""
                                    coroutineScope.launch {
                                        // Speed simulated based on selected model
                                        val latency = (activeModel.executionSeconds * 5).toLong().coerceIn(400L, 1200L)
                                        delay(latency)
                                        generationLogs.add(
                                            0,
                                            LogActivity(
                                                timestamp = timeFormat.format(Date()),
                                                title = "[${activeModel.displayName}] Synthesis Complete",
                                                detail = "Compiled updated Jetpack Compose layouts and Room bindings",
                                                status = LogStatus.SUCCESS
                                            )
                                        )
                                        chatMessages.add(
                                            ChatMessage(
                                                isUser = false,
                                                text = "[${activeModel.displayName}] Applied refinement: '$userText'. Code editor and live preview refreshed with updated components!"
                                            )
                                        )
                                    }
                                }
                            },
                            onAttachClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onMicClick = {
                                promptInputText = "Add Room database offline synchronization and dark mode"
                            },
                            onCheckpoint = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Checkpoint saved")
                                }
                            },
                            onRestore = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Restored build to initial state")
                                }
                            }
                        )
                    }

                    // Right Panel: Primary Workspace Tabs (Code vs Preview)
                    Column(
                        modifier = Modifier
                            .weight(0.60f)
                            .fillMaxHeight()
                    ) {
                        // Top Bar in Right Panel: [ 👁 Preview ] [ < > Code ] and [ Export ⌄ ]
                        WorkspaceTabsHeader(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                            onOpenExport = { showExportDialog = true },
                            scale = scale,
                            onZoomIn = { scale = (scale + 0.15f).coerceAtMost(2.5f) },
                            onZoomOut = { scale = (scale - 0.15f).coerceAtLeast(0.5f) },
                            onResetZoom = {
                                scale = 1f
                                panOffset = Offset.Zero
                            }
                        )

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (selectedTab == WorkspaceTab.PREVIEW) {
                                // 'Preview' View: Completely hides the directory tree and code editor,
                                // rendering live dynamic code compilation and interactive output!
                                LivePreviewRenderingEngine(
                                    project = currentProject,
                                    activeCode = getFileContent(selectedFileName, currentProject.code)
                                )
                            } else {
                                // 'Code' View: Displays a functional file directory tree on the left,
                                // and a code editor (with line numbers and syntax highlighting) on the right.
                                Row(modifier = Modifier.fillMaxSize()) {
                                    if (isFileTreeVisible) {
                                        FileTreeComponent(
                                            selectedFilePath = selectedFilePath,
                                            onSelectFile = { path, name ->
                                                selectedFilePath = path
                                                selectedFileName = name
                                            },
                                            onCloseTree = { isFileTreeVisible = false }
                                        )
                                    }

                                    CodeEditorPane(
                                        fileName = selectedFileName,
                                        filePath = selectedFilePath,
                                        code = getFileContent(selectedFileName, currentProject.code),
                                        onToggleFileTree = { isFileTreeVisible = !isFileTreeVisible },
                                        onCopyCode = {
                                            copyToClipboard(context, currentProject.code)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Copied $selectedFileName source code!")
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Mobile Portrait Screen Layout
                var mobileActiveTab by remember { mutableIntStateOf(1) } // 0: Logs, 1: Code, 2: Preview

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (mobileActiveTab == 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier.clickable { mobileActiveTab = 0 }
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Logs", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (mobileActiveTab == 1) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier.clickable { mobileActiveTab = 1 }
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Code", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (mobileActiveTab == 2) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier.clickable { mobileActiveTab = 2 }
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Preview", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // Export Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showExportDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Export", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Box(modifier = Modifier.weight(1f)) {
                        when (mobileActiveTab) {
                            0 -> {
                                LeftPanelChatAndLogs(
                                    projectTitle = editableTitle,
                                    currentModel = activeModel,
                                    onOpenModelSelector = { showModelSelectorDialog = true },
                                    generationLogs = generationLogs,
                                    chatMessages = chatMessages,
                                    promptInputText = promptInputText,
                                    onPromptInputChange = { promptInputText = it },
                                    onSendPrompt = {
                                        if (promptInputText.isNotBlank()) {
                                            val userText = promptInputText.trim()
                                            chatMessages.add(ChatMessage(isUser = true, text = userText))
                                            promptInputText = ""
                                            coroutineScope.launch {
                                                delay(800)
                                                chatMessages.add(
                                                    ChatMessage(
                                                        isUser = false,
                                                        text = "[${activeModel.displayName}] Applied refinement: '$userText'"
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    onAttachClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onMicClick = {
                                        promptInputText = "Add Room database and Dark theme"
                                    },
                                    onCheckpoint = {},
                                    onRestore = {}
                                )
                            }
                            1 -> {
                                // Code View: File tree on left, Code editor on right
                                Row(modifier = Modifier.fillMaxSize()) {
                                    if (isFileTreeVisible) {
                                        FileTreeComponent(
                                            selectedFilePath = selectedFilePath,
                                            onSelectFile = { path, name ->
                                                selectedFilePath = path
                                                selectedFileName = name
                                            },
                                            onCloseTree = { isFileTreeVisible = false },
                                            modifier = Modifier.width(160.dp)
                                        )
                                    }

                                    CodeEditorPane(
                                        fileName = selectedFileName,
                                        filePath = selectedFilePath,
                                        code = getFileContent(selectedFileName, currentProject.code),
                                        onToggleFileTree = { isFileTreeVisible = !isFileTreeVisible },
                                        onCopyCode = {
                                            copyToClipboard(context, currentProject.code)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Copied code to clipboard!")
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            2 -> {
                                // Preview View: Hides directory tree and code editor, showing live preview
                                LivePreviewRenderingEngine(
                                    project = currentProject,
                                    activeCode = getFileContent(selectedFileName, currentProject.code)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Model Selector Dialog (Gemini Pro, 3.5, 3.8)
    if (showModelSelectorDialog) {
        ModelSelectorDialog(
            currentModel = activeModel,
            onDismiss = { showModelSelectorDialog = false },
            onSelectModel = { selected ->
                activeModel = selected
                generationLogs.add(
                    0,
                    LogActivity(
                        timestamp = timeFormat.format(Date()),
                        title = "AI Model Switched to ${selected.displayName}",
                        detail = "Dynamic generation calibrated for ${selected.description} (${selected.speedInfo})",
                        status = LogStatus.SUCCESS
                    )
                )
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Model switched to ${selected.displayName} (${selected.badgeLabel})")
                }
            }
        )
    }

    // 2. Export Dialog with 'Push to GitHub' (pre-filled @sahilkurrey & live 0-100% progress) and 'Download ZIP'
    if (showExportDialog) {
        ExportDialog(
            project = currentProject,
            onDismiss = { showExportDialog = false },
            onZipExportSuccess = { zipName, sizeBytes ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Project ZIP packaged: $zipName (${sizeBytes / 1024} KB)")
                }
            },
            onGitHubPushSuccess = { repoName, repoUrl ->
                generationLogs.add(
                    0,
                    LogActivity(
                        timestamp = timeFormat.format(Date()),
                        title = "GitHub Remote Sync Complete",
                        detail = "Successfully pushed code to $repoUrl (branch: main)",
                        status = LogStatus.SUCCESS
                    )
                )
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Successfully pushed to GitHub: $repoUrl")
                }
            }
        )
    }
}

// -----------------------------------------------------------------
// 1. Studio Workspace Top Bar (Back, Editable Title, Remix, Share, Publish, Export)
// -----------------------------------------------------------------
@Composable
fun StudioWorkspaceTopBar(
    title: String,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onToggleEditTitle: () -> Unit,
    onBack: () -> Unit,
    onRemix: () -> Unit,
    onShare: () -> Unit,
    onPublish: () -> Unit,
    onOpenExport: () -> Unit,
    onFeedback: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: [ <- Back to start ]
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("back_to_start_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to start",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Back to start",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Center: Project Title (Editable / Centered)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onToggleEditTitle() }
                    .padding(horizontal = 8.dp)
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.widthIn(max = 200.dp)
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
            }

            // Right Actions: Remix (functional), Share (placeholder), Publish (placeholder), Export
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Remix Button (Fully Functional)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onRemix() }
                        .testTag("topbar_remix_btn")
                ) {
                    Text(
                        text = "⤳ Remix",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                // Share Button (UI Placeholder)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onShare() }
                        .testTag("topbar_share_btn")
                ) {
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                // Publish Button (UI Placeholder)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onPublish() }
                        .testTag("topbar_publish_btn")
                ) {
                    Text(
                        text = "Publish",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Export Button (Opens dialog with Push to GitHub and Download ZIP)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenExport() }
                        .testTag("topbar_export_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Export", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                IconButton(onClick = onFeedback, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = "Report issue", modifier = Modifier.size(15.dp))
                }

                IconButton(onClick = onSettings, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings / Select Model", modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 2. Left Panel (AI Generation Logs, Model Selector & Expandable Prompt Bar)
// -----------------------------------------------------------------
@Composable
fun LeftPanelChatAndLogs(
    projectTitle: String,
    currentModel: GeminiModel,
    onOpenModelSelector: () -> Unit,
    generationLogs: List<LogActivity>,
    chatMessages: List<ChatMessage>,
    promptInputText: String,
    onPromptInputChange: (String) -> Unit,
    onSendPrompt: () -> Unit,
    onAttachClick: () -> Unit,
    onMicClick: () -> Unit,
    onCheckpoint: () -> Unit,
    onRestore: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isActionHistoryExpanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header: AI Generation Logs + Model Selector Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GeminiSparkle(size = 18.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Generation Logs",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Interactive Model Selector Pill (Gemini Pro, 3.5, 3.8)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenModelSelector() }
                        .testTag("model_selector_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentModel.badgeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch Gemini Model",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(onClick = onOpenModelSelector, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New generation session",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Scrollable Panel: Real-Time AI Generation Activities
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Status Activities with Timestamps and Status Tags (Success, In-Progress, Error)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Real-time AI Pipeline Activity",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentModel.badgeLabel} • ${currentModel.speedInfo}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    generationLogs.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val (tagColor, tagBg, tagIcon) = when (log.status) {
                                LogStatus.SUCCESS -> Triple(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f), Icons.Default.CheckCircle)
                                LogStatus.IN_PROGRESS -> Triple(Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.15f), Icons.Default.HourglassTop)
                                LogStatus.ERROR -> Triple(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f), Icons.Default.Error)
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tagBg,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = tagIcon, contentDescription = null, tint = tagColor, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = when (log.status) {
                                            LogStatus.SUCCESS -> "Success"
                                            LogStatus.IN_PROGRESS -> "In-Progress"
                                            LogStatus.ERROR -> "Error"
                                        },
                                        color = tagColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = log.timestamp,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = log.detail,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // User prompt message
            chatMessages.firstOrNull { it.isUser }?.let { userMsg ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = userMsg.text,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Screenshot_2026-10-02-10-42-58-44_40deb401b9ff...",
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Model status line with active model
            Text(
                text = "${currentModel.badgeLabel} • ${currentModel.speedInfo}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            // Collapsible Action History Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isActionHistoryExpanded = !isActionHistoryExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Action history", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        Icon(
                            imageVector = if (isActionHistoryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isActionHistoryExpanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "✏️ Edited 7 files",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            listOf(
                                "app/src/main/java/com/example/ui/screens/WorkspaceScreen.kt",
                                "app/src/main/java/com/example/ui/components/ExportDialog.kt",
                                "app/src/main/java/com/example/ui/components/ModelSelectorDialog.kt",
                                "app/src/main/java/com/example/ui/engine/LivePreviewRenderingEngine.kt",
                                "app/src/main/java/com/example/viewmodel/AIStudioViewModel.kt",
                                "app/src/main/java/com/example/model/GeminiModel.kt",
                                "app/src/main/java/com/example/MainActivity.kt"
                            ).forEach { filePath ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = filePath,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "🔨 Built successfully with ${currentModel.displayName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                        }
                    }
                }
            }

            // AI explanation
            chatMessages.filter { !it.isUser }.forEach { aiMsg ->
                Text(
                    text = aiMsg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Action row above prompt bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCheckpoint, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = "Checkpoint", modifier = Modifier.size(14.dp))
                    }
                    Text("Checkpoint", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Helpful", modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ThumbDown, contentDescription = "Unhelpful", modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { onRestore() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Restore", fontSize = 11.sp)
                    }
                }
            }
        }

        // Expandable & Scrollable Prompt Bar (Bottom)
        ExpandablePromptBar(
            promptText = promptInputText,
            onPromptChange = onPromptInputChange,
            onSend = onSendPrompt,
            onAttach = onAttachClick,
            onMic = onMicClick
        )
    }
}

// -----------------------------------------------------------------
// 3. Workspace Tabs Header (Preview vs Code and Export ⌄)
// -----------------------------------------------------------------
@Composable
fun WorkspaceTabsHeader(
    selectedTab: WorkspaceTab,
    onSelectTab: (WorkspaceTab) -> Unit,
    onOpenExport: () -> Unit,
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: [ 👁 Preview ] + [ < > Code ]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // [ 👁 Preview ]
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTab == WorkspaceTab.PREVIEW) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                    border = BorderStroke(1.dp, if (selectedTab == WorkspaceTab.PREVIEW) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color.Transparent),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(WorkspaceTab.PREVIEW) }
                        .testTag("tab_preview")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (selectedTab == WorkspaceTab.PREVIEW) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Preview",
                            fontSize = 12.5.sp,
                            fontWeight = if (selectedTab == WorkspaceTab.PREVIEW) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // [ < > Code ]
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTab == WorkspaceTab.CODE) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                    border = BorderStroke(1.dp, if (selectedTab == WorkspaceTab.CODE) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color.Transparent),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(WorkspaceTab.CODE) }
                        .testTag("tab_code")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = if (selectedTab == WorkspaceTab.CODE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Code",
                            fontSize = 12.5.sp,
                            fontWeight = if (selectedTab == WorkspaceTab.CODE) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Right: Zoom controls and [ Export ⌄ ] button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onZoomOut, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom out", modifier = Modifier.size(14.dp))
                }
                Text(
                    text = "${(scale * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onResetZoom() }
                )
                IconButton(onClick = onZoomIn, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom in", modifier = Modifier.size(14.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                // [ Export ⌄ ] Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenExport() }
                        .testTag("export_dropdown_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Export",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Export options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 4. Code Editor Pane with Line Numbers and Active File Tab
// -----------------------------------------------------------------
@Composable
fun CodeEditorPane(
    fileName: String,
    filePath: String,
    code: String,
    onToggleFileTree: () -> Unit,
    onCopyCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val lines = remember(code) { code.lines() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF0F172A))
    ) {
        // Tab Header: Active file name + settings
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFF7F52FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = fileName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopyCode, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onToggleFileTree, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Editor settings",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Code Editor Body: Line numbers + Code with Syntax Highlighting
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Line numbers gutter
            Column(
                modifier = Modifier
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.indices.forEach { index ->
                    Text(
                        text = "${index + 1}",
                        color = Color(0xFF475569),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1E293B))
            )

            // Syntax highlighted code
            SelectionContainer(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    lines.forEach { line ->
                        HighlightedCodeLine(line = line)
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightedCodeLine(line: String) {
    val trimmed = line.trimStart()
    val color = when {
        trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*") -> Color(0xFF64748B)
        trimmed.startsWith("package ") || trimmed.startsWith("import ") -> Color(0xFF38BDF8)
        trimmed.startsWith("class ") || trimmed.startsWith("fun ") || trimmed.startsWith("override fun ") -> Color(0xFFA78BFA)
        trimmed.startsWith("val ") || trimmed.startsWith("var ") -> Color(0xFF93C5FD)
        trimmed.startsWith("@") -> Color(0xFFFBBF24)
        trimmed.contains(" = ") || trimmed.contains("return ") -> Color(0xFFE2E8F0)
        else -> Color(0xFFCBD5E1)
    }

    Text(
        text = line.ifEmpty { " " },
        color = color,
        fontSize = 11.5.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 18.sp
    )
}

// -----------------------------------------------------------------
// 5. Expandable Prompt Bar
// -----------------------------------------------------------------
@Composable
fun ExpandablePromptBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onMic: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Internally scrollable multi-line text input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp, max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (promptText.isEmpty()) {
                            Text(
                                text = "Make changes, add new features, ask for anything",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        }

                        BasicTextField(
                            value = promptText,
                            onValueChange = onPromptChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("workspace_prompt_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onAttach, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Attach file",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = onMic, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            val canSend = promptText.isNotBlank()
                            Surface(
                                shape = CircleShape,
                                color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canSend) { onSend() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Send",
                                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "3",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = Icons.Default.ExpandLess,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

fun getFileContent(fileName: String, defaultAppCode: String): String {
    return when (fileName) {
        "MainActivity.kt" -> """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AIStudioViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AIStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AIStudioMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AIStudioMainApp(viewModel: AIStudioViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // Live Compose Screen Content...
    }
}
        """.trimIndent()

        "Models.kt" -> """
package com.example.model

enum class Screen { HOME, WORKSPACE, MY_APPS, HISTORY, PLAYGROUND, GALLERY }

enum class ProjectLanguage { KOTLIN, TYPESCRIPT, FLUTTER, ANDROID, OTHER }

data class AppProject(
    val id: String,
    val title: String,
    val description: String,
    val language: ProjectLanguage,
    val lastModified: String
)
        """.trimIndent()

        "SavedAppEntity.kt" -> """
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_apps")
data class SavedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val description: String,
    val category: String,
    val previewType: String,
    val codeSnippet: String,
    val language: String = "ANDROID",
    val lastModified: String = "Today"
)
        """.trimIndent()

        "build.gradle.kts" -> """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.aistudio.build.workspace"
        minSdk = 24
        targetSdk = 36
    }
}
        """.trimIndent()

        else -> defaultAppCode
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("AI Studio Code", text)
    clipboard.setPrimaryClip(clip)
}
