package com.example.edufeed.data.converters

import androidx.room.TypeConverter
import com.example.edufeed.data.models.QuestionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converters for Room database to store complex types as strings.
 * This is the canonical implementation of QuizTypeConverters used throughout the app.
 */
class QuizTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(data, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromQuestionType(type: QuestionType): String {
        return type.name
    }

    @TypeConverter
    fun toQuestionType(name: String?): QuestionType {
        return try {
            QuestionType.valueOf(name ?: return QuestionType.MCQ)
        } catch (e: IllegalArgumentException) {
            QuestionType.MCQ
        }
    }
}
