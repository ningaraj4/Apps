package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edufeed.viewmodel.QuizViewModel
import com.example.edufeed.data.models.QuizQuestion
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.edufeed.ui.components.NoCopyPasteTextField
import androidx.navigation.NavController
import com.example.edufeed.data.models.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionBankScreen(
    teacherId: String,
    navController: NavController,
    viewModel: QuizViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var editQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var questionText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MCQ") }
    var options by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var marks by remember { mutableStateOf("1") }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var questionBank by remember { mutableStateOf(listOf<QuizQuestion>()) }
    val questions by viewModel.questions.collectAsState()
    var questionTextError by remember { mutableStateOf<String?>(null) }
    var optionsError by remember { mutableStateOf<String?>(null) }
    var correctAnswerError by remember { mutableStateOf<String?>(null) }
    var marksError by remember { mutableStateOf<String?>(null) }

    // Fetch question bank on first composition
    LaunchedEffect(teacherId) {
        viewModel.syncQuestionsForQuizFromRemote("quiz_bank_$teacherId")
    }

    // Handle questions state
    LaunchedEffect(questions) {
        questionBank = questions
    }

    fun validateInputs(): Boolean {
        var valid = true
        questionTextError = null
        optionsError = null
        correctAnswerError = null
        marksError = null
        
        if (questionText.isBlank()) {
            questionTextError = "Question text required"
            valid = false
        }
        if (type == "MCQ" && options.split(",").map { it.trim() }.filter { it.isNotBlank() }.size < 2) {
            optionsError = "At least 2 options required for MCQ"
            valid = false
        }
        if (correctAnswer.isBlank()) {
            correctAnswerError = "Correct answer required"
            valid = false
        }
        if (marks.toIntOrNull() == null || marks.toInt() !in 1..2) {
            marksError = "Marks must be 1 or 2"
            valid = false
        }
        return valid
    }

    if (showError != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { showError = null }) { Text("Dismiss") } }
            ) { Text(showError ?: "") }
        }
    }

    if (showSuccess) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { showSuccess = false }) { Text("OK") } }
            ) { Text("Question saved successfully!") }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE4CFF8), Color(0xFFA084E8)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF5E60CE)
    val cardColor = Color.White
    val textColor = Color(0xFF1A1A1A)
    val hintColor = Color(0xFF808080)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title and subtitles
            Spacer(Modifier.height(32.dp))
            Text(
                "Quiz Question Bank Management",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = accentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(360.dp))
            Text(
                "Add quiz questions to your question bank",
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
        }
        // Floating Action Button (FAB) at bottom right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 0.dp, 24.dp, 96.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    editQuestion = null
                    questionText = ""
                    type = "MCQ"
                    options = ""
                    correctAnswer = ""
                    marks = "1"
                    showDialog = true
                },
                containerColor = accentColor,
                contentColor = Color.White
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }
        }
        // Add Question Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editQuestion == null) "Add Quiz Question" else "Edit Quiz Question", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            label = { Text("Question Text", color = MaterialTheme.colorScheme.primary) },
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            isError = questionTextError != null,
                            supportingText = { if (questionTextError != null) Text(questionTextError!!, color = Color.Red) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Type:",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = type == "MCQ",
                                    onClick = { type = "MCQ" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "MCQ",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = type == "OneWord",
                                    onClick = { type = "OneWord" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "One Word",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        if (type == "MCQ") {
                            OutlinedTextField(
                                value = options,
                                onValueChange = { options = it },
                                label = { Text("Options (comma separated)", color = MaterialTheme.colorScheme.primary) },
                                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                                isError = optionsError != null,
                                supportingText = { if (optionsError != null) Text(optionsError!!, color = Color.Red) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            label = { Text("Correct Answer", color = MaterialTheme.colorScheme.primary) },
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            isError = correctAnswerError != null,
                            supportingText = { if (correctAnswerError != null) Text(correctAnswerError!!, color = Color.Red) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = marks,
                            onValueChange = { marks = it },
                            label = { Text("Marks (1 or 2)", color = MaterialTheme.colorScheme.primary) },
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            isError = marksError != null,
                            supportingText = { if (marksError != null) Text(marksError!!, color = Color.Red) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                val q = QuizQuestion(
                                    questionId = editQuestion?.questionId ?: "${System.currentTimeMillis()}",
                                    quizId = "quiz_bank_$teacherId",
                                    questionText = questionText,
                                    type = QuestionType.valueOf(type.uppercase()),
                                    options = if (type == "MCQ") options.split(",").map { it.trim() } else emptyList(),
                                    correctAnswer = correctAnswer,
                                    marks = marks.toInt()
                                )
                                viewModel.createQuizQuestion("quiz_bank_$teacherId", q)
                                showSuccess = true
                                showDialog = false
                                editQuestion = null
                                questionText = ""
                                type = "MCQ"
                                options = ""
                                correctAnswer = ""
                                marks = "1"
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(if (editQuestion == null) "Add" else "Update", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
