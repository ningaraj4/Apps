package com.example.edufeed.ui.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.edufeed.ui.student.QuizJoinTakeScreen

@Composable
fun SecureQuizTestScreen(
    navController: NavController,
    testQuizId: String = "test_secure_quiz_123"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Secure Quiz Flow Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = {
                // Navigate to the secure quiz flow directly
                navController.navigate("secure_quiz/$testQuizId")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Start Secure Quiz")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                // Navigate to the legacy quiz flow for comparison
                navController.navigate("student_quiz_take/$testQuizId/student_123")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Start Legacy Quiz Flow")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Test Quiz ID: $testQuizId",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSecureQuizTestScreen() {
    val navController = androidx.navigation.compose.rememberNavController()
    MaterialTheme {
        SecureQuizTestScreen(navController = navController)
    }
}
