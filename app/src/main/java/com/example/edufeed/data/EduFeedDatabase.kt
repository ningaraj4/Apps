package com.example.edufeed.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.edufeed.data.models.Quiz
import com.example.edufeed.data.models.QuizQuestion
import com.example.edufeed.data.models.QuizResponse
import com.example.edufeed.data.models.User
import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.data.models.FeedbackResponse
import com.example.edufeed.data.converters.QuizTypeConverters
import com.example.edufeed.data.models.QuizSession

/**
 * Room database for EduFeed application.
 * Version History:
 * - Version 1: Initial database schema
 * - Version 2: Added FeedbackSession and FeedbackResponse tables
 */
@Database(
    entities = [
        User::class,
        Quiz::class,
        QuizQuestion::class,
        QuizResponse::class,
        QuizSession::class,
        FeedbackSession::class,
        FeedbackResponse::class
    ],
    version = 2,
    exportSchema = true // Enable schema export for migrations
)
@TypeConverters(QuizTypeConverters::class)
abstract class EduFeedDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun userDao(): UserDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: EduFeedDatabase? = null

        fun getDatabase(context: Context): EduFeedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduFeedDatabase::class.java,
                    "edufeed_database"
                )
                // For production, implement proper migrations instead of destructive migration
                .fallbackToDestructiveMigration() // TODO: Replace with proper migrations for production
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Initialize database with default data if needed
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 