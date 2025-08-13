package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.edufeed.data.converters.QuizTypeConverters

@Entity(tableName = "quiz_sessions")
@TypeConverters(QuizTypeConverters::class)
data class QuizSession(
    @PrimaryKey
    val sessionId: String,
    val teacherId: String,
    val title: String,
    val description: String,
    val status: String, // DRAFT, ACTIVE, ENDED
    val createdAt: Long,
    val startTime: Long,
    val endTime: Long,
    val duration: Int,
    val questionIds: List<String>,
    val section: String,
    val code: String = "" // 6-digit code for joining
)
