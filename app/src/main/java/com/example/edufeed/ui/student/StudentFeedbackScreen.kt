package com.example.edufeed.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
// Removed unused import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.edufeed.data.models.FeedbackQuestion
import com.example.edufeed.data.models.FeedbackQuestionType
import com.example.edufeed.data.models.FeedbackResponse
import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.viewmodel.FeedbackViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFeedbackScreen(
    sessionId: String,
    studentId: String,
    navController: NavController,
    viewModel: FeedbackViewModel = viewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    val submitSuccess by viewModel.submitSuccess.collectAsStateWithLifecycle(initialValue = false)
    
    val responses = remember { mutableStateMapOf<String, String>() }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load session and questions when screen is first displayed
    LaunchedEffect(sessionId) {
        viewModel.loadFeedbackSession(sessionId)
        viewModel.loadFeedbackQuestionsForSession(sessionId)
    }
    
    // Handle error messages
    LaunchedEffect(error) {
        error?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    withDismissAction = true
                )
            }
        }
    }
    
    // Handle successful submission
    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Feedback submitted successfully!",
                    withDismissAction = true
                )
                // Navigate back after a short delay
                kotlinx.coroutines.delay(1500)
                navController.popBackStack()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.concept ?: "Feedback") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    .verticalScroll(scrollState)
            ) {
                // Session info
                SessionInfoCard(session!!)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Questions
                questions.filter { question ->
                    question.questionId in (session?.questionList ?: emptyList())
                }.forEach { question ->
                    FeedbackQuestionItem(
                        question = question,
                        response = responses[question.questionId] ?: "",
                        onResponseChange = { response ->
                            responses[question.questionId] = response
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Submit button
                Button(
                    onClick = {
                        val feedbackResponses = responses.map { (questionId, answer) ->
                            FeedbackResponse(
                                responseId = "${sessionId}_${studentId}_$questionId",
                                sessionId = sessionId,
                                studentId = studentId,
                                questionId = questionId,
                                answer = answer,
                                submittedAt = System.currentTimeMillis()
                            )
                        }
                        
                        // Validate required questions
                        val missingRequired = questions.any { question ->
                            question.isRequired == true && 
                            question.questionId in (session?.questionList ?: emptyList()) &&
                            (responses[question.questionId].isNullOrBlank())
                        }
                        
                        if (missingRequired) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please answer all required questions",
                                    withDismissAction = true
                                )
                            }
                        } else {
                            viewModel.submitFeedbackResponses(
                                sessionId = sessionId,
                                studentId = studentId,
                                responses = feedbackResponses
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Submit Feedback")
                }
                
                // Add some bottom padding
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SessionInfoCard(session: com.example.edufeed.data.models.FeedbackSession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.concept,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (session.section.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Section: ${session.section}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Code: ${session.code}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedbackQuestionItem(
    question: FeedbackQuestion,
    response: String,
    onResponseChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Question text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Required indicator
            if (question.isRequired) {
                Text(
                    text = "Required",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Response input based on question type
            when (question.type) {
                FeedbackQuestionType.RATING -> {
                    // Star rating input
                    var rating by remember { mutableIntStateOf(response.toIntOrNull() ?: 0) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..5).forEach { value ->
                            Icon(
                                imageVector = if (value <= rating) 
                                    Icons.Default.Star 
                                else 
                                    Icons.Default.StarBorder,
                                contentDescription = "$value stars",
                                tint = if (value <= rating) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        rating = if (rating == value) 0 else value
                                        onResponseChange(rating.toString())
                                    }
                            )
                        }
                    }
                    
                    // Rating labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 - Poor", style = MaterialTheme.typography.labelSmall)
                        Text("5 - Excellent", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                FeedbackQuestionType.MULTIPLE_CHOICE -> {
                    // Radio buttons for multiple choice
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        question.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onResponseChange(option)
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = (response == option),
                                    onClick = { onResponseChange(option) }
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                FeedbackQuestionType.TEXT -> {
                    // Text input
                    OutlinedTextField(
                        value = response,
                        onValueChange = onResponseChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your response here...") },
                        maxLines = 4
                    )
                }
            }
        }
    }
}
