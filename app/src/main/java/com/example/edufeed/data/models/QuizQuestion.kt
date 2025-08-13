package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.edufeed.data.converters.QuizTypeConverters

@Entity(
    tableName = "quiz_questions",
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["questionId"], unique = true)
    ]
)
@TypeConverters(QuizTypeConverters::class)
data class QuizQuestion(
    @PrimaryKey
    val questionId: String = "",
    val quizId: String = "",
    val questionText: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val marks: Int = 1,
    val createdAt: Long = 0L
)