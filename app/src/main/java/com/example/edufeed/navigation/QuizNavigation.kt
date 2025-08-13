package com.example.edufeed.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edufeed.ui.student.StudentQuizDetailsScreen
import com.example.edufeed.ui.student.SecureQuizScreen
import com.example.edufeed.viewmodel.QuizViewModel
import com.example.edufeed.viewmodel.QuizUiState

/**
 * Handles the navigation between quiz screens (quiz taking and results)
 */
@Composable
fun QuizNavigation(
    quizId: String,
    onQuizCompleted: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load the quiz when the navigation starts
    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }
    
    // Navigate to results when the quiz is submitted
    LaunchedEffect(uiState) {
        if (uiState is QuizUiState.Results) {
            // Quiz was submitted, stay on results screen
            return@LaunchedEffect
        }
    }
    
    when (val currentState = uiState) {
        is QuizUiState.Loaded -> {
            SecureQuizScreen(
                quizId = quizId,
                onQuizSubmitted = { viewModel.submitQuiz() }
            )
        }
        is QuizUiState.Results -> {
            StudentQuizDetailsScreen(
                quizId = quizId,
                onBackToDashboard = onQuizCompleted
            )
        }
        is QuizUiState.Error -> {
            // Show error state
            ErrorState(
                message = currentState.message,
                onRetry = { viewModel.loadQuiz(quizId) },
                onBack = onQuizCompleted
            )
        }
        QuizUiState.Loading -> {
            // Show loading state
            FullScreenLoading()
        }
        QuizUiState.Idle -> {
            // Show loading state while waiting for data
            FullScreenLoading()
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back to Dashboard")
            }
            
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}


