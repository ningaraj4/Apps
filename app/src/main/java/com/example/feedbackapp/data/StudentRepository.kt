package com.example.feedbackapp.data

import com.example.feedbackapp.data.models.FeedbackResponse
import com.example.feedbackapp.data.models.FeedbackSession
import com.example.feedbackapp.data.models.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StudentRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    suspend fun validateAndFetchSession(code: String, studentSection: String): Result<FeedbackSession> {
        return try {
            val snapshot = db.collection("FeedbackSessions")
                .whereEqualTo("code", code)
                .whereEqualTo("status", "ACTIVE")
                .get().await()
            val session = snapshot.documents.firstOrNull()?.toObject(FeedbackSession::class.java)
            if (session == null) return Result.failure(Exception("Invalid or inactive session code"))
            if (session.section != studentSection) return Result.failure(Exception("Section mismatch"))
            val now = System.currentTimeMillis()
            if (now < session.startTime || now > session.endTime) return Result.failure(Exception("Session not active"))
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchQuestions(session: FeedbackSession): Result<List<Question>> {
        return try {
            val teacherId = session.teacherId
            val questionIds = session.questionList
            val questions = mutableListOf<Question>()
            for (qid in questionIds) {
                val doc = db.collection("Teachers").document(teacherId)
                    .collection("QuestionBank").document(qid).get().await()
                doc.toObject(Question::class.java)?.let { questions.add(it) }
            }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasSubmitted(sessionId: String, studentId: String): Result<Boolean> {
        return try {
            val snapshot = db.collection("FeedbackResponses")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("studentId", studentId)
                .get().await()
            Result.success(snapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitFeedback(responses: List<FeedbackResponse>): Result<Unit> {
        return try {
            val batch = db.batch()
            for (response in responses) {
                val docId = UUID.randomUUID().toString()
                val docRef = db.collection("FeedbackResponses").document(docId)
                batch.set(docRef, response.copy(responseId = docId))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 