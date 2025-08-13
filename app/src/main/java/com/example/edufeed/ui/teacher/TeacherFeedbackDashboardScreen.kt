package com.example.edufeed.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.font.FontWeight
import com.example.edufeed.ui.components.DashboardButton
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp

@Composable
fun TeacherFeedbackDashboardScreen(
    teacherId: String,
    navController: NavController
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E8), Color(0xFF4CAF50)),
        startY = 0f,
        endY = 1200f
    )
    val accentColor = Color(0xFF4CAF50)
    val cardColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(32.dp))
                // Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.1f),
                                        accentColor.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Decorative circle background
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        accentColor.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Removed emoji symbol
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = "Feedback Dashboard",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(0xFF22223B),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Collect and analyze student feedback effectively",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            item {
                // Quick Actions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF22223B),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        DashboardButton(
                            label = "Create Feedback Session",
                            color = accentColor,
                            onClick = { navController.navigate("teacher_create_feedback_session/$teacherId") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        DashboardButton(
                            label = "Manage Question Bank",
                            color = Color(0xFF2196F3),
                            onClick = { navController.navigate("teacher_feedback_question_bank/$teacherId") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                // Management Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Feedback Management",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF22223B),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        DashboardButton(
                            label = "View Previous Sessions",
                            color = Color(0xFF7C3AED),
                            onClick = { navController.navigate("teacher_feedback_sessions/$teacherId") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        DashboardButton(
                            label = "Feedback Analytics & Reports",
                            color = Color(0xFFFF9800),
                            onClick = { navController.navigate("teacher_feedback_analytics/$teacherId") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

