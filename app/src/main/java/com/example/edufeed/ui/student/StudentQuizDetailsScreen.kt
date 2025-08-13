package com.example.edufeed.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edufeed.data.models.QuizQuestion
import com.example.edufeed.data.models.QuizResponse
import com.example.edufeed.viewmodel.QuizViewModel
import com.example.edufeed.viewmodel.QuizUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edufeed.data.models.QuestionType

@Composable
fun StudentQuizDetailsScreen(
    quizId: String,
    viewModel: QuizViewModel = viewModel(),
    onBackToDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Ensure quiz is loaded; results state is produced by SecureQuizScreen submit
    LaunchedEffect(quizId) {
        // No-op here; caller should navigate here after submission
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quiz title and score
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = CenterHorizontally
            ) {
                Text(
                    text = when (val state = uiState) {
                        is QuizUiState.Results -> state.quiz?.title ?: "Quiz Results"
                        is QuizUiState.Loaded -> state.quiz?.title ?: "Quiz Results"
                        else -> "Quiz Results"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Score circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Column(horizontalAlignment = CenterHorizontally) {
                        Text(
                            text = when (val s = uiState) {
                                is QuizUiState.Results -> "${s.score}%"
                                else -> "--%"
                            },
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Performance metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val metrics = when (val s = uiState) {
                        is QuizUiState.Results -> Triple(s.correctAnswers, s.incorrectAnswers, s.unansweredQuestions)
                        else -> Triple(0, 0, 0)
                    }
                    MetricItem(
                        value = metrics.first,
                        label = "Correct",
                        color = MaterialTheme.colorScheme.primary
                    )
                    MetricItem(
                        value = metrics.second,
                        label = "Incorrect",
                        color = MaterialTheme.colorScheme.error
                    )
                    MetricItem(
                        value = metrics.third,
                        label = "Unanswered",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question review section
        Text(
            text = "Question Review",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val state = uiState) {
                is QuizUiState.Results -> {
                    items(state.questions) { question ->
                        val response = state.responses.find { it.questionId == question.questionId }
                        QuestionReviewItem(
                            question = question,
                            userAnswer = response?.selectedAnswer ?: "",
                            isCorrect = response?.isCorrect ?: false
                        )
                    }
                }
                is QuizUiState.Loaded -> {
                    items(state.questions) { question ->
                        val response = state.responses[question.questionId]
                        QuestionReviewItem(
                            question = question,
                            userAnswer = response?.selectedAnswer ?: "",
                            isCorrect = response?.isCorrect ?: false
                        )
                    }
                }
                else -> {}
            }
        }
        
        // Back to dashboard button
        Button(
            onClick = onBackToDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun MetricItem(value: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionReviewItem(
    question: QuizQuestion,
    userAnswer: String,
    isCorrect: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Question text
            Text(
                text = question.questionText ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // User's answer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Correct" else "Incorrect",
                    tint = if (isCorrect) MaterialTheme.colorScheme.primary 
                          else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Your answer: $userAnswer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Show correct answer if the user was wrong
            if (!isCorrect) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Correct answer: ${question.correctAnswer ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Explanation section removed as it's not part of the QuizQuestion model
        }
    }
}
