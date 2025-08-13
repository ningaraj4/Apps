package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.edufeed.R
import com.example.edufeed.data.models.FeedbackQuestion
import com.example.edufeed.data.models.FeedbackQuestionType
import com.example.edufeed.viewmodel.FeedbackViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackQuestionEditScreen(
    questionId: String,
    teacherId: String,
    navController: NavController,
    viewModel: FeedbackViewModel = viewModel()
) {
    val isNewQuestion = questionId == "new"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collect UI state
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    
    // Form state
    var questionText by remember { mutableStateOf("") }
    var questionType by remember { mutableStateOf(FeedbackQuestionType.RATING) }
    var isRequired by remember { mutableStateOf(true) }
    var section by remember { mutableStateOf("General") }
    var options by remember { mutableStateOf(listOf("")) }
    
    // Load question data if editing
    LaunchedEffect(questionId, questions) {
        if (!isNewQuestion) {
            val question = questions.find { it.questionId == questionId }
            question?.let {
                questionText = it.questionText
                questionType = it.type
                isRequired = it.isRequired
                section = it.section
                options = if (it.options.isNotEmpty()) it.options else listOf("")
            } ?: run {
                // If question not found, navigate back
                navController.popBackStack()
            }
        }
    }
    
    // Show error message if any
    LaunchedEffect(error) {
        error?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    withDismissAction = true
                )
            }
        }
    }
    
    // Handle save result
    LaunchedEffect(Unit) {
        viewModel.questions.collect { questions ->
            if (isNewQuestion && questions.any { it.questionText == questionText && it.questionId != questionId }) {
                // Question was saved successfully, navigate back
                navController.popBackStack()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewQuestion) "New Question" else "Edit Question") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (validateForm(questionText, questionType, options)) {
                        val question = FeedbackQuestion(
                            questionId = if (isNewQuestion) "fq_${System.currentTimeMillis()}" else questionId,
                            questionText = questionText.trim(),
                            type = questionType,
                            options = if (questionType == FeedbackQuestionType.MULTIPLE_CHOICE) 
                                options.filter { it.isNotBlank() } 
                            else 
                                emptyList(),
                            isRequired = isRequired,
                            section = section.ifBlank { "" },
                            createdBy = teacherId,
                            createdAt = if (isNewQuestion) System.currentTimeMillis() else 0
                        )
                        
                        scope.launch {
                            if (isNewQuestion) {
                                viewModel.createFeedbackQuestion(question)
                            } else {
                                viewModel.updateFeedbackQuestion(question)
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Please fill in all required fields",
                                withDismissAction = true
                            )
                        }
                    }
                },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save") },
                modifier = Modifier.padding(16.dp)
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
            // Question Text
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Text *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                isError = questionText.isBlank()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Question Type
            Text(
                text = "Question Type *",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(Modifier.selectableGroup()) {
                FeedbackQuestionType.values().forEach { type ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (type == questionType),
                                onClick = { questionType = type }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (type == questionType),
                            onClick = { questionType = type }
                        )
                        Text(
                            text = when (type) {
                                FeedbackQuestionType.RATING -> "Rating (1-5)"
                                FeedbackQuestionType.TEXT -> "Text Response"
                                FeedbackQuestionType.MULTIPLE_CHOICE -> "Multiple Choice"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Options for multiple choice
            if (questionType == FeedbackQuestionType.MULTIPLE_CHOICE) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Options *",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newValue ->
                                    options = options.toMutableList().apply {
                                        set(index, newValue)
                                    }
                                },
                                label = { Text("Option ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            IconButton(
                                onClick = {
                                    if (options.size > 1) {
                                        options = options.toMutableList().apply { removeAt(index) }
                                    }
                                },
                                enabled = options.size > 1
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove option",
                                    tint = if (options.size > 1) MaterialTheme.colorScheme.error 
                                          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            options = options.toMutableList().apply { add("") }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Option")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section
            OutlinedTextField(
                value = section,
                onValueChange = { section = it },
                label = { Text("Section") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Required toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = isRequired,
                    onCheckedChange = { isRequired = it },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Required")
            }
            
            // Add some bottom padding for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun validateForm(
    questionText: String,
    questionType: FeedbackQuestionType,
    options: List<String>
): Boolean {
    if (questionText.isBlank()) return false
    
    return when (questionType) {
        FeedbackQuestionType.MULTIPLE_CHOICE -> {
            val validOptions = options.filter { it.isNotBlank() }
            validOptions.size >= 2 && validOptions.size == options.size
        }
        else -> true
    }
}

@Composable
private fun FeedbackQuestionEditScreen(
    questionText: String,
    onQuestionTextChange: (String) -> Unit,
    questionType: FeedbackQuestionType,
    onQuestionTypeChange: (FeedbackQuestionType) -> Unit,
    isRequired: Boolean,
    onRequiredChange: (Boolean) -> Unit,
    section: String,
    onSectionChange: (String) -> Unit,
    options: List<String>,
    onOptionsChange: (List<String>) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    isNewQuestion: Boolean,
    isLoading: Boolean,
    error: String?
) {
    // Implementation of the preview composable
    // This is just a placeholder for the actual implementation
    // that would be used in the preview
}
