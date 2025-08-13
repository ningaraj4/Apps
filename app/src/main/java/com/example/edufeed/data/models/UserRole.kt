package com.example.edufeed.data.models

/**
 * Enum representing the different user roles in the EduFeed application
 */
enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    TEACHER("Teacher");
    
    companion object {
        fun fromString(value: String): UserRole? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
