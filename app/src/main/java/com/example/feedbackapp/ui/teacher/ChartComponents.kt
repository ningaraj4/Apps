package com.example.feedbackapp.ui.teacher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight

// Shared data class

data class QuestionResult(val questionText: String, val type: String, val options: List<String>, val answers: List<String>)

// Shared PieChart composable
@Composable
fun PieChart(options: List<String>, answers: List<String>, modifier: Modifier = Modifier) {
    val counts = options.map { option -> answers.count { it == option } }
    val total = counts.sum().takeIf { it > 0 } ?: 1
    val colors = listOf(Color(0xFF90CAF9), Color(0xFFFFB74D), Color(0xFFA5D6A7), Color(0xFFE57373), Color(0xFFBA68C8))
    Box(modifier.height(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(120.dp)) {
            var startAngle = 0f
            counts.forEachIndexed { i, count ->
                val sweep = 360f * count / total
                drawArc(
                    color = colors[i % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )
                startAngle += sweep
            }
        }
    }
}

// Shared BarChart composable
@Composable
fun BarChart(options: List<String>, answers: List<String>) {
    val counts = options.map { option -> answers.count { it == option } }
    val maxCount = counts.maxOrNull()?.takeIf { it > 0 } ?: 1
    val colors = listOf(Color(0xFF90CAF9), Color(0xFFFFB74D), Color(0xFFA5D6A7), Color(0xFFE57373), Color(0xFFBA68C8))
    val total = counts.sum().takeIf { it > 0 } ?: 1
    val avg = if (options.all { it.toIntOrNull() != null }) {
        val sum = options.zip(counts).sumOf { (opt, cnt) -> (opt.toIntOrNull() ?: 0) * cnt }
        if (total > 0) sum.toFloat() / total else 0f
    } else null
    val darkTextColor = Color(0xFF22223B)
    Column(Modifier.height(180.dp).fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.Bottom) {
        counts.forEachIndexed { i, count ->
            val percent = if (total > 0) (count * 100 / total) else 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(12.dp)) { drawCircle(colors[i % colors.size]) }
                Spacer(Modifier.width(4.dp))
                androidx.compose.material3.Text(options[i], style = MaterialTheme.typography.bodySmall.copy(color = darkTextColor, fontWeight = FontWeight.Bold), modifier = Modifier.width(80.dp))
                Box(Modifier.height(24.dp).weight(1f)) {
                    Canvas(Modifier.fillMaxHeight().fillMaxWidth(count / maxCount.toFloat())) {
                        drawRect(colors[i % colors.size])
                    }
                }
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.Text("${count} ($percent%)", style = MaterialTheme.typography.bodySmall.copy(color = darkTextColor, fontWeight = FontWeight.Bold))
            }
        }
        androidx.compose.material3.Text("Total responses: $total", style = MaterialTheme.typography.bodySmall.copy(color = darkTextColor, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
        if (avg != null) {
            androidx.compose.material3.Text("Average: %.2f".format(avg), style = MaterialTheme.typography.bodySmall.copy(color = darkTextColor, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 2.dp))
        }
    }
} 