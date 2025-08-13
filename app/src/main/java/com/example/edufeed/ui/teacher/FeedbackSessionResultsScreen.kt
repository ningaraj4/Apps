package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.edufeed.data.models.FeedbackQuestion
import com.example.edufeed.data.models.FeedbackResponse
import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.viewmodel.FeedbackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSessionResultsScreen(
    sessionId: String,
    navController: NavController,
    viewModel: FeedbackViewModel = viewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val responsesList by viewModel.responsesList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    
    // Load session data and responses when screen is first displayed
    LaunchedEffect(sessionId) {
        viewModel.loadFeedbackSession(sessionId)
        viewModel.loadFeedbackResponses(sessionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.title ?: "Session Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Session not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Session info
                SessionInfoCard(session!!)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Response count
                Text(
                    "${responsesList.size} ${if (responsesList.size == 1) "Response" else "Responses"}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Questions and responses
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(questions) { question ->
                        val selectedIds = session?.questionList ?: emptyList()
                        if (selectedIds.contains(question.questionId)) {
                            QuestionResponseCard(
                                question = question,
                                responses = responsesList,
                                isAnonymous = session?.isAnonymous ?: true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionInfoCard(session: FeedbackSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (session.concept.isNotBlank()) {
                Text(
                    text = session.concept,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Text(
                text = "Section: ${session.section}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Status: ${if (session.status == "ACTIVE") "Active" else session.status}",
                style = MaterialTheme.typography.bodySmall,
                color = if (session.status == "ACTIVE")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuestionResponseCard(
    question: FeedbackQuestion,
    responses: List<FeedbackResponse>,
    isAnonymous: Boolean
) {
    val questionResponses = responses
        .filter { it.questionId == question.questionId }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (question.type) {
                com.example.edufeed.data.models.FeedbackQuestionType.RATING -> {
                    // Show average rating
                    val average = questionResponses
                        .mapNotNull { it.answer.toDoubleOrNull() }
                        .average()
                        .takeIf { !it.isNaN() }
                    
                    if (average != null) {
                        Text(
                            text = "Average Rating: ${String.format("%.1f", average)}/5",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = "No ratings yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Show distribution
                    val ratingCounts = (1..5).associateWith { rating ->
                        questionResponses.count { it.answer == rating.toString() }
                    }
                    
                    RatingDistribution(ratingCounts = ratingCounts, total = questionResponses.size)
                }

                com.example.edufeed.data.models.FeedbackQuestionType.MULTIPLE_CHOICE -> {
                    // Group responses by answer
                    val answerCounts = questionResponses.groupBy { it.answer }
                        .mapValues { it.value.size }
                    
                    // Show each option with count and percentage
                    question.options.forEach { option ->
                        val count = answerCounts[option] ?: 0
                        val percentage = if (questionResponses.isNotEmpty()) 
                            (count * 100f / questionResponses.size).toInt() 
                        else 0
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(option)
                                Text("$count (${percentage}%)")
                            }
                            
                            // Progress bar
                            LinearProgressIndicator(
                                progress = percentage / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                com.example.edufeed.data.models.FeedbackQuestionType.TEXT -> {
                    // Show text responses in an expandable section
                    var expanded by remember { mutableStateOf(false) }
                    
                    Text(
                        text = "${questionResponses.size} ${if (questionResponses.size == 1) "response" else "responses"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                    
                    if (expanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            questionResponses.forEach { response ->
                                Text(
                                    text = "\u2022 ${response.answer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Show respondent info if not anonymous
            if (!isAnonymous && questionResponses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Respondents: ${questionResponses.map { it.studentId }.distinct().joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatingDistribution(
    ratingCounts: Map<Int, Int>,
    total: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Show distribution for each rating
        (5 downTo 1).forEach { rating ->
            val count = ratingCounts[rating] ?: 0
            val percentage = if (total > 0) (count * 100f / total).toInt() else 0
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = "$rating",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(24.dp)
                )
                
                // Star icon
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                
                // Progress bar
                LinearProgressIndicator(
                    progress = if (total > 0) count.toFloat() / total else 0f,
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                // Count and percentage
                Text(
                    text = "$count (${percentage}%)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}
