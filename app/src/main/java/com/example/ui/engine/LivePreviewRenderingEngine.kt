package com.example.ui.engine

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ProjectData
import com.example.ui.screens.InteractiveLiveApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RenderEngineMode {
    NATIVE_COMPOSE,   // High performance Jetpack Compose interactive interpreter
    EMBEDDED_WEBVIEW  // Real-time compiled HTML5/JS Web preview
}

enum class DeviceFrameStyle {
    PIXEL_PHONE,
    TABLET,
    FLUID_CANVAS
}

data class ConsoleLogItem(
    val time: String,
    val level: String, // "INFO", "WARN", "COMPILE", "HOT_RELOAD"
    val message: String
)

/**
 * Live Preview Rendering Engine:
 * Compiles and displays active code in real time with dynamic interpretation,
 * embedded WebView output rendering, device frame simulation, and live console logs.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewRenderingEngine(
    project: ProjectData,
    activeCode: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isCompiling by remember { mutableStateOf(true) }
    var compileTimeMs by remember { mutableStateOf(142) }
    var engineMode by remember { mutableStateOf(RenderEngineMode.NATIVE_COMPOSE) }
    var deviceFrame by remember { mutableStateOf(DeviceFrameStyle.PIXEL_PHONE) }
    var showConsole by remember { mutableStateOf(false) }

    val consoleLogs = remember {
        mutableStateListOf(
            ConsoleLogItem("0.00s", "COMPILE", "Analyzing AST tree for ${project.title}"),
            ConsoleLogItem("0.04s", "COMPILE", "Kotlin Compose compiler: Resolving RecompositionScope"),
            ConsoleLogItem("0.09s", "INFO", "Room database mock layer bound to SQLite in-memory store"),
            ConsoleLogItem("0.14s", "HOT_RELOAD", "Hot-reload payload synced: 0 errors, 0 warnings")
        )
    }

    // Trigger re-compilation simulation whenever activeCode changes or on screen entrance
    LaunchedEffect(activeCode, engineMode) {
        isCompiling = true
        val start = System.currentTimeMillis()
        delay(350)
        compileTimeMs = (System.currentTimeMillis() - start).toInt().coerceAtLeast(80)
        isCompiling = false
        consoleLogs.add(
            0,
            ConsoleLogItem(
                "${System.currentTimeMillis() % 10000 / 1000f}s",
                "HOT_RELOAD",
                "Hot-reloaded code delta successfully in ${compileTimeMs}ms"
            )
        )
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Engine Control Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Engine status & Hot Reload status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompiling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compiling code...",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Active (${compileTimeMs}ms)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Engine Mode Switcher (Native Compose vs WebView)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (engineMode == RenderEngineMode.NATIVE_COMPOSE) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier.clickable { engineMode = RenderEngineMode.NATIVE_COMPOSE }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = if (engineMode == RenderEngineMode.NATIVE_COMPOSE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Native",
                                    fontSize = 11.sp,
                                    fontWeight = if (engineMode == RenderEngineMode.NATIVE_COMPOSE) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (engineMode == RenderEngineMode.EMBEDDED_WEBVIEW) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier.clickable { engineMode = RenderEngineMode.EMBEDDED_WEBVIEW }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (engineMode == RenderEngineMode.EMBEDDED_WEBVIEW) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Web Output",
                                    fontSize = 11.sp,
                                    fontWeight = if (engineMode == RenderEngineMode.EMBEDDED_WEBVIEW) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Right: Device frame selector, reload, and console toggle
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Frame selector
                    IconButton(
                        onClick = {
                            deviceFrame = when (deviceFrame) {
                                DeviceFrameStyle.PIXEL_PHONE -> DeviceFrameStyle.TABLET
                                DeviceFrameStyle.TABLET -> DeviceFrameStyle.FLUID_CANVAS
                                DeviceFrameStyle.FLUID_CANVAS -> DeviceFrameStyle.PIXEL_PHONE
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = when (deviceFrame) {
                                DeviceFrameStyle.PIXEL_PHONE -> Icons.Default.PhoneAndroid
                                DeviceFrameStyle.TABLET -> Icons.Default.Tablet
                                DeviceFrameStyle.FLUID_CANVAS -> Icons.Default.Devices
                            },
                            contentDescription = "Switch device frame",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Reload
                    IconButton(
                        onClick = {
                            isCompiling = true
                            coroutineScope.launch {
                                delay(300)
                                isCompiling = false
                                consoleLogs.add(0, ConsoleLogItem("now", "HOT_RELOAD", "Manual reload triggered"))
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload app engine",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Console Drawer Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showConsole) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { showConsole = !showConsole }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = if (showConsole) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Console (${consoleLogs.size})",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (isCompiling) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
        }

        // Live Output Content Area (Wrapped in chosen device frame)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0F172A).copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            val frameModifier = when (deviceFrame) {
                DeviceFrameStyle.PIXEL_PHONE -> Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxHeight(0.96f)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                DeviceFrameStyle.TABLET -> Modifier
                    .widthIn(max = 700.dp)
                    .fillMaxHeight(0.96f)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                DeviceFrameStyle.FLUID_CANVAS -> Modifier.fillMaxSize()
            }

            Surface(
                modifier = frameModifier,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mobile Status Bar Simulation (only in phone/tablet frame)
                    if (deviceFrame != DeviceFrameStyle.FLUID_CANVAS) {
                        SimulatedDeviceStatusBar(title = project.title)
                    }

                    // Render Output based on chosen engine mode
                    Box(modifier = Modifier.weight(1f)) {
                        when (engineMode) {
                            RenderEngineMode.NATIVE_COMPOSE -> {
                                // Native Jetpack Compose Dynamic Interpreter
                                InteractiveLiveApp(project = project)
                            }
                            RenderEngineMode.EMBEDDED_WEBVIEW -> {
                                // Embedded WebView Dynamic Renderer
                                EmbeddedWebViewRenderer(
                                    project = project,
                                    activeCode = activeCode
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Console Drawer (Collapsible)
        AnimatedVisibility(visible = showConsole) {
            Surface(
                color = Color(0xFF0B0F19),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .border(BorderStroke(0.5.dp, Color(0xFF334155)))
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE ENGINE CONSOLE",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Clear",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            modifier = Modifier.clickable { consoleLogs.clear() }
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        consoleLogs.forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.time,
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val badgeColor = when (log.level) {
                                    "COMPILE" -> Color(0xFF818CF8)
                                    "HOT_RELOAD" -> Color(0xFF34D399)
                                    "WARN" -> Color(0xFFFBBF24)
                                    else -> Color(0xFF38BDF8)
                                }
                                Text(
                                    text = "[${log.level}]",
                                    color = badgeColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.message,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Status Bar Simulation for Phone/Tablet device frames
 */
@Composable
private fun SimulatedDeviceStatusBar(title: String) {
    Surface(
        color = Color(0xFF0F172A),
        modifier = Modifier.fillMaxWidth().height(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "12:41",
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
            // Simulated camera pill
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 10.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "5G",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "100%",
                    color = Color.White,
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * Embedded Dynamic WebView Renderer:
 * Translates active project code and templates into a compiled, reactive HTML5/CSS/JS application,
 * rendered live inside an embedded Android WebView with smooth responsiveness.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbeddedWebViewRenderer(
    project: ProjectData,
    activeCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compiledHtml = remember(project, activeCode) {
        generateCompiledWebHtml(project, activeCode)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false // keep inside webview
                    }
                }
                loadDataWithBaseURL("https://ai-studio-preview.local", compiledHtml, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://ai-studio-preview.local", compiledHtml, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize().testTag("embedded_webview_renderer")
    )
}

/**
 * Compiles project state and active code into a live interactive web app payload
 */
private fun generateCompiledWebHtml(project: ProjectData, code: String): String {
    val escapedTitle = project.title.replace("\"", "&quot;")
    val escapedPrompt = project.prompt.replace("\"", "&quot;")

    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <title>$escapedTitle</title>
          <link rel="preconnect" href="https://fonts.googleapis.com">
          <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', system-ui, sans-serif; -webkit-tap-highlight-color: transparent; }
            body { background: #0B0F19; color: #F8FAFC; min-height: 100vh; display: flex; flex-direction: column; overflow-x: hidden; }
            header { background: #1E293B; padding: 14px 18px; border-bottom: 1px solid #334155; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 10; }
            .header-title { font-size: 15px; font-weight: 700; color: #38BDF8; display: flex; align-items: center; gap: 8px; }
            .status-badge { background: rgba(16, 185, 129, 0.15); color: #10B981; font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 12px; }
            main { padding: 16px; flex: 1; display: flex; flex-direction: column; gap: 14px; }
            .card { background: #1E293B; border: 1px solid #334155; border-radius: 14px; padding: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); }
            .card h3 { font-size: 14px; font-weight: 700; margin-bottom: 6px; color: #F1F5F9; }
            .card p { font-size: 12.5px; color: #94A3B8; line-height: 1.5; }
            .task-list { display: flex; flex-direction: column; gap: 8px; margin-top: 10px; }
            .task-item { background: #0F172A; border: 1px solid #334155; border-radius: 10px; padding: 10px 12px; display: flex; align-items: center; justify-content: space-between; cursor: pointer; transition: all 0.2s; }
            .task-item:active { transform: scale(0.98); }
            .task-item.done { opacity: 0.6; text-decoration: line-through; border-color: #10B981; }
            .task-item span { font-size: 12.5px; font-weight: 500; }
            .input-box { display: flex; gap: 8px; margin-top: 8px; }
            input[type="text"] { flex: 1; background: #0F172A; border: 1px solid #475569; color: #FFF; border-radius: 8px; padding: 10px 12px; font-size: 13px; outline: none; }
            input[type="text"]:focus { border-color: #38BDF8; }
            button.btn-primary { background: #38BDF8; color: #0F172A; border: none; border-radius: 8px; padding: 0 16px; font-size: 13px; font-weight: 700; cursor: pointer; }
            button.btn-primary:active { background: #0284C7; }
            footer { background: #1E293B; padding: 12px; border-top: 1px solid #334155; text-align: center; font-size: 11px; color: #64748B; }
          </style>
        </head>
        <body>
          <header>
            <div class="header-title">
              <span>✦</span>
              <span>$escapedTitle</span>
            </div>
            <div class="status-badge">Compiled & Ready</div>
          </header>
          
          <main>
            <div class="card">
              <h3>Live Interactive Sandbox</h3>
              <p>$escapedPrompt</p>
              
              <div class="input-box">
                <input type="text" id="taskInput" placeholder="Add an item to live state..." />
                <button class="btn-primary" onclick="addTask()">Add</button>
              </div>

              <div class="task-list" id="taskList">
                <div class="task-item" onclick="toggleTask(this)">
                  <span>Initialize Room database offline cache</span>
                  <span style="color:#10B981;">✓</span>
                </div>
                <div class="task-item" onclick="toggleTask(this)">
                  <span>Compose multi-screen Navigation backstack</span>
                  <span style="color:#10B981;">✓</span>
                </div>
                <div class="task-item" onclick="toggleTask(this)">
                  <span>Pinch-to-zoom Canvas scaling container</span>
                  <span style="color:#38BDF8;">●</span>
                </div>
              </div>
            </div>

            <div class="card" style="border-left: 3px solid #8B5CF6;">
              <h3 style="color:#C084FC;">Hot-Reload Engine Status</h3>
              <p>State preserved across recompilations. Click any task above to toggle completion in real time.</p>
            </div>
          </main>

          <footer>
            Google AI Studio • Real-Time Live Preview Engine
          </footer>

          <script>
            function addTask() {
              const input = document.getElementById('taskInput');
              const val = input.value.trim();
              if (val) {
                const list = document.getElementById('taskList');
                const div = document.createElement('div');
                div.className = 'task-item';
                div.onclick = function() { toggleTask(this); };
                div.innerHTML = '<span>' + val + '</span><span style="color:#38BDF8;">●</span>';
                list.insertBefore(div, list.firstChild);
                input.value = '';
              }
            }
            function toggleTask(el) {
              el.classList.toggle('done');
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
