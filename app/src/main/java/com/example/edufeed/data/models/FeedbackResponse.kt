package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "feedback_responses",
    indices = [
        Index(value = ["responseId"], unique = true),
        Index(value = ["sessionId"]),
        Index(value = ["studentId"]),
        Index(value = ["questionId"]),
        Index(value = ["sessionId", "studentId", "questionId"], unique = true)
    ]
)
data class FeedbackResponse(
    @PrimaryKey
    val responseId: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val studentId: String = "",
    val questionId: String = "",
    val answer: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Needed for Firestore deserialization
    constructor() : this(
        responseId = "",
        sessionId = "",
        studentId = "",
        questionId = "",
        answer = "",
        submittedAt = 0L,
        updatedAt = 0L
    )
}
