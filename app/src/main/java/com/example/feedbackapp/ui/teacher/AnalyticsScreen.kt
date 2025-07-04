package com.example.feedbackapp.ui.teacher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.feedbackapp.ui.SectionListProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import com.example.feedbackapp.ui.teacher.PieChart
import com.example.feedbackapp.ui.teacher.BarChart
import com.example.feedbackapp.ui.teacher.QuestionResult
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navBack: () -> Unit, teacherId: String) {
    if (teacherId.isBlank()) {
        Text("Error: Teacher ID is missing.", color = Color.Red, modifier = Modifier.padding(32.dp))
        return
    }
    val sectionOptions = SectionListProvider.sectionOptions
    var selectedSection by remember { mutableStateOf(sectionOptions.first()) }
    var startDate by remember { mutableStateOf(getNDaysAgo(7)) }
    var endDate by remember { mutableStateOf(Date()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var analyticsResults by remember { mutableStateOf<List<QuestionResult>>(emptyList()) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportImageRequested by remember { mutableStateOf(false) }
    var showDateError by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSection, startDate, endDate) {
        if (endDate.before(startDate)) {
            showDateError = true
            analyticsResults = emptyList()
            return@LaunchedEffect
        } else {
            showDateError = false
        }
        loading = true
        error = null
        analyticsResults = emptyList()
        try {
            val db = FirebaseFirestore.getInstance()
            val sessionsSnap = db.collection("FeedbackSessions")
                .get().await()
            val filteredSessions = sessionsSnap.documents.filter {
                val sectionValue = (it.getString("section") ?: "").lowercase()
                sectionValue == selectedSection.lowercase()
            }
            val sessionIds = filteredSessions
                .filter {
                    val start = it.getLong("startTime") ?: 0L
                    val end = it.getLong("endTime") ?: 0L
                    val inRange = start >= startDate.time && end <= endDate.time
                    inRange
                }
                .map { it.id }
            if (sessionIds.isEmpty()) {
                analyticsResults = emptyList()
                loading = false
                error = null
                return@LaunchedEffect
            }
            val responsesSnap = db.collection("FeedbackResponses")
                .whereIn("sessionId", sessionIds.take(10)) // Firestore limit workaround
                .get().await()
            val responses = responsesSnap.documents.mapNotNull { it.data }
            val allQuestionIds = sessionsSnap.documents.flatMap {
                (it["questionList"] as? List<String>) ?: emptyList()
            }.distinct()
            val questionObjs = mutableListOf<Map<String, Any>>()
            for (qid in allQuestionIds) {
                val qSnap = db.collection("Teachers").document(teacherId)
                    .collection("QuestionBank").document(qid).get().await()
                qSnap.data?.let { questionObjs.add(it) }
            }
            val results = questionObjs.map { q ->
                val qid = q["questionId"] as? String ?: ""
                val qText = q["questionText"] as? String ?: ""
                val qType = q["type"] as? String ?: "MCQ"
                val qOptions = (q["options"] as? List<String>) ?: emptyList()
                val answers = responses.filter { it["questionId"] == qid }.map { it["answer"] as? String ?: "" }
                QuestionResult(qText, qType, qOptions, answers)
            }
            analyticsResults = results
            loading = false
        } catch (e: Exception) {
            println("AnalyticsScreen Firestore error: ${e.message}")
            e.printStackTrace()
            error = e.message
            loading = false
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFB2B6FF), Color(0xFF7C3AED)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF7C3AED)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)
    val darkTextColor = Color(0xFF22223B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Section Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = backgroundGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 96.dp, bottom = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text("Section:", modifier = Modifier.padding(end = 8.dp), color = textColor)
                                OutlinedTextField(
                                    value = selectedSection,
                                    onValueChange = { selectedSection = it },
                                    label = { Text("Section", color = textColor) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFF4A90E2),
                                        unfocusedBorderColor = Color(0xFF3F51B5),
                                        focusedLabelColor = Color(0xFF4A90E2),
                                        unfocusedLabelColor = Color(0xFF3F51B5),
                                        cursorColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                DatePickerField(date = startDate, onDateChange = { startDate = it })
                                }
                                Spacer(Modifier.width(16.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                DatePickerField(date = endDate, onDateChange = { endDate = it })
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (showDateError) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Light yellow
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    "Please select a valid date range: 'From' date should be before 'To' date.",
                                    color = Color(0xFFF57C00),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (loading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                    if (error != null) {
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                    if (analyticsResults.isNotEmpty()) {
                        analyticsResults.forEach { result ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(result.questionText, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp), color = textColor)
                                    if (result.type == "MCQ" || result.type == "understood") {
                                        val counts = result.options.map { opt -> result.answers.count { it == opt } }
                                        val total = counts.sum().takeIf { it > 0 } ?: 1
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            PieChart(
                                                result.options,
                                                result.answers,
                                                // TODO: Update PieChart to draw option letter, count, and percent on each slice
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            Column {
                                                result.options.forEachIndexed { i, opt ->
                                                    val percent = if (total > 0) (counts[i] * 100 / total) else 0
                                                    Text(
                                                        text = "$opt: ${counts[i]} responses ($percent%)",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = darkTextColor,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                                Text(
                                                    "Total responses: $total",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = darkTextColor,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    } else if (result.type == "scale") {
                                        BarChart(result.options, result.answers)
                                        val counts = result.options.map { opt -> result.answers.count { it == opt } }
                                        val total = counts.sum().takeIf { it > 0 } ?: 1
                                        Column(Modifier.padding(top = 8.dp)) {
                                            result.options.forEachIndexed { i, opt ->
                                                val percent = if (total > 0) (counts[i] * 100 / total) else 0
                                                Text(
                                                    text = "$opt: ${counts[i]} responses ($percent%)",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = darkTextColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            Text(
                                                "Total responses: $total",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = darkTextColor,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!loading && !showDateError && error == null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC)), // Light blue
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    "No feedback responses found for this section and date range yet. Encourage your students to participate!",
                                    color = Color(0xFF01579B),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Section", color = MaterialTheme.colorScheme.onSurface) },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            },
            modifier = Modifier.width(100.dp)
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun DatePickerField(date: Date, onDateChange: (Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { time = date }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val darkTextColor = Color(0xFF22223B)
    OutlinedTextField(
        value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date),
        onValueChange = {},
        readOnly = true,
        label = { Text("Date", color = darkTextColor) },
        textStyle = LocalTextStyle.current.copy(color = darkTextColor),
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = darkTextColor, modifier = Modifier.clickable {
                DatePickerDialog(context, { _, y, m, d ->
                    val cal = Calendar.getInstance()
                    cal.set(y, m, d)
                    onDateChange(cal.time)
                }, year, month, day).show()
            })
        },
        modifier = Modifier.width(120.dp)
    )
}

fun getNDaysAgo(days: Int): Date {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -days)
    return cal.time
} 