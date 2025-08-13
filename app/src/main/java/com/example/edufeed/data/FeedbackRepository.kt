package com.example.edufeed.data

import com.example.edufeed.data.FeedbackDao
import com.example.edufeed.data.models.FeedbackQuestion
import com.example.edufeed.data.models.FeedbackResponse
import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // --- Simplified operations (Firebase only) ---
    suspend fun insertFeedbackQuestion(question: FeedbackQuestion) = Unit
    suspend fun insertFeedbackSession(session: FeedbackSession) = Unit
    suspend fun insertFeedbackResponse(response: FeedbackResponse) = Unit
    suspend fun getAllFeedbackQuestions() = emptyList<FeedbackQuestion>()
    suspend fun getFeedbackSessionsByTeacherId(teacherId: String) = emptyList<FeedbackSession>()
    suspend fun getFeedbackResponsesBySessionId(sessionId: String) = emptyList<FeedbackResponse>()

    // --- Questions ---
    suspend fun getFeedbackQuestions(teacherId: String): Result<List<FeedbackQuestion>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("FeedbackQuestions")
                .whereEqualTo("createdBy", teacherId)
                .get().await()
            val questions = snapshot.documents.mapNotNull { it.toObject(FeedbackQuestion::class.java) }
            Result.Success(questions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createFeedbackQuestion(question: FeedbackQuestion): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = if (question.questionId.isEmpty()) UUID.randomUUID().toString() else question.questionId
            val q = question.copy(questionId = id)
            db.collection("FeedbackQuestions").document(id).set(q).await()
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateFeedbackQuestion(question: FeedbackQuestion): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("FeedbackQuestions").document(question.questionId).set(question).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteFeedbackQuestion(questionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("FeedbackQuestions").document(questionId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getFeedbackQuestionsForSession(sessionId: String): Result<List<FeedbackQuestion>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val sessionDoc = db.collection("FeedbackSessions").document(sessionId).get().await()
            val session = sessionDoc.toObject(FeedbackSession::class.java)
            val ids = session?.questionList ?: emptyList()
            if (ids.isEmpty()) return@withContext Result.Success(emptyList())
            val batch = db.collection("FeedbackQuestions").whereIn("questionId", ids.take(10)).get().await()
            val questions = batch.documents.mapNotNull { it.toObject(FeedbackQuestion::class.java) }
            Result.Success(questions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // --- Sessions ---
    suspend fun getFeedbackSessions(teacherId: String): Result<List<FeedbackSession>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("FeedbackSessions")
                .whereEqualTo("teacherId", teacherId)
                .get().await()
            val sessions = snapshot.documents.mapNotNull { it.toObject(FeedbackSession::class.java) }
            Result.Success(sessions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createFeedbackSession(session: FeedbackSession): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = if (session.sessionId.isEmpty()) UUID.randomUUID().toString() else session.sessionId
            val s = session.copy(sessionId = id)
            db.collection("FeedbackSessions").document(id).set(s).await()
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateFeedbackSession(session: FeedbackSession): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("FeedbackSessions").document(session.sessionId).set(session).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteFeedbackSession(sessionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("FeedbackSessions").document(sessionId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getFeedbackSession(sessionId: String): Result<FeedbackSession?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = db.collection("FeedbackSessions").document(sessionId).get().await()
            Result.Success(doc.toObject(FeedbackSession::class.java))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // --- Responses ---
    suspend fun getFeedbackResponses(sessionId: String): Result<List<FeedbackResponse>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("FeedbackSessions").document(sessionId)
                .collection("Responses").get().await()
            val responses = snapshot.documents.mapNotNull { it.toObject(FeedbackResponse::class.java) }
            Result.Success(responses)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun submitFeedbackResponses(responses: List<FeedbackResponse>): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Group by session and write documents
            responses.groupBy { it.sessionId }.forEach { (sessionId, group) ->
                val batch = db.batch()
                group.forEach { r ->
                    val id = if (r.responseId.isEmpty()) UUID.randomUUID().toString() else r.responseId
                    val rr = r.copy(responseId = id)
                    val ref = db.collection("FeedbackSessions").document(sessionId)
                        .collection("Responses").document(id)
                    batch.set(ref, rr)
                }
                batch.commit().await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun submitFeedbackResponse(sessionId: String, response: FeedbackResponse): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = if (response.responseId.isEmpty()) UUID.randomUUID().toString() else response.responseId
            val rr = response.copy(responseId = id)
            db.collection("FeedbackSessions").document(sessionId)
                .collection("Responses").document(id).set(rr).await()
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // --- Sync logic ---
    suspend fun syncFeedbackSessionsToLocal(teacherId: String) {
        try {
            val snapshot = db.collection("FeedbackSessions")
                .whereEqualTo("teacherId", teacherId).get().await()
            val sessions = snapshot.documents.mapNotNull { it.toObject(FeedbackSession::class.java) }
            // Local caching removed
        } catch (e: Exception) {
            // Swallow for now; sync is best-effort
        }
    }
}