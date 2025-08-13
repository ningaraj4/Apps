package com.example.edufeed.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class QuizResult(
    val quizId: String,
    val title: String,
    val score: Int,
    val totalMarks: Int,
    val completedAt: Long,
    val canViewAnswers: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizResultsScreen(
    navController: NavController,
    studentId: String
) {
    var quizResults by remember { mutableStateOf<List<QuizResult>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studentId) {
        try {
            val db = FirebaseFirestore.getInstance()
            // Fetch actual quiz results from Firebase
            val querySnapshot = db.collection("QuizResponses")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            
            val results = mutableListOf<QuizResult>()
            for (document in querySnapshot.documents) {
                val quizId = document.getString("quizId") ?: ""
                val score = document.getLong("score")?.toInt() ?: 0
                val totalMarks = document.getLong("totalMarks")?.toInt() ?: 100
                val submittedAt = document.getLong("submittedAt") ?: System.currentTimeMillis()
                val title = document.getString("quizTitle") ?: "Quiz"
                
                results.add(
                    QuizResult(
                        quizId = quizId,
                        title = title,
                        score = score,
                        totalMarks = totalMarks,
                        completedAt = submittedAt,
                        canViewAnswers = true // For now, allow viewing answers
                    )
                )
            }
            
            quizResults = results
            loading = false
        } catch (e: Exception) {
            error = "Failed to load quiz results: ${e.message}"
            loading = false
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE0ECFF), Color(0xFF90CAF9)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF3F51B5)
    val cardColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = accentColor
                    )
                }
                Text(
                    text = "Quiz Results",
                    style = MaterialTheme.typography.headlineSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = error ?: "",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (quizResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = "No Results",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No Quiz Results Yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete a quiz to see your results here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(quizResults) { result ->
                        QuizResultCard(
                            result = result,
                            accentColor = accentColor,
                            onViewAnswers = {
                                if (result.canViewAnswers) {
                                    navController.navigate("quiz_answers/${result.quizId}/$studentId")
                                }
                            }
                        )
                    }
                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultCard(
    result: QuizResult,
    accentColor: Color,
    onViewAnswers: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Score display
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.score >= result.totalMarks * 0.8) Color(0xFF10B981)
                        else if (result.score >= result.totalMarks * 0.6) Color(0xFFF59E0B)
                        else Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${result.score}/${result.totalMarks}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Percentage and grade
            val percentage = (result.score.toFloat() / result.totalMarks * 100).toInt()
            val grade = when {
                percentage >= 90 -> "A+"
                percentage >= 80 -> "A"
                percentage >= 70 -> "B+"
                percentage >= 60 -> "B"
                percentage >= 50 -> "C"
                else -> "F"
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Percentage: $percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Grade: $grade",
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (result.canViewAnswers) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onViewAnswers,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "View Answers",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "⏳ Answers will be available after the quiz session ends",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF59E0B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}