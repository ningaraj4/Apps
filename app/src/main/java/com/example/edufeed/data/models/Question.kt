package com.example.edufeed.data.models

data class Question(
    val questionId: String = "",
    val questionText: String = "",
    val type: String = "MCQ", // MCQ, scale, understood
    val options: List<String> = emptyList()
) {
    constructor() : this("", "", "MCQ", emptyList())
}
