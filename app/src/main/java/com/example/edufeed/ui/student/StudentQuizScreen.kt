package com.example.edufeed.ui.student

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log
import androidx.compose.ui.text.input.KeyboardType
import com.example.edufeed.ui.components.NoCopyPasteTextField
import com.example.edufeed.data.models.QuestionType
import com.example.edufeed.utils.enableKioskMode
import com.example.edufeed.utils.setScreenshotPrevention
import com.example.edufeed.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizScreen(
    navController: NavController,
    studentId: String,
    sessionCode: String,
    quizViewModel: QuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // Quiz state
    var timeLeft by remember { mutableStateOf(0) }
    var started by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var showWarning by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }
    
    // Quiz data
    val questions by quizViewModel.questions.collectAsState()
    var answers by remember { mutableStateOf(mutableMapOf<String, String>()) }
    
    // Format time as MM:SS
    val formattedTime = remember(timeLeft) {
        String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)
    }

    // Apply security measures when quiz starts
    LaunchedEffect(started) {
        if (started && activity != null) {
            // Enable security features
            setScreenshotPrevention(activity, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    enableKioskMode(activity)
                } catch (e: SecurityException) {
                    warningMessage = "Full screen mode not available: ${e.message}"
                    showWarning = true
                }
            }
        } else if (activity != null) {
            // Clean up security features
            setScreenshotPrevention(activity, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    activity.stopLockTask()
                } catch (e: Exception) {
                    // Ignore if already not in lock task mode
                }
            }
        }
    }

    // Timer logic
    LaunchedEffect(started) {
        if (started) {
            timeLeft = (quizViewModel.quiz.value?.duration ?: 30) * 60 // Convert minutes to seconds
            while (timeLeft > 0 && started) {
                delay(1000)
                timeLeft--
            }
            
            if (timeLeft <= 0) {
                // Auto-submit when time's up
                submitting = true
                quizViewModel.submitQuiz(studentId, sessionCode, answers)
                // Clean up security
                activity?.let { setScreenshotPrevention(it, false) }
                navController.popBackStack()
            }
        }
    }

    // Show warning dialog if needed
    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("Warning") },
            text = { Text(warningMessage) },
            confirmButton = {
                Button(onClick = { showWarning = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Quiz header with timer and navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    // Show confirmation before leaving
                    warningMessage = "Are you sure you want to leave? Your progress will be lost."
                    showWarning = true
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            if (started) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Quiz content
        if (!started) {
            // Quiz start screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Quiz Instructions",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Quiz instructions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Text("• This quiz contains ${questions.size} questions")
                    Text("• Time limit: ${quizViewModel.quiz.value?.duration ?: 30} minutes")
                    Text("• No switching between apps is allowed")
                    Text("• Screenshots are disabled")
                    Text("• Copy/paste is disabled")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Start quiz button
                Button(
                    onClick = { started = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Start Quiz", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            // Quiz in progress
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                items(questions) { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Question text
                            Text(
                                text = "Q${questions.indexOf(question) + 1}. ${question.questionText}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Answer input
                            NoCopyPasteTextField(
                                value = answers[question.questionId] ?: "",
                                onValueChange = { answers[question.questionId] = it },
                                label = { Text("Your answer") },
                                modifier = Modifier.fillMaxWidth(),
                                isEnabled = !submitting,
                                keyboardType = when (question.type) {
                                    QuestionType.MCQ -> KeyboardType.Text
                                    QuestionType.ONE_WORD -> KeyboardType.Text
                                    QuestionType.TRUE_FALSE -> KeyboardType.Text
                                    QuestionType.MULTIPLE_CORRECT -> KeyboardType.Text
                                    else -> KeyboardType.Text
                                },
                                maxLength = when (question.type) {
                                    QuestionType.ONE_WORD -> 50
                                    QuestionType.MULTIPLE_CORRECT -> 100
                                    else -> Int.MAX_VALUE
                                },
                                placeholder = { Text("Type your answer here...") }
                            )
                        }
                    }
                }
                
                // Submit button
                item {
                    Button(
                        onClick = {
                            submitting = true
                            quizViewModel.submitQuiz(studentId, sessionCode, answers)
                            // Clean up security
                            activity?.let { setScreenshotPrevention(it, false) }
                            navController.popBackStack()
                        },
                        enabled = !submitting && answers.size == questions.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(56.dp)
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Submit Quiz", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
