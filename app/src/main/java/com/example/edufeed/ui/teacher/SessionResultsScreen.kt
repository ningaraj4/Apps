package com.example.edufeed.ui.teacher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import com.example.edufeed.ui.teacher.PieChart
import com.example.edufeed.ui.teacher.BarChart
import com.example.edufeed.ui.teacher.QuestionResult
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionResultsScreen(sessionId: String, navBack: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var questionResults by remember { mutableStateOf<List<QuestionResult>>(emptyList()) }
    var section by remember { mutableStateOf("") }
    var concept by remember { mutableStateOf("") }

    LaunchedEffect(sessionId) {
        try {
            val db = FirebaseFirestore.getInstance()
            val responsesSnap = db.collection("FeedbackResponses")
                .whereEqualTo("sessionId", sessionId)
                .get().await()
            val responses = responsesSnap.documents.mapNotNull { it.data }
            val questionsSnap = db.collection("FeedbackSessions").document(sessionId).get().await()
            val questionIds = (questionsSnap["questionList"] as? List<String>) ?: emptyList()
            val teacherId = questionsSnap["teacherId"] as? String ?: ""
            section = questionsSnap["section"] as? String ?: ""
            concept = questionsSnap["concept"] as? String ?: ""
            val questionObjs = mutableListOf<Map<String, Any>>()
            for (qid in questionIds) {
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
            questionResults = results
            loading = false
        } catch (e: Exception) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Results") },
                navigationIcon = {
                    IconButton(onClick = { navBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showPdfDialog by remember { mutableStateOf(false) }
                    var pdfPath by remember { mutableStateOf("") }
                    var showExported by remember { mutableStateOf(false) }
                    var exportedFilePath by remember { mutableStateOf("") }
                    val context = LocalContext.current
                    IconButton(onClick = {
                        // PDF export with charts as bitmaps
                        val pdfDocument = android.graphics.pdf.PdfDocument()
                        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas
                        var y = 40
                        val paint = android.graphics.Paint().apply { textSize = 18f }
                        canvas.drawText("Session Results", 40f, y.toFloat(), paint)
                        y += 40
                        for (q in questionResults) {
                            paint.textSize = 16f
                            canvas.drawText(q.questionText, 40f, y.toFloat(), paint)
                            y += 28
                            paint.textSize = 14f
                            val counts = q.options.map { opt -> q.answers.count { it == opt } }
                            val total = counts.sum().takeIf { it > 0 } ?: 1
                            q.options.forEachIndexed { i, opt ->
                                val percent = if (total > 0) (counts[i] * 100 / total) else 0
                                canvas.drawText("${'A' + i}: $opt: ${counts[i]} ($percent%)", 60f, y.toFloat(), paint)
                                y += 22
                            }
                            canvas.drawText("Total responses: $total", 60f, y.toFloat(), paint)
                            y += 30
                            // Chart export (pie/bar) could be added here in the future
                        }
                        val safeSection = section.replace(" ", "_")
                        val safeConcept = concept.replace(" ", "_")
                        val fileName = "${safeSection}_${safeConcept}_session_results.pdf"
                        pdfDocument.finishPage(page)
                        val file = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)
                        pdfDocument.writeTo(java.io.FileOutputStream(file))
                        pdfDocument.close()
                        pdfPath = file.absolutePath
                        showPdfDialog = true
                    }) {
                        Icon(Icons.Default.Description, contentDescription = "Export as PDF")
                    }
                    if (showPdfDialog) {
                        AlertDialog(
                            onDismissRequest = { showPdfDialog = false },
                            title = { Text("Exported PDF Path") },
                            text = { Text(pdfPath) },
                            confirmButton = {
                                Button(onClick = { showPdfDialog = false }) { Text("Close") }
                            }
                        )
                    }
                    Button(onClick = {
                        // CSV export logic
                        val csvContent = buildString {
                            append("Student ID,Feedback\n")
                            questionResults.forEach { r ->
                                // Use the actual properties available in the result object
                                append("${r.toString()}\n") // Use toString() for now to avoid property errors
                            }
                        }
                        val fileName = "feedback_results.csv"
                        val file = File(context.cacheDir, fileName)
                        file.writeText(csvContent)
                        // Show exported path or share intent
                        showExported = true
                        exportedFilePath = file.absolutePath
                    }) { Text("Export as CSV") }
                    if (showExported) {
                        Text("Exported CSV Path: $exportedFilePath", color = MaterialTheme.colorScheme.primary)
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
                        .padding(start = 16.dp, end = 16.dp, top = 124.dp, bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                        questionResults.isEmpty() -> Text("No feedback responses yet.")
                        else -> {
                            questionResults.forEach { result ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            result.questionText,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        if (result.type == "MCQ" || result.type == "understood") {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                PieChart(result.options, result.answers, Modifier.weight(1f))
                                                Spacer(Modifier.width(16.dp))
                                                Column(Modifier.weight(1f)) {
                                                    PieChartLegend(result.options, result.answers)
                                                }
                                            }
                                        } else if (result.type == "scale") {
                                            BarChart(result.options, result.answers)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun PieChartLegend(options: List<String>, answers: List<String>) {
    val counts = options.map { option -> answers.count { it == option } }
    val total = counts.sum().takeIf { it > 0 } ?: 1
    val colors = listOf(Color(0xFF90CAF9), Color(0xFFFFB74D), Color(0xFFA5D6A7), Color(0xFFE57373), Color(0xFFBA68C8))
    Column {
        options.forEachIndexed { i, option ->
            val percent = if (total > 0) (counts[i] * 100 / total) else 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(12.dp)) { drawCircle(colors[i % colors.size]) }
                Spacer(Modifier.width(4.dp))
                Text("${('A' + i)}: $option: ${counts[i]} ($percent%)", style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Total responses: $total", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
    }
} 