package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.edufeed.data.converters.QuizTypeConverters

@Entity(
    tableName = "quiz_responses",
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["studentId"]),
        Index(value = ["questionId"]),
        Index(value = ["quizId", "studentId", "questionId"], unique = true)
    ]
)
@TypeConverters(QuizTypeConverters::class)
data class QuizResponse(
    @PrimaryKey
    val responseId: String = "",
    val quizId: String = "",
    val studentId: String = "",
    val questionId: String = "",
    val selectedAnswer: String = "",
    val score: Int = 0,
    val submittedAt: Long = 0L,
    val isCorrect: Boolean = false,
    val timeSpent: Long = 0L // Time spent on question in milliseconds
) 