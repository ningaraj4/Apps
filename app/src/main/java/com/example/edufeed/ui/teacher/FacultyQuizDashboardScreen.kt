package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FacultyQuizDashboardScreen(navController: NavController, teacherId: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quiz Dashboard", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = { navController.navigate("create_quiz_session/$teacherId") }, modifier = Modifier.fillMaxWidth()) {
            Text("Create Session")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("quiz_question_bank/$teacherId") }, modifier = Modifier.fillMaxWidth()) {
            Text("Manage Question Bank")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("quiz_session_list/$teacherId") }, modifier = Modifier.fillMaxWidth()) {
            Text("View Previous Sessions")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("quiz_analytics/$teacherId") }, modifier = Modifier.fillMaxWidth()) {
            Text("View Analytics")
        }
    }
}
