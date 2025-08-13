package com.example.edufeed.di

import android.content.Context
import androidx.room.Room
import com.example.edufeed.data.EduFeedDatabase
import com.example.edufeed.data.FeedbackDao
import com.example.edufeed.data.QuizDao
import com.example.edufeed.data.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEduFeedDatabase(@ApplicationContext context: Context): EduFeedDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            EduFeedDatabase::class.java,
            "edufeed_database"
        )
        .fallbackToDestructiveMigration() // TODO: Replace with proper migrations for production
        .build()
    }

    @Provides
    fun provideQuizDao(database: EduFeedDatabase): QuizDao = database.quizDao()

    @Provides
    fun provideUserDao(database: EduFeedDatabase): UserDao = database.userDao()

    @Provides
    fun provideFeedbackDao(database: EduFeedDatabase): FeedbackDao = database.feedbackDao()
}
