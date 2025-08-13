package com.example.edufeed.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edufeed.viewmodel.QuizViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun QuizResultScreen(
    quizId: String,
    studentId: String,
    quizViewModel: QuizViewModel = viewModel(),
    showCorrectAnswers: Boolean = false,
    quizEndTime: Long = 0L
) {
    val responses by quizViewModel.responses.collectAsState()
    val questions by quizViewModel.questions.collectAsState()
    val myResponses = responses.filter { it.studentId == studentId }
    val now = System.currentTimeMillis()
    val canShowCorrect = showCorrectAnswers && now > quizEndTime

    // Calculate score
    val totalQuestions = questions.size
    val correctAnswers = myResponses.count { response ->
        val question = questions.find { it.questionId == response.questionId }
        question?.correctAnswer == response.selectedAnswer
    }
    val scorePercentage = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0

    // UI Colors
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E8), Color(0xFF4CAF50)),
        startY = 0f,
        endY = 1200f
    )
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val successColor = Color(0xFF4CAF50)
    val errorColor = Color(0xFFE53E3E)

    LaunchedEffect(quizId) {
        quizViewModel.syncResponsesForQuizFromRemote(quizId)
        quizViewModel.syncQuestionsForQuizFromRemote(quizId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (myResponses.isNotEmpty()) {
                // Header Card with Score
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Result",
                            tint = if (scorePercentage >= 60) successColor else errorColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Quiz Results",
                            style = MaterialTheme.typography.headlineMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "$scorePercentage%",
                            style = MaterialTheme.typography.displayMedium,
                            color = if (scorePercentage >= 60) successColor else errorColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$correctAnswers out of $totalQuestions correct",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }

                // Detailed Results Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Detailed Results",
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        myResponses.forEachIndexed { index, response ->
                            val question = questions.find { it.questionId == response.questionId }
                            val isCorrect = question?.correctAnswer == response.selectedAnswer

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCorrect) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = if (isCorrect) "Correct" else "Incorrect",
                                        tint = if (isCorrect) successColor else errorColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Q${index + 1}: ${question?.questionText ?: "Question"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Your answer: ${response.selectedAnswer}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        if (canShowCorrect && !isCorrect) {
                                            Text(
                                                text = "Correct answer: ${question?.correctAnswer ?: "-"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = successColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showCorrectAnswers && !canShowCorrect) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Correct answers will be visible after the quiz time is over.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF856404),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // No Results Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "No Results",
                            tint = errorColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No Results Found",
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "We couldn't find any quiz results for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
} 