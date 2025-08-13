package com.example.edufeed.data

import androidx.room.*
import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.data.models.FeedbackResponse

@Dao
interface FeedbackDao {
    // FeedbackSession operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedbackSession(session: FeedbackSession)

    @Query("SELECT * FROM feedback_sessions WHERE sessionId = :sessionId")
    suspend fun getFeedbackSessionById(sessionId: String): FeedbackSession?

    @Query("SELECT * FROM feedback_sessions WHERE teacherId = :teacherId")
    suspend fun getFeedbackSessionsByTeacher(teacherId: String): List<FeedbackSession>

    @Query("SELECT * FROM feedback_sessions WHERE code = :code")
    suspend fun getFeedbackSessionByCode(code: String): FeedbackSession?

    @Query("SELECT * FROM feedback_sessions WHERE status = :status")
    suspend fun getFeedbackSessionsByStatus(status: String): List<FeedbackSession>

    @Update
    suspend fun updateFeedbackSession(session: FeedbackSession)

    @Delete
    suspend fun deleteFeedbackSession(session: FeedbackSession)

    // FeedbackResponse operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedbackResponse(response: FeedbackResponse)

    @Query("SELECT * FROM feedback_responses WHERE sessionId = :sessionId")
    suspend fun getFeedbackResponsesBySession(sessionId: String): List<FeedbackResponse>

    @Query("SELECT * FROM feedback_responses WHERE studentId = :studentId")
    suspend fun getFeedbackResponsesByStudent(studentId: String): List<FeedbackResponse>

    @Query("SELECT * FROM feedback_responses WHERE sessionId = :sessionId AND studentId = :studentId")
    suspend fun getFeedbackResponsesBySessionAndStudent(sessionId: String, studentId: String): List<FeedbackResponse>

    @Delete
    suspend fun deleteFeedbackResponse(response: FeedbackResponse)
}