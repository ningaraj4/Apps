package com.example.feedbackapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoleButton(role: String, selected: Boolean, onClick: () -> Unit) {
    val circleColor = if (selected) Color(0xFF4A90E2) else Color.White
    val borderColor = Color(0xFF4A90E2)
    val textColor = if (selected) Color.White else Color(0xFF4A90E2)
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(circleColor, CircleShape)
            .border(
                width = if (selected) 0.dp else 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (role == "Student") "S" else "T",
            color = textColor,
            fontSize = 20.sp
        )
    }
} 