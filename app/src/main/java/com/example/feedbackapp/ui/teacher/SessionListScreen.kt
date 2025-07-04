package com.example.feedbackapp.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feedbackapp.viewmodel.TeacherViewModel
import com.example.feedbackapp.viewmodel.TeacherUiState
import com.example.feedbackapp.data.models.FeedbackSession
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    teacherId: String,
    onStartSession: (String, Int) -> Unit,
    onViewResults: (String) -> Unit,
    viewModel: TeacherViewModel = viewModel()
) {
    var sessions by remember { mutableStateOf(listOf<FeedbackSession>()) }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf<String?>(null) }
    var showDurationDialog by remember { mutableStateOf<String?>(null) }
    var customDuration by remember { mutableStateOf("") }
    val durationOptions = listOf(2, 5, 10)
    val uiState by viewModel.uiState.collectAsState()
    var sessionToDelete by remember { mutableStateOf<FeedbackSession?>(null) }
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

    // Fetch sessions on first composition
    LaunchedEffect(teacherId) {
        viewModel.getSessions(teacherId)
    }

    // Handle UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is TeacherUiState.Sessions -> {
                sessions = (uiState as TeacherUiState.Sessions).sessions
            }
            is TeacherUiState.Success -> {
                showSuccess = (uiState as TeacherUiState.Success).message
                // Refresh sessions after starting one
                viewModel.getSessions(teacherId)
            }
            is TeacherUiState.Error -> {
                showError = (uiState as TeacherUiState.Error).message
            }
            else -> {}
        }
    }

    if (showError != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { showError = null }) { Text("Dismiss") } }
            ) { Text(showError ?: "") }
        }
    }

    if (showSuccess != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { showSuccess = null }) { Text("OK") } }
            ) { Text(showSuccess ?: "") }
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
                text = "Session List",
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
            val displaySessions = if (selectedTab == 0) activeSessions else completedSessions

            if (uiState is TeacherUiState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    if (displaySessions.isEmpty()) {
                        item {
                            Text(
                                "No sessions found.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                color = mutedTextColor
                            )
                        }
                    } else {
                        items(displaySessions) { session ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Concept: ${session.concept}", style = MaterialTheme.typography.titleMedium)
                                    Text("Section: ${session.section}")
                                    Text("Code: ${session.code}")
                                    if (session.endTime > 0L) {
                                        val endTimeFormatted = java.text.SimpleDateFormat("HH:mm:ss, dd MMM yyyy").format(java.util.Date(session.endTime))
                                        Text("Ends at: $endTimeFormatted", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (session.startTime > 0L && session.endTime > session.startTime) {
                                        val durationMin = ((session.endTime - session.startTime) / 60000).toInt()
                                        Text("Duration: $durationMin min", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (selectedTab == 0) {
                                            if (session.status == "DRAFT") "Status: Ready to Start" else "Status: Active"
                                        } else {
                                            "Status: Completed"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            selectedTab == 0 && session.status == "DRAFT" -> MaterialTheme.colorScheme.primary
                                            selectedTab == 0 && session.status == "ACTIVE" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (selectedTab == 0) {
                                            if (session.status == "DRAFT") {
                                                Button(
                                                    onClick = { showDurationDialog = session.sessionId },
                                                    enabled = true
                                                ) {
                                                    Text("Start Session")
                                                }
                                            } else if (session.status == "ACTIVE") {
                                                Text("Session is active", color = MaterialTheme.colorScheme.secondary)
                                            }
                                        } else {
                                            Button(
                                                onClick = { onViewResults(session.sessionId) },
                                                modifier = Modifier
                                                    .widthIn(min = 120.dp, max = 200.dp)
                                                    .align(Alignment.CenterVertically),
                                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                            ) {
                                                Text("Analyze", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
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
                                onStartSession(showDurationDialog!!, min)
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
                                onStartSession(showDurationDialog!!, min)
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
                    viewModel.deleteSession(sessionToDelete!!.sessionId)
                    sessionToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancel") }
            }
        )
    }
} 