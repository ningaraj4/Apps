package com.example.feedbackapp.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedbackapp.data.models.Question
import com.example.feedbackapp.viewmodel.TeacherUiState
import com.example.feedbackapp.viewmodel.TeacherViewModel
import com.example.feedbackapp.ui.SectionListProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration.Companion.None
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.LocalTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCreateScreen(
    viewModel: TeacherViewModel,
    teacherId: String,
    uiState: TeacherUiState,
    onSessionCreated: () -> Unit
) {
    var concept by remember { mutableStateOf("") }
    val sectionOptions = SectionListProvider.sectionOptions
    var selectedSection by remember { mutableStateOf(sectionOptions.first()) }
    var selectedQuestions by remember { mutableStateOf<Set<String>>(setOf()) }
    var showError by remember { mutableStateOf<String?>(null) }
    var conceptError by remember { mutableStateOf<String?>(null) }
    var sectionError by remember { mutableStateOf<String?>(null) }
    var questionError by remember { mutableStateOf<String?>(null) }
    var questionBank by remember { mutableStateOf<List<Question>>(emptyList()) }
    var sessionCreated by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getQuestionBank(teacherId)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TeacherUiState.QuestionBank -> {
                questionBank = uiState.questions
            }
            is TeacherUiState.SessionCreated -> {
                sessionCreated = Pair(uiState.sessionId, uiState.code)
            }
            is TeacherUiState.Error -> {
                showError = uiState.message
            }
            else -> {}
        }
    }

    fun validateInputs(): Boolean {
        var valid = true
        conceptError = null
        sectionError = null
        questionError = null
        if (concept.isBlank()) {
            conceptError = "Concept required"
            valid = false
        }
        if (selectedQuestions.isEmpty()) {
            questionError = "Select at least one question"
            valid = false
        }
        return valid
    }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null },
            title = { Text("Error") },
            text = { Text(showError ?: "") },
            confirmButton = {
                Button(onClick = { showError = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (sessionCreated != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Session Created Successfully!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text("Section: $selectedSection", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Share this code with your students:")
                    Spacer(Modifier.height(8.dp))
                    Text(sessionCreated!!.second, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onSessionCreated() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
        return
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD0BCFF), Color(0xFFA084E8)),
        startY = 0f,
        endY = 1000f
    )

    val accentColor = Color(0xFF5E60CE)
    val cardColor = Color.White
    val textColor = Color(0xFF1A1A1A)
    val hintColor = Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 72.dp)
                .navigationBarsPadding(),  // ✅ Fix: ensures bottom button is not obscured
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Create Feedback Session",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text("Concept", color = Color.Black) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        isError = conceptError != null,
                        supportingText = {
                            conceptError?.let { Text(it, color = Color.Red) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = selectedSection,
                        onValueChange = { selectedSection = it },
                        label = { Text("Section", color = Color.Black) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        isError = sectionError != null,
                        supportingText = {
                            Text("E.g. CSE-A, ECE-B, 6TH-A, 7TH-D (case insensitive)", fontSize = 12.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 420.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Select Questions:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    if (questionError != null) {
                        Text(questionError!!, color = Color.Red)
                    }
                    Spacer(Modifier.height(8.dp))

                    if (uiState is TeacherUiState.Loading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (questionBank.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No questions available. Please add questions first.")
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            questionBank.forEach { question ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedQuestions.contains(question.questionId),
                                            onCheckedChange = { checked ->
                                                selectedQuestions = if (checked)
                                                    selectedQuestions + question.questionId
                                                else
                                                    selectedQuestions - question.questionId
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(question.questionText, color = textColor)
                                            Text("Type: ${question.type}", fontSize = 13.sp, color = hintColor)
                                            if (question.options.isNotEmpty()) {
                                                Text("Options: ${question.options.joinToString(", ")}", fontSize = 13.sp, color = hintColor)
                                            }
                                        }
                                        IconButton(onClick = {
                                            questionBank = questionBank.filter { it.questionId != question.questionId }
                                            selectedQuestions = selectedQuestions - question.questionId
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp)) // ✅ Increased space above button

            Button(
                onClick = {
                    if (validateInputs()) {
                        viewModel.createSession(
                            teacherId = teacherId,
                            section = selectedSection,
                            concept = concept,
                            questionIds = selectedQuestions.toList()
                        )
                    }
                },
                enabled = uiState !is TeacherUiState.Loading && questionBank.isNotEmpty(),
                modifier = Modifier
                    .width(220.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                if (uiState is TeacherUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Create Session", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
