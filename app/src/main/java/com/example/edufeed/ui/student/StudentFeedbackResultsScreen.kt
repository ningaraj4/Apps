package com.example.edufeed.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Feedback
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

data class FeedbackResult(
    val sessionId: String,
    val title: String,
    val completedAt: Long,
    val responses: Map<String, String>,
    val canViewResponses: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFeedbackResultsScreen(
    navController: NavController,
    studentId: String
) {
    var feedbackResults by remember { mutableStateOf<List<FeedbackResult>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studentId) {
        try {
            val db = FirebaseFirestore.getInstance()
            // Fetch actual feedback results from Firebase
            val querySnapshot = db.collection("FeedbackResponses")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            
            val results = mutableListOf<FeedbackResult>()
            for (document in querySnapshot.documents) {
                val sessionId = document.getString("sessionId") ?: ""
                val title = document.getString("sessionTitle") ?: "Feedback Session"
                val submittedAt = document.getLong("submittedAt") ?: System.currentTimeMillis()
                val responses = document.get("responses") as? Map<String, String> ?: emptyMap()
                
                results.add(
                    FeedbackResult(
                        sessionId = sessionId,
                        title = title,
                        completedAt = submittedAt,
                        responses = responses,
                        canViewResponses = true // For now, allow viewing responses
                    )
                )
            }
            
            feedbackResults = results
            loading = false
        } catch (e: Exception) {
            error = "Failed to load feedback results: ${e.message}"
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
                    text = "Feedback Results",
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
            } else if (feedbackResults.isEmpty()) {
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
                                imageVector = Icons.Default.Feedback,
                                contentDescription = "No Results",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No Feedback Results Yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete a feedback session to see your responses here",
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
                    items(feedbackResults) { result ->
                        FeedbackResultCard(
                            result = result,
                            accentColor = accentColor,
                            onViewResponses = {
                                if (result.canViewResponses) {
                                    navController.navigate("feedback_responses/${result.sessionId}/$studentId")
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
fun FeedbackResultCard(
    result: FeedbackResult,
    accentColor: Color,
    onViewResponses: () -> Unit
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
                
                // Status indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ Submitted",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Response count
            Text(
                text = "Responses: ${result.responses.size} questions answered",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
            
            if (result.canViewResponses) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onViewResponses,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "View My Responses",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "⏳ Responses will be available after the feedback session ends",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF59E0B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}