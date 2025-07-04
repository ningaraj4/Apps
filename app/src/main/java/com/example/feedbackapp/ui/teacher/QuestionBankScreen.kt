package com.example.feedbackapp.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feedbackapp.viewmodel.TeacherViewModel
import com.example.feedbackapp.viewmodel.TeacherUiState
import com.example.feedbackapp.data.models.Question
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    teacherId: String,
    navBack: () -> Unit,
    viewModel: TeacherViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var editQuestion by remember { mutableStateOf<Question?>(null) }
    var questionText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MCQ") }
    var options by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var questionBank by remember { mutableStateOf(listOf<Question>()) }
    val uiState by viewModel.uiState.collectAsState()
    var questionTextError by remember { mutableStateOf<String?>(null) }
    var optionsError by remember { mutableStateOf<String?>(null) }

    // Fetch question bank on first composition
    LaunchedEffect(teacherId) {
        viewModel.getQuestionBank(teacherId)
    }

    // Handle UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is TeacherUiState.QuestionBank -> {
                questionBank = (uiState as TeacherUiState.QuestionBank).questions
            }
            is TeacherUiState.Success -> {
                showSuccess = true
                viewModel.getQuestionBank(teacherId)
                showDialog = false
                editQuestion = null
                questionText = ""
                type = "MCQ"
                options = ""
            }
            is TeacherUiState.Error -> {
                showError = (uiState as TeacherUiState.Error).message
            }
            else -> {}
        }
    }

    fun validateInputs(): Boolean {
        var valid = true
        questionTextError = null
        optionsError = null
        if (questionText.isBlank()) {
            questionTextError = "Question text required"
            valid = false
        }
        if ((type == "MCQ" || type == "scale") && options.split(",").map { it.trim() }.filter { it.isNotBlank() }.size < 2) {
            optionsError = "At least 2 options required"
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
                "Question Bank Management",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = accentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(360.dp))
            Text(
                "Add questions to your question bank",
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
//            Text(
//                "Students will see the options when answering",
//                color = hintColor,
//                fontSize = 14.sp,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
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
                title = { Text(if (editQuestion == null) "Add Question" else "Edit Question", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                                    selected = type == "scale",
                                    onClick = { type = "scale" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Scale",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = type == "understood",
                                    onClick = { type = "understood" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Understood",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
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
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                val q = Question(
                                    questionId = editQuestion?.questionId ?: "",
                                    questionText = questionText,
                                    type = type,
                                    options = if (type == "MCQ" || type == "scale") options.split(",").map { it.trim() } else emptyList()
                                )
                                viewModel.addOrUpdateQuestion(teacherId, q)
                            }
                        },
                        enabled = uiState !is TeacherUiState.Loading,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        if (uiState is TeacherUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
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