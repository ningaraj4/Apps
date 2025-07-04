package com.example.feedbackapp.data

import com.example.feedbackapp.data.models.FeedbackSession
import com.example.feedbackapp.data.models.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.util.Log

class TeacherRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    suspend fun createSession(
        teacherId: String,
        section: String,
        concept: String,
        questionList: List<String>
    ): Result<Pair<String, String>> {
        return try {
            Log.d("TeacherRepository", "createSession called for teacherId=$teacherId, section=$section, concept=$concept, questions=$questionList")
            val sessionId = UUID.randomUUID().toString()
            val code = (100000..999999).random().toString()
            val session = FeedbackSession(
                sessionId = sessionId,
                teacherId = teacherId,
                section = section,
                code = code,
                concept = concept,
                questionList = questionList,
                status = "DRAFT"
            )
            db.collection("FeedbackSessions").document(sessionId).set(session).await()
            Log.d("TeacherRepository", "Session created with ID: $sessionId and code: $code")
            Result.success(Pair(sessionId, code))
        } catch (e: Exception) {
            Log.e("TeacherRepository", "Failed to create session", e)
            Result.failure(Exception("Failed to create session: ${e.message}"))
        }
    }

    suspend fun startSession(sessionId: String, durationMinutes: Int): Result<Unit> {
        return try {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMinutes * 60 * 1000
            db.collection("FeedbackSessions").document(sessionId).update(
                mapOf(
                    "status" to "ACTIVE",
                    "startTime" to startTime,
                    "endTime" to endTime
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSessions(teacherId: String): Result<List<FeedbackSession>> {
        return try {
            val snapshot = db.collection("FeedbackSessions")
                .whereEqualTo("teacherId", teacherId)
                .get().await()
            val sessions = snapshot.documents.mapNotNull { it.toObject(FeedbackSession::class.java) }
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuestionBank(teacherId: String): Result<List<Question>> {
        return try {
            val snapshot = db.collection("Teachers").document(teacherId)
                .collection("QuestionBank").get().await()
            val questions = snapshot.documents.mapNotNull { it.toObject(Question::class.java) }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrUpdateQuestion(teacherId: String, question: Question): Result<Unit> {
        return try {
            val qId = if (question.questionId.isEmpty()) UUID.randomUUID().toString() else question.questionId
            val q = question.copy(questionId = qId)
            db.collection("Teachers").document(teacherId)
                .collection("QuestionBank").document(qId).set(q).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuestion(teacherId: String, questionId: String): Result<Unit> {
        return try {
            db.collection("Teachers").document(teacherId)
                .collection("QuestionBank").document(questionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            db.collection("FeedbackSessions").document(sessionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 