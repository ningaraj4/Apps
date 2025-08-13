package com.example.edufeed.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edufeed.components.SecureTextField
import com.example.edufeed.data.models.QuestionType
import com.example.edufeed.data.models.QuizQuestion
import com.example.edufeed.utils.SecureExamModeHandler
import com.example.edufeed.viewmodel.QuizViewModel
import com.example.edufeed.viewmodel.QuizUiState
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureQuizScreen(
    quizId: String,
    viewModel: QuizViewModel = viewModel(),
    onQuizSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSubmitDialog by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(30.minutes) } // Default 30 minutes
    
    // Enable secure exam mode
    SecureExamModeHandler(isEnabled = true) { violationReason ->
        // Handle security violation
        viewModel.onSecurityViolation(violationReason)
    }

    // Load quiz data
    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
        // Start timer updates
        viewModel.timeRemaining.collectLatest { remaining ->
            timeRemaining = remaining
            if (remaining.isNegative()) {
                // Auto-submit when time is up
                viewModel.submitQuiz()
                onQuizSubmitted()
            }
        }
    }

    // Navigate when results are ready
    LaunchedEffect(uiState) {
        if (uiState is QuizUiState.Results) {
            onQuizSubmitted()
        }
    }

    // Show loading state
    if (uiState is QuizUiState.Loading || uiState is QuizUiState.Idle) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error state
    if (uiState is QuizUiState.Error) {
        val error = (uiState as QuizUiState.Error).message
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    // Main quiz content
    val loaded = uiState as QuizUiState.Loaded

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quiz header with timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quiz: ${loaded.quiz?.title ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Countdown timer
            CountdownTimer(
                timeRemaining = timeRemaining,
                onTimeUp = { viewModel.submitQuiz() },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = if (timeRemaining.inWholeMinutes < 5) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Questions list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(loaded.questions) { index, question ->
                QuestionItem(
                    question = question,
                    questionNumber = index + 1,
                    onAnswerChange = { answer ->
                        viewModel.updateAnswer(question.questionId, answer)
                    },
                    isSecureMode = true
                )
            }
        }

        // Submit button
        Button(
            onClick = { showSubmitDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !(loaded.isSubmitting)
        ) {
            if (loaded.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submitting...")
            } else {
                Text("Submit Quiz")
            }
        }
    }

    // Submit confirmation dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Quiz") },
            text = { Text("Are you sure you want to submit your quiz? You won't be able to make changes after submitting.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitQuiz()
                        onQuizSubmitted()
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSubmitDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuestionItem(
    question: QuizQuestion,
    questionNumber: Int,
    onAnswerChange: (String) -> Unit,
    isSecureMode: Boolean = false
) {
    var answer by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Question text
            Text(
                text = "$questionNumber. ${question.questionText}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Answer input based on question type
            when (question.type) {
                QuestionType.MCQ -> {
                    question.options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = answer == option,
                                onClick = {
                                    answer = option
                                    onAnswerChange(option)
                                }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                QuestionType.ONE_WORD -> {
                    SecureTextField(
                        value = answer,
                        onValueChange = {
                            answer = it
                            onAnswerChange(it)
                        },
                        label = { Text("Your answer") },
                        singleLine = true,
                        secureMode = isSecureMode
                    )
                }
                QuestionType.MULTIPLE_CORRECT -> {
                    // For multiple correct answers
                    question.options.forEach { option ->
                        var isSelected by remember { mutableStateOf(false) }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    isSelected = checked
                                    // Update answer with all selected options
                                    val currentAnswers = answer.split(",").toMutableSet()
                                    if (checked) {
                                        currentAnswers.add(option)
                                    } else {
                                        currentAnswers.remove(option)
                                    }
                                    answer = currentAnswers.filter { it.isNotBlank() }.joinToString(",")
                                    onAnswerChange(answer)
                                }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    listOf("True", "False").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = answer == option,
                                onClick = {
                                    answer = option
                                    onAnswerChange(option)
                                }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownTimer(
    timeRemaining: Duration,
    onTimeUp: () -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    val minutes = timeRemaining.inWholeMinutes
    val seconds = timeRemaining.inWholeSeconds % 60
    
    LaunchedEffect(timeRemaining) {
        if (timeRemaining.isNegative() || timeRemaining == Duration.ZERO) {
            onTimeUp()
        }
    }
    
    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        style = textStyle,
        fontWeight = FontWeight.Bold
    )
}
