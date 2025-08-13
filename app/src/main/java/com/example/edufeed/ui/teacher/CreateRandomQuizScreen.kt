package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.edufeed.data.models.QuestionType
import com.example.edufeed.data.models.Quiz
import com.example.edufeed.viewmodel.QuizViewModel

// Generate a random 6-character code
private fun generateRandomCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6)
        .map { chars.random() }
        .joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRandomQuizScreen(
    teacherId: String,
    navController: NavController,
    viewModel: QuizViewModel = viewModel()
) {
    // Form state
    var title by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var questionCount by remember { mutableStateOf("10") }
    var duration by remember { mutableStateOf("30") }
    
    // Question type filters
    val questionTypes = QuestionType.values().toList()
    val selectedQuestionTypes = remember { mutableStateListOf<QuestionType>() }
    
    // Marks filters
    val marksOptions = listOf(1, 2)
    val selectedMarks = remember { mutableStateListOf<Int>() }
    
    // Question banks
    val questionBanks by viewModel.questions.collectAsStateWithLifecycle()
    var selectedQuestionBank by remember { mutableStateOf<String?>(null) }
    
    // Loading and error states
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Load question banks on first composition
    LaunchedEffect(Unit) {
        viewModel.syncAllQuizzesFromRemote()
    }
    
    // Handle form submission
    fun onSubmit() {
        // Validate form
        if (title.isBlank() || section.isBlank() || questionCount.toIntOrNull() == null || 
            duration.toIntOrNull() == null || selectedQuestionBank == null) {
            showError = "Please fill in all required fields"
            return
        }
        
        if (selectedQuestionTypes.isEmpty()) {
            showError = "Please select at least one question type"
            return
        }
        
        if (selectedMarks.isEmpty()) {
            showError = "Please select at least one mark value"
            return
        }
        
        isLoading = true
        
        // Create the quiz with random questions
        val quiz = Quiz(
            quizId = "", // Will be set by the ViewModel
            title = title,
            sectionId = section,
            createdBy = teacherId,
            duration = duration.toInt(),
            code = generateRandomCode(),
            createdAt = System.currentTimeMillis()
        )
        
        viewModel.createQuizWithRandomQuestions(
            quiz = quiz,
            questionCount = questionCount.toInt(),
            questionBankId = selectedQuestionBank!!,
            questionTypes = selectedQuestionTypes.map { it.name },
            marksFilter = selectedMarks
        )
        
        isLoading = false
        showSuccess = true
        navController.popBackStack()
    }
    

    
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Random Quiz") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Basic Info Section
            Text(
                text = "Quiz Details",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Quiz Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = section,
                onValueChange = { section = it },
                label = { Text("Section/Class *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = questionCount,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) questionCount = it },
                    label = { Text("Questions *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) duration = it },
                    label = { Text("Minutes *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            
            // Question Bank Selection
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Question Bank *",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (questionBanks.isEmpty()) {
                Text(
                    text = "No question banks found. Please create a question bank first.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                questionBanks.forEach { bank ->
                    SelectableChip(
                        text = bank.questionText,
                        isSelected = selectedQuestionBank == bank.quizId,
                        onSelect = { selectedQuestionBank = bank.quizId },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            // Question Type Filter
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Question Types *",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            questionTypes.forEach { type ->
                FilterChip(
                    selected = selectedQuestionTypes.contains(type),
                    onClick = { 
                        if (selectedQuestionTypes.contains(type)) {
                            selectedQuestionTypes.remove(type)
                        } else {
                            selectedQuestionTypes.add(type)
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) {
                    Text(type.name)
                }
            }
            
            // Marks Filter
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Marks *",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row {
                marksOptions.forEach { mark ->
                    FilterChip(
                        selected = selectedMarks.contains(mark),
                        onClick = { 
                            if (selectedMarks.contains(mark)) {
                                selectedMarks.remove(mark)
                            } else {
                                selectedMarks.add(mark)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("$mark Mark${if (mark > 1) "s" else ""}")
                    }
                }
            }
            
            // Submit Button
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { onSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Quiz", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            // Error message
            showError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
               else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.Checkbox
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = content,
        modifier = modifier
    )
}
