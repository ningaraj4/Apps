package com.example.edufeed.ui.student

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.edufeed.data.models.QuestionType
import com.example.edufeed.data.models.QuizQuestion
import com.example.edufeed.data.models.QuizResponse
import com.example.edufeed.data.models.QuizSession
import com.example.edufeed.navigation.QuizNavigation
import com.example.edufeed.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizJoinTakeScreen(
    navController: NavController,
    quizCode: String? = null,
    quizViewModel: QuizViewModel = viewModel(),
    // New parameter to enable secure quiz flow
    useSecureFlow: Boolean = true
) {
    // If secure flow is enabled and we have a quiz code, use the secure quiz flow
    if (useSecureFlow && !quizCode.isNullOrBlank()) {
        QuizNavigation(
            quizId = quizCode,
            onQuizCompleted = {
                // Navigate back to student dashboard with quiz results
                navController.navigate("student_quiz_results/$quizCode") {
                    popUpTo("student_quiz_take/{quizId}/{studentId}") { inclusive = true }
                }
            }
        )
        return
    }
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

    // State variables
    var code by remember { mutableStateOf(quizCode ?: "") }
    var loading by remember { mutableStateOf(false) }
    var joined by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(0) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var quizCompleted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var sessionCode by remember { mutableStateOf("") }

    // Observe questions from ViewModel
    val questions by quizViewModel.questions.collectAsState()

    // Function to join quiz - SIMPLIFIED VERSION
    fun joinQuizWithCode(enteredCode: String) {
        if (enteredCode.length != 6) {
            showError = "Please enter a valid 6-digit code"
            return
        }

        loading = true
        showError = null
        sessionCode = enteredCode

        // Test code - works immediately
        if (enteredCode == "123456") {
            val testQuestions = listOf(
                QuizQuestion(
                    questionId = "test_q1",
                    quizId = "test_quiz",
                    questionText = "What is 2 + 2?",
                    type = QuestionType.MCQ,
                    options = listOf("3", "4", "5", "6"),
                    correctAnswer = "4",
                    marks = 1
                ),
                QuizQuestion(
                    questionId = "test_q2",
                    quizId = "test_quiz",
                    questionText = "What is the capital of France?",
                    type = QuestionType.MCQ,
                    options = listOf("London", "Paris", "Berlin", "Madrid"),
                    correctAnswer = "Paris",
                    marks = 1
                ),
                QuizQuestion(
                    questionId = "test_q3",
                    quizId = "test_quiz",
                    questionText = "Which programming language is used for Android development?",
                    type = QuestionType.MCQ,
                    options = listOf("Python", "Java", "Kotlin", "Both Java and Kotlin"),
                    correctAnswer = "Both Java and Kotlin",
                    marks = 1
                )
            )

            quizViewModel.setTestQuestions(testQuestions)
            joined = true
            timeLeft = 30 * 60 // 30 minutes
            started = true
            loading = false
            return
        }

        // Real faculty codes - find session and load questions
        quizViewModel.findQuizSessionByCode(enteredCode) { session ->
            if (session != null) {
                // Accept both ACTIVE and DRAFT sessions
                if (session.status == "ACTIVE" || session.status == "DRAFT") {
                    // Create default questions for any real session
                    val defaultQuestions = listOf(
                        QuizQuestion(
                            questionId = "real_q1",
                            quizId = "quiz_bank_${session.teacherId}",
                            questionText = "What is 2 + 2?",
                            type = QuestionType.MCQ,
                            options = listOf("3", "4", "5", "6"),
                            correctAnswer = "4",
                            marks = 1
                        ),
                        QuizQuestion(
                            questionId = "real_q2",
                            quizId = "quiz_bank_${session.teacherId}",
                            questionText = "What is the capital of France?",
                            type = QuestionType.MCQ,
                            options = listOf("London", "Paris", "Berlin", "Madrid"),
                            correctAnswer = "Paris",
                            marks = 1
                        )
                    )

                    quizViewModel.setTestQuestions(defaultQuestions)
                    joined = true
                    timeLeft = if (session.duration > 0) session.duration * 60 else 30 * 60
                    started = true
                    loading = false
                } else {
                    showError = "Quiz session is not active. Status: ${session.status}"
                    loading = false
                }
            } else {
                showError = "Invalid quiz session code. Please check the code and try again."
                loading = false
            }
        }
    }

    // Function to submit quiz
    fun submitQuiz() {
        if (questions.isEmpty()) return

        var correctAnswers = 0
        questions.forEach { question ->
            val userAnswer = selectedAnswers[question.questionId]
            if (userAnswer == question.correctAnswer) {
                correctAnswers++
            }
        }

        score = correctAnswers
        quizCompleted = true

        // Save responses to database (one per question)
        questions.forEach { question ->
            val userAnswer = selectedAnswers[question.questionId] ?: ""
            val questionScore = if (userAnswer == question.correctAnswer) 1 else 0
            
            val response = QuizResponse(
                responseId = "response_${question.questionId}_${System.currentTimeMillis()}",
                quizId = question.quizId,
                studentId = "student_${System.currentTimeMillis()}",
                questionId = question.questionId,
                selectedAnswer = userAnswer,
                score = questionScore,
                submittedAt = System.currentTimeMillis()
            )
            quizViewModel.submitQuizResponse(response)
        }

        // Disable security features
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                activity?.stopLockTask()
            } catch (e: Exception) {
                // Ignore if not in lock task mode
            }
        }
    }

    // Timer effect
    LaunchedEffect(started, timeLeft) {
        if (started && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (started && timeLeft <= 0) {
            // Time's up - auto submit
            submitQuiz()
        }
    }

    // Auto-join if code provided
    LaunchedEffect(quizCode) {
        if (!quizCode.isNullOrEmpty() && !joined) {
            joinQuizWithCode(quizCode)
        }
    }

    // UI Colors
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), Color(0xFF1976D2)),
        startY = 0f,
        endY = 1200f
    )
    val accentColor = Color(0xFF1976D2)
    val cardColor = Color.White
    val textColor = Color(0xFF1A1A1A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        if (loading) {
            // Loading screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = accentColor)
                        Spacer(Modifier.height(16.dp))
                        Text("Searching for session with code: $sessionCode")
                    }
                }
            }
        } else if (quizCompleted) {
            // Results screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Quiz Completed!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Your Score: $score/${questions.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Percentage: ${(score * 100 / questions.size)}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Back to Dashboard", color = Color.White)
                        }
                    }
                }
            }
        } else if (joined && started && questions.isNotEmpty()) {
            // Quiz screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Timer and progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Question ${currentQuestionIndex + 1}/${questions.size}",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            "Time: ${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 300) Color.Red else accentColor
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Current question
                if (currentQuestionIndex < questions.size) {
                    val question = questions[currentQuestionIndex]
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                question.questionText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            question.options.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedAnswers[question.questionId] == option,
                                        onClick = {
                                            selectedAnswers[question.questionId] = option
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentQuestionIndex > 0) {
                        Button(
                            onClick = { currentQuestionIndex-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Previous", color = Color.White)
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (currentQuestionIndex < questions.size - 1) {
                        Button(
                            onClick = { currentQuestionIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Next", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { submitQuiz() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Quiz", color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Join screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Join Quiz Session",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            "Enter the 6-digit code provided by your teacher",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = code,
                            onValueChange = { if (it.length <= 6) code = it },
                            label = { Text("Quiz Code", color = accentColor) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(
                                color = textColor,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                                focusedLabelColor = accentColor,
                                unfocusedLabelColor = accentColor.copy(alpha = 0.7f),
                                cursorColor = accentColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Button(
                            onClick = { joinQuizWithCode(code) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = code.length == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Join Quiz",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        if (showError != null) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                showError!!,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            "💡 Try code '123456' for test mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}