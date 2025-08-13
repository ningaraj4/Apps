package com.example.edufeed.data

import androidx.room.*
import com.example.edufeed.data.models.Quiz
import com.example.edufeed.data.models.QuizQuestion
import com.example.edufeed.data.models.QuizResponse
import com.example.edufeed.data.models.QuizSession

@Dao
interface QuizDao {

    // Quiz
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz)

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId")
    suspend fun getQuizById(quizId: String): Quiz?

    @Query("SELECT * FROM quizzes")
    suspend fun getAllQuizzes(): List<Quiz>

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    // QuizQuestion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestion(question: QuizQuestion)

    @Update
    suspend fun updateQuizQuestion(question: QuizQuestion)

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuiz(quizId: String): List<QuizQuestion>

    @Delete
    suspend fun deleteQuizQuestion(question: QuizQuestion)

    // ✅ NEW: Delete question by questionId
    @Query("DELETE FROM quiz_questions WHERE questionId = :questionId")
    suspend fun deleteQuizQuestionById(questionId: String)

    // QuizResponse
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResponse(response: QuizResponse)

    @Query("SELECT * FROM quiz_responses WHERE quizId = :quizId")
    suspend fun getResponsesForQuiz(quizId: String): List<QuizResponse>

    @Query("SELECT * FROM quiz_responses WHERE studentId = :studentId")
    suspend fun getResponsesForStudent(studentId: String): List<QuizResponse>

    @Delete
    suspend fun deleteQuizResponse(response: QuizResponse)

    // QuizSession
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSession(session: QuizSession)

    @Query("SELECT * FROM quiz_sessions WHERE teacherId = :teacherId")
    suspend fun getQuizSessionsByTeacherId(teacherId: String): List<QuizSession>

    @Query("SELECT * FROM quiz_sessions WHERE sessionId = :sessionId")
    suspend fun getQuizSessionById(sessionId: String): QuizSession?

    @Query("SELECT * FROM quiz_sessions WHERE code = :code")
    suspend fun getQuizSessionByCode(code: String): QuizSession?

    @Query("DELETE FROM quiz_sessions WHERE sessionId = :sessionId")
    suspend fun deleteQuizSessionById(sessionId: String)
}
