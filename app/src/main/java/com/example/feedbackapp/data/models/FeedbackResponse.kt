package com.example.feedbackapp.data.models

data class FeedbackResponse(
    val responseId: String = "",
    val sessionId: String = "",
    val studentId: String = "",
    val questionId: String = "",
    val answer: String = ""
) 