package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quizzes",
    indices = [
        Index(value = ["quizId"], unique = true),
        Index(value = ["createdBy"]),
        Index(value = ["sectionId"]),
        Index(value = ["code"], unique = true)
    ]
)
data class Quiz(
    @PrimaryKey
    val quizId: String = "",
    val title: String = "",
    val description: String = "",
    val sectionId: String = "",
    val createdBy: String = "", // teacherId
    val duration: Int = 30, // in minutes
    val code: String = "", // 6-digit join code
    val isActive: Boolean = true,
    val totalMarks: Int = 0,
    val passingMarks: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)