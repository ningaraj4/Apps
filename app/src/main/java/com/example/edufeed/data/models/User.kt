package com.example.edufeed.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["userId"], unique = true)
    ]
)
data class User(
    @PrimaryKey
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // or teacher
    val section: String = "",
    val profilePictureUrl: String = "",
    val createdAt: Long = 0L,
    val lastLogin: Long = 0L,
    val isActive: Boolean = true
)