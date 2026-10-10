package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.PromptHistoryEntity
import com.example.data.SavedAppEntity
import com.example.model.AttachedFile
import com.example.model.BuildStep
import com.example.model.ProjectData
import com.example.model.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AIStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(database.appDao())
    }

    // Room reactive streams
    val savedApps: StateFlow<List<SavedAppEntity>> = repository.allSavedApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val promptHistory: StateFlow<List<PromptHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation and screen state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isSidebarVisible = MutableStateFlow(false)
    val isSidebarVisible: StateFlow<Boolean> = _isSidebarVisible.asStateFlow()

    // Prompt bar state
    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _attachedFiles = MutableStateFlow<List<AttachedFile>>(emptyList())
    val attachedFiles: StateFlow<List<AttachedFile>> = _attachedFiles.asStateFlow()

    // Workspace & Build state
    private val _activeProject = MutableStateFlow<ProjectData?>(null)
    val activeProject: StateFlow<ProjectData?> = _activeProject.asStateFlow()

    private val _buildStep = MutableStateFlow(BuildStep.READY)
    val buildStep: StateFlow<BuildStep> = _buildStep.asStateFlow()

    // Dialogs state
    private val _showUpgradeDialog = MutableStateFlow(false)
    val showUpgradeDialog: StateFlow<Boolean> = _showUpgradeDialog.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showDashboardDialog = MutableStateFlow(false)
    val showDashboardDialog: StateFlow<Boolean> = _showDashboardDialog.asStateFlow()

    private val _showDocsDialog = MutableStateFlow(false)
    val showDocsDialog: StateFlow<Boolean> = _showDocsDialog.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-2.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _userApiKey = MutableStateFlow(com.example.util.ApiKeyManager.getApiKey(application))
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    // "I'm feeling lucky" sample pool
    private val luckyPrompts = listOf(
        "Build an Android app with Room database, Material 3 cards, task status toggling, and priority tags",
        "Build a responsive Google Drive file explorer with storage metrics, upload progress, and document preview",
        "Build a Google Sheets finance tracker with expense categorization and real-time calculation",
        "Build a smart Gmail triage inbox with priority labels, quick reply drafts, and thread view",
        "Build an AI-powered fitness workout logger with daily set counts and heart-rate zone visualization",
        "Build a modern e-commerce product catalog with shopping cart, search filter, and checkout flow"
    )
    private var luckyIndex = 0

    fun onPromptChange(newText: String) {
        _promptText.value = newText
    }

    fun onAttachFile(file: AttachedFile) {
        _attachedFiles.value = _attachedFiles.value + file
    }

    fun onRemoveAttachment(fileId: String) {
        _attachedFiles.value = _attachedFiles.value.filter { it.id != fileId }
    }

    fun toggleSidebar() {
        _isSidebarVisible.value = !_isSidebarVisible.value
    }

    fun setSidebarVisible(visible: Boolean) {
        _isSidebarVisible.value = visible
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _isSidebarVisible.value = false
    }

    fun onFeelingLucky() {
        _promptText.value = luckyPrompts[luckyIndex % luckyPrompts.size]
        luckyIndex++
    }

    fun submitPrompt() {
        val prompt = _promptText.value.trim().ifEmpty {
            "Build an Android app with Room local database and Material 3 UI"
        }

        val attachmentsCount = _attachedFiles.value.size

        // Determine app type & title
        val (title, category, previewType) = when {
            prompt.contains("Drive", ignoreCase = true) -> Triple("Google Drive Sync", "Integration", "GOOGLE_DRIVE")
            prompt.contains("Sheet", ignoreCase = true) -> Triple("Sheets Financial Grid", "Productivity", "GOOGLE_SHEETS")
            prompt.contains("Gmail", ignoreCase = true) || prompt.contains("Email", ignoreCase = true) -> Triple("Gmail Triage Inbox", "Communication", "GMAIL")
            else -> Triple("Android Sprint Board", "Android App", "ANDROID_APP")
        }

        val project = ProjectData(
            id = System.currentTimeMillis(),
            title = title,
            prompt = prompt,
            category = category,
            previewType = previewType,
            code = generateCodeSnippet(title, previewType)
        )

        _activeProject.value = project

        // Save prompt into Room History
        viewModelScope.launch {
            repository.insertHistory(
                PromptHistoryEntity(
                    promptText = prompt,
                    attachmentCount = attachmentsCount,
                    generatedAppTitle = title
                )
            )
        }

        // Navigate to Two-Panel Workspace and trigger build step simulation
        _currentScreen.value = Screen.WORKSPACE
        _isSidebarVisible.value = false

        viewModelScope.launch {
            _buildStep.value = BuildStep.ANALYZING
            delay(400)
            _buildStep.value = BuildStep.ARCHITECTING
            delay(500)
            _buildStep.value = BuildStep.CODING
            delay(600)
            _buildStep.value = BuildStep.COMPILING
            delay(400)
            _buildStep.value = BuildStep.READY
        }
    }

    fun openSavedAppInWorkspace(app: SavedAppEntity) {
        _activeProject.value = ProjectData(
            id = app.id,
            title = app.title,
            prompt = app.prompt,
            category = app.category,
            previewType = app.previewType,
            code = app.codeSnippet,
            createdAt = app.createdAt
        )
        _buildStep.value = BuildStep.READY
        _currentScreen.value = Screen.WORKSPACE
    }

    fun remixApp(promptText: String) {
        _promptText.value = promptText
        _currentScreen.value = Screen.HOME
    }

    fun duplicateOrRemixProject(project: ProjectData): ProjectData {
        val remixedTitle = if (project.title.startsWith("Remix: ")) "Remix (Copy): ${project.title.removePrefix("Remix: ")}" else "Remix: ${project.title}"
        val remixed = ProjectData(
            id = System.currentTimeMillis(),
            title = remixedTitle,
            prompt = "Remixed from ${project.title} • ${project.prompt}",
            category = project.category,
            previewType = project.previewType,
            code = project.code,
            language = project.language
        )
        _activeProject.value = remixed
        viewModelScope.launch {
            repository.insertApp(
                SavedAppEntity(
                    title = remixed.title,
                    prompt = remixed.prompt,
                    description = "Remix of ${project.title}",
                    category = remixed.category,
                    previewType = remixed.previewType,
                    codeSnippet = remixed.code,
                    language = remixed.language.name,
                    lastModified = "Just now",
                    iconType = "code"
                )
            )
        }
        return remixed
    }

    fun deleteSavedApp(appId: Long) {
        viewModelScope.launch {
            repository.deleteApp(appId)
        }
    }

    fun deleteHistoryEntry(historyId: Long) {
        viewModelScope.launch {
            repository.deleteHistory(historyId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun openProjectById(projectId: String) {
        val app = savedApps.value.firstOrNull { it.id.toString() == projectId }
        if (app != null) {
            openSavedAppInWorkspace(app)
        } else {
            openRecentProject("Project $projectId")
        }
    }

    fun openRecentProject(title: String) {
        val app = savedApps.value.firstOrNull { it.title.equals(title, ignoreCase = true) }
        if (app != null) {
            openSavedAppInWorkspace(app)
        } else {
            val previewType = when {
                title.contains("Drive", ignoreCase = true) -> "GOOGLE_DRIVE"
                title.contains("Sheet", ignoreCase = true) || title.contains("Khata", ignoreCase = true) -> "GOOGLE_SHEETS"
                title.contains("Gmail", ignoreCase = true) -> "GMAIL"
                else -> "ANDROID_APP"
            }
            _activeProject.value = ProjectData(
                id = System.currentTimeMillis(),
                title = title,
                prompt = "Interactive project $title generated by Gemini",
                category = "Android App",
                previewType = previewType,
                code = generateCodeSnippet(title, previewType)
            )
            _buildStep.value = BuildStep.READY
            _currentScreen.value = Screen.WORKSPACE
        }
        _isSidebarVisible.value = false
    }

    fun saveCurrentAppToRoom() {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            repository.insertApp(
                SavedAppEntity(
                    title = current.title,
                    prompt = current.prompt,
                    description = "Created with Gemini 2.5 in AI Studio Build",
                    category = current.category,
                    previewType = current.previewType,
                    codeSnippet = current.code,
                    isFavorite = true
                )
            )
        }
    }

    // Dialog controls
    fun setUpgradeDialog(show: Boolean) { _showUpgradeDialog.value = show }
    fun setApiKeyDialog(show: Boolean) { _showApiKeyDialog.value = show }
    fun setSettingsDialog(show: Boolean) { _showSettingsDialog.value = show }
    fun setDashboardDialog(show: Boolean) { _showDashboardDialog.value = show }
    fun setDocsDialog(show: Boolean) { _showDocsDialog.value = show }
    fun setSelectedModel(model: String) { _selectedModel.value = model }
    fun setUserApiKey(key: String) {
        _userApiKey.value = key
        com.example.util.ApiKeyManager.saveApiKey(getApplication(), key)
    }

    private fun generateCodeSnippet(title: String, previewType: String): String {
        return """
package com.example.generated

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Generated by Google AI Studio
 * Architecture: Clean MVVM + Room Database + Material 3
 * Model: Gemini 2.5 Flash
 */

@Composable
fun ${title.replace(" ", "")}Screen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Live Interactive Sandbox Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Ready to test and deploy.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
        """.trimIndent()
    }
}
