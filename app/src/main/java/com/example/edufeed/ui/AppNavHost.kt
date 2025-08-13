package com.example.edufeed.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.edufeed.ui.teacher.TeacherDashboardScreen
import com.example.edufeed.ui.teacher.SessionCreateScreen
import com.example.edufeed.ui.teacher.QuestionBankScreen
import com.example.edufeed.ui.teacher.SessionListScreen
import com.example.edufeed.ui.teacher.SessionResultsScreen
import com.example.edufeed.ui.teacher.AnalyticsScreen
import com.example.edufeed.ui.teacher.TeacherQuizDashboardScreen
import com.example.edufeed.ui.teacher.TeacherFeedbackDashboardScreen
import com.example.edufeed.ui.teacher.QuizCreateScreen
import com.example.edufeed.ui.teacher.QuizQuestionBankScreen
import com.example.edufeed.ui.teacher.QuizSessionListScreen
import com.example.edufeed.ui.teacher.QuizAnalyticsScreen
import com.example.edufeed.ui.student.StudentDashboardScreen
import com.example.edufeed.ui.student.FeedbackScreen
import com.example.edufeed.ui.student.QuizJoinTakeScreen
import com.example.edufeed.ui.student.FeedbackJoinScreen
import com.example.edufeed.ui.student.StudentQuizResultsScreen
import com.example.edufeed.ui.student.StudentFeedbackResultsScreen
import com.example.edufeed.ui.auth.LoginScreen
import com.example.edufeed.ui.auth.RegisterScreen
import com.example.edufeed.viewmodel.TeacherViewModel
import com.example.edufeed.viewmodel.StudentViewModel
import com.example.edufeed.viewmodel.AuthViewModel
import com.example.edufeed.ui.HomeScreen
import com.example.edufeed.data.models.User
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.edufeed.viewmodel.QuizViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.edufeed.ui.teacher.TeacherQuizAnalyticsScreen
import com.example.edufeed.ui.teacher.CreateRandomQuizScreen
import com.example.edufeed.ui.teacher.FeedbackQuestionBankScreen
import com.example.edufeed.ui.teacher.FeedbackQuestionEditScreen
import com.example.edufeed.ui.teacher.FeedbackSessionListScreen
import com.example.edufeed.ui.teacher.FeedbackSessionEditScreen
import com.example.edufeed.ui.teacher.FeedbackSessionResultsScreen
import com.example.edufeed.ui.teacher.FeedbackAnalyticsScreen
import com.example.edufeed.ui.student.StudentFeedbackScreen
import com.example.edufeed.ui.teacher.TeacherQuizAnalyticsScreen
import com.example.edufeed.ui.teacher.CreateRandomQuizScreen
import com.example.edufeed.navigation.QuizNavigation
import com.example.edufeed.ui.student.SecureQuizScreen
import com.example.edufeed.ui.debug.SecureQuizTestScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController = navController) }
        composable("welcome") { 
            com.example.edufeed.ui.auth.WelcomeScreen(navController = navController) 
        }
        composable("register") { 
            com.example.edufeed.ui.auth.RegisterScreen(navController = navController) 
        }
        composable("login") { LoginScreen(navController = navController) }
        composable("teacher_dashboard/{teacherId}/{teacherName}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            val teacherName = backStackEntry.arguments?.getString("teacherName") ?: "Teacher"
            TeacherDashboardScreen(
                navController = navController,
                teacherId = teacherId,
                teacherName = teacherName,
                authViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            )
        }

        // Teacher Dashboard Routes
        composable("teacher_quiz_dashboard/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            TeacherQuizDashboardScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        composable("teacher_feedback_dashboard/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            TeacherFeedbackDashboardScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        // Quiz Management Routes
        composable("teacher_create_quiz/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            QuizCreateScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        composable("teacher_quiz_question_bank/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            QuizQuestionBankScreen(
                teacherId = teacherId,
                navController = navController
            )
        }
        
        composable("teacher_create_random_quiz/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            CreateRandomQuizScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        composable("teacher_quiz_sessions/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            QuizSessionListScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        composable("teacher_quiz_analytics/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            QuizAnalyticsScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        composable("teacher_quiz_results/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val quizViewModel: QuizViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            TeacherQuizAnalyticsScreen(
                quizId = sessionId,
                navController = navController,
                quizViewModel = quizViewModel
            )
        }

        // Student Quiz Routes
        composable("student_quiz_join/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val quizViewModel: QuizViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            QuizJoinTakeScreen(
                navController = navController,
                quizViewModel = quizViewModel
            )
        }
        
        // Student Feedback Join Route
        composable("student_feedback_join/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentViewModel: StudentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            FeedbackJoinScreen(
                navController = navController,
                studentId = studentId,
                viewModel = studentViewModel
            )
        }
        
        // Student Quiz Results Route
        composable("student_quiz_results/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentQuizResultsScreen(
                navController = navController,
                studentId = studentId
            )
        }
        
        // Student Feedback Results Route
        composable("student_feedback_results/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentFeedbackResultsScreen(
                navController = navController,
                studentId = studentId
            )
        }
        
        composable("student_quiz_take/{quizId}/{studentId}") { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            
            // Use the secure quiz flow
            QuizNavigation(
                quizId = quizId,
                onQuizCompleted = {
                    // Navigate back to student dashboard with quiz results
                    navController.navigate("student_quiz_results/$studentId") {
                        popUpTo("student_quiz_take/{quizId}/{studentId}") { inclusive = true }
                    }
                }
            )
        }
        
        // New route for secure quiz with just quiz ID (for direct navigation)
        composable("secure_quiz/{quizId}") { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            
            QuizNavigation(
                quizId = quizId,
                onQuizCompleted = {
                    // Navigate back to the test screen when quiz is completed
                    navController.navigate("secure_quiz_test") {
                        popUpTo("secure_quiz_test") { inclusive = true }
                    }
                }
            )
        }
        
        // Test screen for secure quiz flow
        composable("secure_quiz_test") {
            SecureQuizTestScreen(navController = navController)
        }
        
        // Feedback Management Routes
        composable("teacher_create_feedback_session/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            val teacherViewModel: TeacherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            SessionCreateScreen(
                viewModel = teacherViewModel,
                teacherId = teacherId,
                uiState = teacherViewModel.uiState.collectAsState().value,
                onSessionCreated = {
                    navController.navigate("teacher_feedback_sessions/$teacherId")
                }
            )
        }
        
        composable("teacher_feedback_question_bank/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            FeedbackQuestionBankScreen(
                teacherId = teacherId,
                navController = navController
            )
        }
        
        composable("feedback_question_edit/{questionId}/{teacherId}") { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            FeedbackQuestionEditScreen(
                questionId = questionId,
                teacherId = teacherId,
                navController = navController
            )
        }
        
        // Feedback Session Management
        composable("teacher_feedback_sessions/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            FeedbackSessionListScreen(
                teacherId = teacherId,
                navController = navController
            )
        }
        
        composable("feedback_session_edit/{sessionId}/{teacherId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            FeedbackSessionEditScreen(
                sessionId = sessionId,
                teacherId = teacherId,
                navController = navController
            )
        }
        
        composable("feedback_session_results/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            FeedbackSessionResultsScreen(
                sessionId = sessionId,
                navController = navController
            )
        }
        

        
        composable("teacher_feedback_analytics/{teacherId}") { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            FeedbackAnalyticsScreen(
                teacherId = teacherId,
                navController = navController
            )
        }
        
        composable("student_dashboard/{studentId}/{section}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val section = backStackEntry.arguments?.getString("section") ?: ""
            val studentViewModel: StudentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            StudentDashboardScreen(
                navController = navController,
                studentId = studentId,
                section = section,
                viewModel = studentViewModel,
                authViewModel = authViewModel
            )
        }
        
        composable("student_feedback/{sessionId}/{studentId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentFeedbackScreen(
                sessionId = sessionId,
                studentId = studentId,
                navController = navController
            )
        }
        
        composable("teacher_session_results/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionResultsScreen(sessionId = sessionId, navBack = { navController.popBackStack() })
        }
    }
} 