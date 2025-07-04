package com.example.feedbackapp.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feedbackapp.ui.teacher.TeacherDashboardScreen
import com.example.feedbackapp.ui.teacher.SessionCreateScreen
import com.example.feedbackapp.ui.teacher.QuestionBankScreen
import com.example.feedbackapp.ui.teacher.SessionListScreen
import com.example.feedbackapp.ui.teacher.SessionResultsScreen
import com.example.feedbackapp.ui.teacher.AnalyticsScreen
import com.example.feedbackapp.ui.student.StudentDashboardScreen
import com.example.feedbackapp.ui.student.FeedbackScreen
import com.example.feedbackapp.viewmodel.TeacherViewModel
import com.example.feedbackapp.viewmodel.StudentViewModel
import com.example.feedbackapp.viewmodel.AuthViewModel
import com.example.feedbackapp.ui.HomeScreen
import com.example.feedbackapp.data.models.User
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState

@Composable
fun AppNavHost(navController: NavHostController) {
    val teacherViewModel: TeacherViewModel = viewModel()
    val studentViewModel: StudentViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    var user by remember { mutableStateOf<User?>(null) }
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }
        composable("login") {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable(
            "teacher_dashboard/{teacherId}/{teacherName}",
            arguments = listOf(
                navArgument("teacherId") { type = NavType.StringType },
                navArgument("teacherName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            val teacherName = backStackEntry.arguments?.getString("teacherName") ?: "Teacher"
            TeacherDashboardScreen(
                navController = navController,
                teacherId = teacherId,
                teacherName = teacherName,
                authViewModel = authViewModel
            )
        }
        composable(
            "teacher_create_session/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            val teacherCreateViewModel: TeacherViewModel = viewModel(key = "SessionCreateScreen_$teacherId")
            val uiState by teacherCreateViewModel.uiState.collectAsState()
            SessionCreateScreen(
                viewModel = teacherCreateViewModel,
                teacherId = teacherId,
                uiState = uiState,
                onSessionCreated = {
                    navController.navigate("teacher_sessions/$teacherId") {
                        popUpTo("teacher_dashboard/$teacherId") { inclusive = false }
                    }
                    teacherCreateViewModel.getQuestionBank(teacherId)
                }
            )
        }
        composable(
            "teacher_question_bank/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            QuestionBankScreen(
                teacherId = teacherId,
                navBack = { navController.popBackStack() },
                viewModel = teacherViewModel
            )
        }
        composable(
            "teacher_sessions/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            SessionListScreen(
                teacherId = teacherId,
                onStartSession = { sessionId, durationMin ->
                    teacherViewModel.startSession(sessionId, durationMinutes = durationMin)
                },
                onViewResults = { sessionId ->
                    navController.navigate("teacher_session_results/$sessionId")
                },
                viewModel = teacherViewModel
            )
        }
        composable(
            "student_dashboard/{studentId}/{section}",
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType },
                navArgument("section") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val section = backStackEntry.arguments?.getString("section") ?: ""
            StudentDashboardScreen(
                navController = navController,
                studentId = studentId,
                section = section,
                viewModel = studentViewModel,
                authViewModel = authViewModel
            )
        }
        composable(
            "student_feedback/{sessionId}/{studentId}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("studentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            FeedbackScreen(
                sessionId = sessionId,
                studentId = studentId,
                navBack = { navController.popBackStack() },
                viewModel = studentViewModel
            )
        }
        composable(
            "teacher_session_results/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionResultsScreen(sessionId = sessionId, navBack = { navController.popBackStack() })
        }
        composable(
            "teacher_analytics/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            AnalyticsScreen(
                navBack = { navController.popBackStack() },
                teacherId = teacherId
            )
        }
    }
} 