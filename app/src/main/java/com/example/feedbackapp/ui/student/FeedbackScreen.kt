package com.example.feedbackapp.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feedbackapp.data.models.FeedbackResponse
import com.example.feedbackapp.data.models.FeedbackSession
import com.example.feedbackapp.data.models.Question
import com.example.feedbackapp.viewmodel.StudentViewModel
import com.example.feedbackapp.viewmodel.StudentUiState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    sessionId: String,
    studentId: String,
    navBack: () -> Unit,
    viewModel: StudentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var session by remember { mutableStateOf<FeedbackSession?>(null) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var answers = remember { mutableStateMapOf<String, String>() }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(0L) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFB2FEFA), Color(0xFF4A90E2)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF3F51B5)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)

    // Load session/questions if not loaded
    LaunchedEffect(sessionId) {
        if (uiState !is StudentUiState.SessionLoaded) {
            // This assumes you have a way to get the student's section, which should be passed or fetched
            // For now, just call joinSession with dummy section (should be fixed in integration)
            // viewModel.joinSession(code, section)
        }
    }
    // Update session/questions from state
    LaunchedEffect(uiState) {
        when (uiState) {
            is StudentUiState.SessionLoaded -> {
                val sessionLoaded = uiState as StudentUiState.SessionLoaded
                session = sessionLoaded.session
                questions = sessionLoaded.questions
                timeLeft = ((session?.endTime ?: 0L) - System.currentTimeMillis()) / 1000
            }
            is StudentUiState.FeedbackSubmitted -> {
                showSuccess = true
                navBack()
            }
            is StudentUiState.Error -> {
                showError = (uiState as StudentUiState.Error).message
            }
            is StudentUiState.AlreadySubmitted -> {
                showError = "You have already submitted feedback for this session."
                navBack()
            }
            else -> {}
        }
    }

    // Update timeLeft every second
    LaunchedEffect(session) {
        val currentSession = session
        if (currentSession != null) {
            timeLeft = ((currentSession.endTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                timeLeft = ((currentSession.endTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            }
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
    if (showSuccess) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { showSuccess = false }) { Text("OK") } }
            ) { Text("Feedback submitted successfully!") }
        }
    }

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentSession = session
            if (currentSession != null && System.currentTimeMillis() > currentSession.endTime) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You have missed the feedback window.", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                val currentSessionForTimer = session
                if (currentSessionForTimer != null) {
                    val timeLeft = ((currentSessionForTimer.endTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                    Text("Time left: ${timeLeft}s", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(questions) { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = question.questionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                when (question.type) {
                                    "MCQ" -> {
                                        question.options.forEach { option ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = answers[question.questionId] == option,
                                                    onClick = { answers[question.questionId] = option }
                                                )
                                                Text(option)
                                            }
                                        }
                                    }
                                    "scale" -> {
                                        Row {
                                            question.options.forEach { option ->
                                                val selected = answers[question.questionId] == option
                                                Button(
                                                    onClick = { answers[question.questionId] = option },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                                    ),
                                                    modifier = Modifier.padding(2.dp)
                                                ) { Text(option) }
                                            }
                                        }
                                    }
                                    "understood" -> {
                                        Row {
                                            listOf("Yes", "Somewhat", "No").forEach { option ->
                                                val selected = answers[question.questionId] == option
                                                Button(
                                                    onClick = { answers[question.questionId] = option },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                                    ),
                                                    modifier = Modifier.padding(2.dp)
                                                ) { Text(option) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val currentSessionForSubmit = session
                        if (currentSessionForSubmit != null) {
                            val responses = questions.map {
                                FeedbackResponse(
                                    sessionId = currentSessionForSubmit.sessionId,
                                    studentId = studentId,
                                    questionId = it.questionId,
                                    answer = answers[it.questionId] ?: ""
                                )
                            }
                            viewModel.submitFeedback(responses)
                        }
                    },
                    enabled = answers.size == questions.size && uiState !is StudentUiState.Loading && (session == null || System.currentTimeMillis() <= session!!.endTime),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    if (uiState is StudentUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Submit Feedback", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
} 
 