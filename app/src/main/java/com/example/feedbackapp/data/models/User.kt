package com.example.feedbackapp.data.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // or teacher
    val section: String = "",
    val profilePictureUrl: String = ""
) 