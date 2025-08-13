package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.edufeed.data.converters.QuizTypeConverters
import java.util.UUID

@Entity(
    tableName = "feedback_sessions",
    indices = [
        Index(value = ["sessionId"], unique = true),
        Index(value = ["teacherId"]),
        Index(value = ["code"], unique = true),
        Index(value = ["status"])
    ]
)
@TypeConverters(QuizTypeConverters::class)
data class FeedbackSession(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(),
    val teacherId: String = "",
    val title: String = "",
    val section: String = "",
    val code: String = (100000..999999).random().toString(),
    val concept: String = "",
    val questionList: List<String> = emptyList(),
    val status: String = "DRAFT", // DRAFT, ACTIVE, ENDED
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val responseCount: Int = 0
) {
    // Needed for Firestore deserialization
    constructor() : this(
        sessionId = "",
        teacherId = "",
        section = "",
        code = "",
        concept = "",
        questionList = emptyList(),
        status = "DRAFT",
        startTime = 0L,
        endTime = 0L,
        isAnonymous = false
    )
}
