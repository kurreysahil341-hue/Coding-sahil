package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BuildStep
import com.example.model.ProjectData
import com.example.model.Screen
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.DashboardDialog
import com.example.ui.components.DocumentationDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SidebarContent
import com.example.ui.components.UpgradeDialog
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainWorkspaceHomeScreen
import com.example.ui.screens.MyAppsScreen
import com.example.ui.screens.PlaygroundScreen
import com.example.ui.screens.TwoPanelWorkspaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AIStudioViewModel
import kotlinx.coroutines.launch

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
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isSidebarVisible by viewModel.isSidebarVisible.collectAsStateWithLifecycle()
    val promptText by viewModel.promptText.collectAsStateWithLifecycle()
    val attachedFiles by viewModel.attachedFiles.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val buildStep by viewModel.buildStep.collectAsStateWithLifecycle()
    val savedApps by viewModel.savedApps.collectAsStateWithLifecycle()
    val promptHistory by viewModel.promptHistory.collectAsStateWithLifecycle()

    // Dialog states
    val showUpgradeDialog by viewModel.showUpgradeDialog.collectAsStateWithLifecycle()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showDashboardDialog by viewModel.showDashboardDialog.collectAsStateWithLifecycle()
    val showDocsDialog by viewModel.showDocsDialog.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val userApiKey by viewModel.userApiKey.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Sync drawer state with viewmodel
    LaunchedEffect(isSidebarVisible) {
        if (isSidebarVisible) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        viewModel.setSidebarVisible(drawerState.isOpen)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletOrDesktop = maxWidth >= 840.dp

        if (isTabletOrDesktop) {
            // Wide screen: Permanent Sidebar + Main Content Row
            Row(modifier = Modifier.fillMaxSize()) {
                SidebarContent(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    onToggleSidebar = { viewModel.toggleSidebar() },
                    onOpenUpgrade = { viewModel.setUpgradeDialog(true) },
                    onOpenApiKey = { viewModel.setApiKeyDialog(true) },
                    onOpenSettings = { viewModel.setSettingsDialog(true) },
                    onOpenDashboard = { viewModel.setDashboardDialog(true) },
                    onOpenDocs = { viewModel.setDocsDialog(true) },
                    onOpenNotifications = { viewModel.setDashboardDialog(true) },
                    onOpenRecentProject = { viewModel.openRecentProject(it) },
                    savedAppsCount = savedApps.size,
                    historyCount = promptHistory.size,
                    modifier = Modifier.width(260.dp)
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        promptText = promptText,
                        attachedFiles = attachedFiles,
                        activeProject = activeProject,
                        buildStep = buildStep,
                        savedApps = savedApps,
                        promptHistory = promptHistory,
                        isSidebarVisible = true
                    )
                }
            }
        } else {
            // Mobile screen: Modal Navigation Drawer for smooth, fluid sidebar navigation
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(280.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        SidebarContent(
                            currentScreen = currentScreen,
                            onNavigate = {
                                viewModel.navigateTo(it)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onToggleSidebar = {
                                coroutineScope.launch { drawerState.close() }
                            },
                            onOpenUpgrade = { viewModel.setUpgradeDialog(true) },
                            onOpenApiKey = { viewModel.setApiKeyDialog(true) },
                            onOpenSettings = { viewModel.setSettingsDialog(true) },
                            onOpenDashboard = { viewModel.setDashboardDialog(true) },
                            onOpenDocs = { viewModel.setDocsDialog(true) },
                            onOpenNotifications = { viewModel.setDashboardDialog(true) },
                            onOpenRecentProject = {
                                viewModel.openRecentProject(it)
                                coroutineScope.launch { drawerState.close() }
                            },
                            savedAppsCount = savedApps.size,
                            historyCount = promptHistory.size,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            ) {
                ScreenContent(
                    currentScreen = currentScreen,
                    viewModel = viewModel,
                    promptText = promptText,
                    attachedFiles = attachedFiles,
                    activeProject = activeProject,
                    buildStep = buildStep,
                    savedApps = savedApps,
                    promptHistory = promptHistory,
                    isSidebarVisible = isSidebarVisible
                )
            }
        }
    }

    // Modal Dialogs
    if (showUpgradeDialog) {
        UpgradeDialog(
            onDismiss = { viewModel.setUpgradeDialog(false) },
            onUpgrade = { /* Upgrade action */ }
        )
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = userApiKey,
            onSaveKey = { viewModel.setUserApiKey(it) },
            onDismiss = { viewModel.setApiKeyDialog(false) }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            selectedModel = selectedModel,
            onModelChange = { viewModel.setSelectedModel(it) },
            onDismiss = { viewModel.setSettingsDialog(false) }
        )
    }

    if (showDashboardDialog) {
        DashboardDialog(onDismiss = { viewModel.setDashboardDialog(false) })
    }

    if (showDocsDialog) {
        DocumentationDialog(onDismiss = { viewModel.setDocsDialog(false) })
    }
}

@Composable
private fun ScreenContent(
    currentScreen: Screen,
    viewModel: AIStudioViewModel,
    promptText: String,
    attachedFiles: List<com.example.model.AttachedFile>,
    activeProject: ProjectData?,
    buildStep: BuildStep,
    savedApps: List<com.example.data.SavedAppEntity>,
    promptHistory: List<com.example.data.PromptHistoryEntity>,
    isSidebarVisible: Boolean
) {
    when (currentScreen) {
        Screen.HOME -> {
            MainWorkspaceHomeScreen(
                promptText = promptText,
                onPromptChange = { viewModel.onPromptChange(it) },
                onSubmitPrompt = { viewModel.submitPrompt() },
                onAttachFile = { viewModel.onAttachFile(it) },
                onRemoveAttachment = { viewModel.onRemoveAttachment(it) },
                attachedFiles = attachedFiles,
                onToggleSidebar = { viewModel.toggleSidebar() },
                onOpenSettings = { viewModel.setSettingsDialog(true) },
                onOpenGallery = { viewModel.navigateTo(Screen.GALLERY) },
                onFeelingLucky = { viewModel.onFeelingLucky() },
                isSidebarVisible = isSidebarVisible
            )
        }
        Screen.WORKSPACE -> {
            val project = activeProject ?: ProjectData(
                title = "Android Sprint Board",
                prompt = "Build an Android app with Room local database",
                category = "Android App",
                previewType = "ANDROID_APP",
                code = "// Generated Jetpack Compose App"
            )
            TwoPanelWorkspaceScreen(
                project = project,
                buildStep = buildStep,
                onBack = { viewModel.navigateTo(Screen.HOME) },
                onSaveToMyApps = { viewModel.saveCurrentAppToRoom() },
                onRemixProject = { viewModel.duplicateOrRemixProject(it) }
            )
        }
        Screen.MY_APPS -> {
            MyAppsScreen(
                savedApps = savedApps,
                onOpenApp = { viewModel.openSavedAppInWorkspace(it) },
                onRemixApp = {
                    viewModel.remixApp(it.prompt)
                },
                onDeleteApp = { viewModel.deleteSavedApp(it) },
                onNewApp = { viewModel.navigateTo(Screen.HOME) },
                onBack = { viewModel.navigateTo(Screen.HOME) },
                onToggleSidebar = { viewModel.toggleSidebar() }
            )
        }
        Screen.HISTORY -> {
            HistoryScreen(
                historyList = promptHistory,
                onSelectPrompt = {
                    viewModel.onPromptChange(it)
                    viewModel.navigateTo(Screen.HOME)
                },
                onDeletePrompt = { viewModel.deleteHistoryEntry(it) },
                onClearAll = { viewModel.clearAllHistory() },
                onBack = { viewModel.navigateTo(Screen.HOME) }
            )
        }
        Screen.PLAYGROUND -> {
            PlaygroundScreen(onBack = { viewModel.navigateTo(Screen.HOME) })
        }
        Screen.GALLERY -> {
            GalleryScreen(
                onRemixTemplate = { template ->
                    viewModel.onPromptChange(template.prompt)
                    viewModel.submitPrompt()
                },
                onBack = { viewModel.navigateTo(Screen.HOME) }
            )
        }
    }
}
