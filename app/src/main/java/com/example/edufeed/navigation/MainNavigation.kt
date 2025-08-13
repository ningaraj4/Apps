package com.example.edufeed.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.edufeed.ui.LoginScreen
import com.example.edufeed.ui.RegisterScreen
import com.example.edufeed.ui.student.StudentDashboardScreen
import com.example.edufeed.ui.teacher.TeacherDashboardScreen
import com.example.edufeed.viewmodel.AuthViewModel
import com.example.edufeed.viewmodel.AuthUiState
import com.example.edufeed.viewmodel.StudentViewModel

/**
 * Main navigation graph for the application
 */
@Composable
fun MainNavigation(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable("splash") {
            // Check auth state and navigate accordingly
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthUiState.Success -> {
                        // User is logged in; let role-based navigation happen inside dashboards/screens
                        navController.navigate("student_dashboard") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    is AuthUiState.Idle, is AuthUiState.Error -> {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    is AuthUiState.Loading -> { /* stay on splash until resolved */ }
                }
            }
        }

        // Auth Screens
        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        // Student Screens
        composable("student_dashboard") {
            val user = authViewModel.getCurrentUser()
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("student_dashboard") { inclusive = true }
                    }
                }
            } else {
                val studentViewModel: StudentViewModel = viewModel()
                val studentId = user.uid
                val section = "" // TODO: fetch actual section if required
                StudentDashboardScreen(
                    navController = navController,
                    studentId = studentId,
                    section = section,
                    viewModel = studentViewModel,
                    authViewModel = authViewModel
                )
            }
        }

        // Teacher Screens
        composable("teacher_dashboard") {
            val user = authViewModel.getCurrentUser()
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("teacher_dashboard") { inclusive = true }
                    }
                }
            } else {
                TeacherDashboardScreen(
                    navController = navController,
                    teacherId = user.uid,
                    authViewModel = authViewModel
                )
            }
        }

        // Quiz Flow
        composable(
            route = "quiz/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            QuizNavigation(
                quizId = quizId,
                onQuizCompleted = {
                    // Navigate back to dashboard when quiz is completed
                    navController.popBackStack()
                }
            )
        }
    }
}
