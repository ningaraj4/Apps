package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edufeed.viewmodel.QuizViewModel
import com.example.edufeed.data.models.QuizSession
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration.Companion.None
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSessionListScreen(
    teacherId: String,
    navController: NavController,
    viewModel: QuizViewModel = viewModel()
) {
    var sessions by remember { mutableStateOf(listOf<QuizSession>()) }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf<String?>(null) }
    var showDurationDialog by remember { mutableStateOf<String?>(null) }
    var customDuration by remember { mutableStateOf("") }
    val durationOptions = listOf(2, 5, 10)
    var sessionToDelete by remember { mutableStateOf<QuizSession?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Active, 1 = Completed
    val now = System.currentTimeMillis()
    val activeSessions = sessions.filter { (it.status == "ACTIVE" || it.status == "DRAFT") && (it.endTime == 0L || now < it.endTime) }
    val completedSessions = sessions.filter { it.status == "ENDED" || (it.endTime > 0L && now >= it.endTime) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFB2B6FF), Color(0xFF7C3AED)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF7C3AED)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)

    // Collect quiz sessions with lifecycle awareness
    val quizSessions by viewModel.quizSessions.collectAsStateWithLifecycle()
    
    // Update local sessions when quizSessions changes
    LaunchedEffect(quizSessions) {
        sessions = quizSessions
    }
    
    // Fetch sessions on first composition
    LaunchedEffect(teacherId) {
        viewModel.syncQuizSessionsFromRemote(teacherId)
    }

    // Show error snackbar
    if (showError != null) {
        LaunchedEffect(showError) {
            showError?.let { message ->
                // Auto-dismiss after 3 seconds
                kotlinx.coroutines.delay(3000)
                showError = null
            }
        }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { 
                    TextButton(onClick = { showError = null }) { 
                        Text("Dismiss", color = Color.White) 
                    } 
                }
            ) { 
                Text(showError ?: "", color = Color.White) 
            }
        }
    }

    // Show success snackbar
    if (showSuccess != null) {
        LaunchedEffect(showSuccess) {
            showSuccess?.let { message ->
                // Auto-dismiss after 3 seconds
                kotlinx.coroutines.delay(3000)
                showSuccess = null
            }
        }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { 
                    TextButton(onClick = { showSuccess = null }) { 
                        Text("OK", color = Color.White) 
                    } 
                }
            ) { 
                Text(showSuccess ?: "", color = Color.White) 
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 86.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quiz Session List",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = accentColor,
                modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Tabs for Active/Completed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { Text("Active Sessions", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { selectedTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { Text("Completed Sessions", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
            Spacer(Modifier.height(16.dp))
            
            // Clean up button for old sessions
            if (activeSessions.isNotEmpty()) {
                Button(
                    onClick = {
                        // Delete all sessions for cleanup
                        activeSessions.forEach { session ->
                            viewModel.deleteQuizSession(session.sessionId)
                        }
                        showSuccess = "All sessions cleared"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Clear All Sessions", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
            
            val displaySessions = if (selectedTab == 0) activeSessions else completedSessions

            LazyColumn {
                items(displaySessions) { session ->
                    AnimatedVisibility(visible = true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Title: ${session.title ?: "Untitled"}", style = MaterialTheme.typography.titleMedium)
                                session.section?.let { section ->
                                    Text("Section: $section")
                                }
                                Text("Code: ${session.code}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                if (session.endTime > 0L) {
                                    val endTimeFormatted = try {
                                        java.text.SimpleDateFormat("HH:mm:ss, dd MMM yyyy", java.util.Locale.getDefault())
                                            .format(java.util.Date(session.endTime))
                                    } catch (e: Exception) {
                                        "Invalid date"
                                    }
                                    Text("Ends at: $endTimeFormatted", style = MaterialTheme.typography.bodySmall)
                                }
                                if (session.startTime > 0L && session.endTime > session.startTime) {
                                    val durationMin = ((session.endTime - session.startTime) / 60000).toInt()
                                    Text("Duration: $durationMin min", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Status: ${session.status}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when (session.status) {
                                        "ACTIVE" -> Color(0xFF4CAF50)
                                        "DRAFT" -> Color(0xFFFF9800)
                                        "ENDED" -> Color(0xFF757575)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Code: ${session.code} - ${if (session.status == "ACTIVE") "Ready for students" else session.status}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when (session.status) {
                                            "ACTIVE" -> Color(0xFF4CAF50)
                                            "DRAFT" -> Color(0xFFFF9800)
                                            "ENDED" -> Color(0xFF757575)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { sessionToDelete = session }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Session")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDurationDialog != null) {
        AlertDialog(
            onDismissRequest = { showDurationDialog = null },
            title = { Text("Select Session Duration") },
            text = {
                Column {
                    durationOptions.forEach { min ->
                        Button(
                            onClick = {
                                // Start quiz session with duration
                                viewModel.startQuizSession(showDurationDialog!!, min)
                                showDurationDialog = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("$min minutes")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDuration,
                        onValueChange = { customDuration = it.filter { c -> c.isDigit() } },
                        label = { Text("Custom duration (min)", color = Color.Black) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val min = customDuration.toIntOrNull()
                            if (min != null && min > 0) {
                                viewModel.startQuizSession(showDurationDialog!!, min)
                                showDurationDialog = null
                                customDuration = ""
                            }
                        },
                        enabled = customDuration.isNotBlank() && customDuration.toIntOrNull() != null && customDuration.toInt() > 0,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Start with custom duration")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDurationDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteQuizSession(sessionToDelete!!.sessionId)
                    sessionToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
