package com.example.feedbackapp.data.models

data class FeedbackSession(
    val sessionId: String = "",
    val teacherId: String = "",
    val section: String = "",
    val code: String = "",
    val concept: String = "",
    val questionList: List<String> = emptyList(),
    val status: String = "DRAFT", // or ACTIVE, ENDED
    val startTime: Long = 0L,
    val endTime: Long = 0L
) 