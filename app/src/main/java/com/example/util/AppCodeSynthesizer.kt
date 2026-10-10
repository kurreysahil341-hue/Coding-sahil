package com.example.util

object AppCodeSynthesizer {

    fun extractKotlinCode(geminiResponse: String): String {
        val trimmed = geminiResponse.trim()
        val kotlinBlockRegex = "```kotlin[\\s\\S]*?```".toRegex()
        val genericBlockRegex = "```[\\s\\S]*?```".toRegex()

        val kotlinMatch = kotlinBlockRegex.find(trimmed)
        if (kotlinMatch != null) {
            return kotlinMatch.value
                .removePrefix("```kotlin")
                .removeSuffix("```")
                .trim()
        }

        val genericMatch = genericBlockRegex.find(trimmed)
        if (genericMatch != null) {
            val content = genericMatch.value
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            if (content.contains("package ") || content.contains("@Composable") || content.contains("fun ")) {
                return content
            }
        }

        if (trimmed.contains("@Composable") && (trimmed.contains("fun ") || trimmed.contains("class "))) {
            return trimmed
        }

        return ""
    }

    fun detectPreviewType(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("login") || lower.contains("auth") || lower.contains("sign in") || lower.contains("signup") -> "LOGIN"
            lower.contains("calc") || lower.contains("calculator") || lower.contains("hisaab") -> "CALCULATOR"
            lower.contains("weather") || lower.contains("mausam") || lower.contains("forecast") -> "WEATHER"
            lower.contains("chat") || lower.contains("message") || lower.contains("baat") -> "CHAT"
            lower.contains("counter") || lower.contains("clicker") || lower.contains("ginti") -> "COUNTER"
            lower.contains("shop") || lower.contains("cart") || lower.contains("store") || lower.contains("kharid") -> "SHOPPING"
            lower.contains("drive") -> "GOOGLE_DRIVE"
            lower.contains("sheet") -> "GOOGLE_SHEETS"
            lower.contains("mail") || lower.contains("email") || lower.contains("gmail") -> "GMAIL"
            lower.contains("todo") || lower.contains("task") || lower.contains("note") || lower.contains("list") -> "TODO"
            else -> "CUSTOM"
        }
    }

    fun synthesizeAppCode(prompt: String, existingCode: String = ""): String {
        val type = detectPreviewType(prompt)
        val cleanPrompt = prompt.replace("\"", "\\\"").trim().ifEmpty { "Interactive Android App" }

        return when (type) {
            "LOGIN" -> """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LoginAppScreen()
            }
        }
    }
}

@Composable
fun LoginAppScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoggedIn) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Welcome Back!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isLoggedIn = false; statusMessage = "" }) {
                            Text("Sign Out")
                        }
                    }
                }
            } else {
                Text(
                    text = "Welcome to Gemini App",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$cleanPrompt",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.length >= 4) {
                            isLoggedIn = true
                        } else {
                            statusMessage = "Please enter valid credentials"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold)
                }

                if (statusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(statusMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    }
}
""".trimIndent()

            "CALCULATOR" -> """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = display,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(20.dp)
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(weight).height(60.dp),
                            colors = if (btn in listOf("+", "-", "×", "÷", "=")) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            } else {
                                ButtonDefaults.filledTonalButtonColors()
                            }
                        ) {
                            Text(btn, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
""".trimIndent()

            "WEATHER" -> """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WeatherAppScreen()
            }
        }
    }
}

@Composable
fun WeatherAppScreen() {
    var city by remember { mutableStateOf("New Delhi") }
    var temperature by remember { mutableIntStateOf(29) }
    var condition by remember { mutableStateOf("Partly Sunny") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(city, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(condition, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("${'$'}temperature°C", fontSize = 54.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Feels like 31°C", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.weight(1f).padding(4.dp)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Humidity", fontSize = 11.sp)
                        Text("48%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.weight(1f).padding(4.dp)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Air, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Wind", fontSize = 11.sp)
                        Text("14 km/h", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
""".trimIndent()

            "COUNTER" -> """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CounterScreen()
            }
        }
    }
}

@Composable
fun CounterScreen() {
    var count by remember { mutableIntStateOf(0) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Interactive Counter", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(160.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = count.toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledTonalIconButton(onClick = { count-- }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement")
                }
                IconButton(onClick = { count = 0 }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
                FilledIconButton(onClick = { count++ }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increment")
                }
            }
        }
    }
}
""".trimIndent()

            else -> """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

data class ItemData(val id: Long, val title: String, val isCompleted: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    var items by remember {
        mutableStateOf(
            listOf(
                ItemData(1, "Implemented feature: $cleanPrompt", true),
                ItemData(2, "Room SQLite cache synchronization", true),
                ItemData(3, "Material 3 Dynamic Theme tokens", false),
                ItemData(4, "Coroutines StateFlow reactive binding", false)
            )
        )
    }
    var newItemText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$cleanPrompt", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (newItemText.isNotBlank()) {
                    items = items + ItemData(System.currentTimeMillis(), newItemText)
                    newItemText = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text("Add new feature entry...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { isChecked ->
                                    items = items.map { if (it.id == item.id) it.copy(isCompleted = isChecked) else it }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                modifier = Modifier.weight(1f),
                                fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium
                            )
                            IconButton(onClick = { items = items.filter { it.id != item.id } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
""".trimIndent()
        }
    }
}
