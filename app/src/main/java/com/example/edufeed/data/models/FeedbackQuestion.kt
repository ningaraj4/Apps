package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a feedback question in the system.
 * @property questionId Unique identifier for the question
 * @property questionText The text of the question
 * @property type The type of question (e.g., RATING, TEXT, MULTIPLE_CHOICE)
 * @property options List of options for multiple choice questions (if applicable)
 * @property isRequired Whether the question is required to be answered
 * @property section The section/category this question belongs to (e.g., "Course Content", "Instructor")
 * @property createdBy ID of the teacher who created this question
 * @property createdAt Timestamp when the question was created
 */
@Entity(tableName = "feedback_questions")
data class FeedbackQuestion(
    @PrimaryKey
    val questionId: String = "",
    val questionText: String = "",
    val type: FeedbackQuestionType = FeedbackQuestionType.RATING,
    val options: List<String> = emptyList(),
    val isRequired: Boolean = true,
    val section: String = "General",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing different types of feedback questions.
 */
enum class FeedbackQuestionType {
    RATING,        // 1-5 rating scale
    TEXT,          // Open-ended text response
    MULTIPLE_CHOICE // Multiple choice with predefined options
}
