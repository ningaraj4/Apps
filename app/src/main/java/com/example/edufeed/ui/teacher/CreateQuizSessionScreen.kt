package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edufeed.data.models.QuizQuestion
import com.example.edufeed.viewmodel.QuizViewModel
import com.example.edufeed.ui.SectionListProvider
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizSessionScreen(
    navController: NavController,
    teacherId: String,
    viewModel: QuizViewModel = viewModel()
) {
    var concept by remember { mutableStateOf("") }
    val sectionOptions = SectionListProvider.sectionOptions
    var selectedSection by remember { mutableStateOf(sectionOptions.first()) }
    var selectedQuestions by remember { mutableStateOf<Set<String>>(setOf()) }
    var showError by remember { mutableStateOf<String?>(null) }
    var conceptError by remember { mutableStateOf<String?>(null) }
    var sectionError by remember { mutableStateOf<String?>(null) }
    var questionsError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var sessionCreated by remember { mutableStateOf<Pair<String, String>?>(null) }
    val questions by viewModel.questions.collectAsState()

    // UI Colors and Gradients
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF3E5F5), Color(0xFF7C3AED)),
        startY = 0f,
        endY = 1200f
    )
    val accentColor = Color(0xFF7C3AED)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)

    // Load questions from question bank
    LaunchedEffect(teacherId) {
        viewModel.syncQuestionsForQuizFromRemote("quiz_bank_$teacherId")
    }

    fun validateInputs(): Boolean {
        var valid = true
        conceptError = null
        sectionError = null
        questionsError = null

        if (concept.isBlank()) {
            conceptError = "Quiz topic is required"
            valid = false
        }
        if (selectedSection.isBlank()) {
            sectionError = "Section selection is required"
            valid = false
        }
        if (selectedQuestions.isEmpty()) {
            questionsError = "At least one question must be selected"
            valid = false
        }
        return valid
    }

    fun createQuizSession() {
        if (validateInputs()) {
            isCreating = true
            // Call the ViewModel's createQuizSession method
            viewModel.createQuizSession(
                teacherId, 
                selectedSection, 
                concept, 
                selectedQuestions.toList()
            ) { sessionInfo ->
                showSuccess = true
                sessionCreated = sessionInfo
                isCreating = false
            }
        }
    }

    if (showError != null) {
        LaunchedEffect(showError) {
            kotlinx.coroutines.delay(3000)
            showError = null
        }
    }

    if (showSuccess) {
        LaunchedEffect(showSuccess) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
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
                    Text("Quiz Session Created Successfully!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text("Section: $selectedSection", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Share this 6-digit code with your students:")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        sessionCreated!!.second, 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            navController.navigate("teacher_quiz_sessions/$teacherId")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("View Quiz Sessions", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = accentColor
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Create Quiz Session",
                        style = MaterialTheme.typography.headlineMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Quiz Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Quiz Topic
                    OutlinedTextField(
                        value = concept,
                        onValueChange = { 
                            concept = it
                            conceptError = null
                        },
                        label = { Text("Quiz Topic/Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = conceptError != null,
                        supportingText = conceptError?.let { { Text(it, color = Color.Red) } },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Section Selection
                    Text(
                        text = "Select Section",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSection,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Section") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = sectionError != null,
                            supportingText = sectionError?.let { { Text(it, color = Color.Red) } },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sectionOptions.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text(section) },
                                    onClick = {
                                        selectedSection = section
                                        sectionError = null
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Questions Selection Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Questions",
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedQuestions.size} selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    questionsError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (questions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📝",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "No Questions Available",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Add questions to your question bank first",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(questions) { question ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedQuestions.contains(question.questionId))
                                            accentColor.copy(alpha = 0.1f) else Color(0xFFF8F9FA)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedQuestions.contains(question.questionId),
                                            onCheckedChange = { checked ->
                                                selectedQuestions = if (checked) {
                                                    selectedQuestions + question.questionId
                                                } else {
                                                    selectedQuestions - question.questionId
                                                }
                                                questionsError = null
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = accentColor
                                            )
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = question.questionText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColor,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${question.type} • ${question.marks} marks",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Button(
                        onClick = { createQuizSession() },
                        enabled = !isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Creating...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Create Quiz Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Success/Error Messages
        if (showError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = showError!!,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red
                    )
                }
            }
        }

        if (showSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Quiz session created successfully!",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}
