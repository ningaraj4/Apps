package com.example.edufeed.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edufeed.data.FeedbackRepository
import com.example.edufeed.data.models.FeedbackQuestion
import com.example.edufeed.data.models.FeedbackQuestionType
import com.example.edufeed.data.models.FeedbackResponse
import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


class FeedbackViewModel(
    application: Application,
    private val repository: FeedbackRepository = FeedbackRepository()
) : AndroidViewModel(application) {

    private val _questions = MutableStateFlow<List<FeedbackQuestion>>(emptyList())
    val questions: StateFlow<List<FeedbackQuestion>> = _questions.asStateFlow()

    private val _sessions = MutableStateFlow<List<FeedbackSession>>(emptyList())
    val sessions: StateFlow<List<FeedbackSession>> = _sessions.asStateFlow()

    // Current responses (in-memory during feedback submission)
    private val _responses = MutableStateFlow<Map<String, String>>(emptyMap())
    val responses: StateFlow<Map<String, String>> = _responses.asStateFlow()

    // Full list of FeedbackResponse objects for analytics/results screens
    private val _responsesList = MutableStateFlow<List<FeedbackResponse>>(emptyList())
    val responsesList: StateFlow<List<FeedbackResponse>> = _responsesList.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    private val _session = MutableStateFlow<FeedbackSession?>(null)
    val session: StateFlow<FeedbackSession?> = _session.asStateFlow()
    
    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    /**
     * Loads feedback questions for a specific teacher
     */
    fun loadFeedbackQuestions(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val result = repository.getFeedbackQuestions(teacherId)) {
                    is Result.Success -> {
                        _questions.value = result.data
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to load questions"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load questions"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createFeedbackQuestion(question: FeedbackQuestion, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.createFeedbackQuestion(question)
                when (result) {
                    is Result.Success -> {
                        loadFeedbackQuestions(question.createdBy)
                        onComplete(Result.Success(result.data))
                    }
                    is Result.Error -> {
                        val errorMessage = result.exception.message ?: "Failed to create question"
                        _error.value = errorMessage
                        onComplete(Result.Error(result.exception))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to create question"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFeedbackQuestion(question: FeedbackQuestion, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val repoResult = repository.updateFeedbackQuestion(question)
                when (repoResult) {
                    is Result.Success -> {
                        loadFeedbackQuestions(question.createdBy)
                    }
                    is Result.Error -> {
                        _error.value = repoResult.exception.message
                    }
                }
                onComplete(repoResult)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to update question"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFeedbackQuestion(questionId: String, teacherId: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val repoResult = repository.deleteFeedbackQuestion(questionId)
                when (repoResult) {
                    is Result.Success -> loadFeedbackQuestions(teacherId)
                    is Result.Error -> _error.value = repoResult.exception.message
                }
                onComplete(repoResult)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to delete question"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFeedbackSessions(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val result = repository.getFeedbackSessions(teacherId)) {
                    is Result.Success -> _sessions.value = result.data
                    is Result.Error -> _error.value = result.exception.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load sessions"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createFeedbackSession(session: FeedbackSession, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.createFeedbackSession(session)
                when (result) {
                    is Result.Success -> {
                        loadFeedbackSessions(session.teacherId)
                        onComplete(Result.Success(result.data))
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message
                        onComplete(Result.Error(result.exception))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to create session"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFeedbackSession(session: FeedbackSession, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.updateFeedbackSession(session)
                when (result) {
                    is Result.Success -> {
                        loadFeedbackSessions(session.teacherId)
                        onComplete(Result.Success(Unit))
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message
                        onComplete(Result.Error(result.exception))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to update session"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFeedbackSession(sessionId: String, teacherId: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.deleteFeedbackSession(sessionId)
                when (result) {
                    is Result.Success -> {
                        loadFeedbackSessions(teacherId)
                        onComplete(Result.Success(Unit))
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message
                        onComplete(Result.Error(result.exception))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to delete session"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Submits feedback responses for a session
     */
    fun submitFeedbackResponses(
        sessionId: String,
        studentId: String,
        responses: List<FeedbackResponse>,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _submitSuccess.value = false
            
            try {
                // Validate input
                if (responses.isEmpty()) {
                    throw IllegalArgumentException("No responses to submit")
                }
                
                // Prepare responses with metadata
                val timestamp = System.currentTimeMillis()
                val feedbackResponses = responses.map { response ->
                    response.copy(
                        responseId = response.responseId.ifEmpty { generateId("resp_") },
                        sessionId = sessionId,
                        studentId = studentId,
                        submittedAt = timestamp
                    )
                }
                
                // Submit to repository
                when (val result = repository.submitFeedbackResponses(feedbackResponses)) {
                    is Result.Success -> {
                        // Update local state
                        _submitSuccess.value = true
                        _responses.update { emptyMap() } // Clear current responses
                        _responsesList.update { it + feedbackResponses }
                        onComplete(Result.Success(Unit))
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to submit feedback"
                        onComplete(Result.Error(result.exception))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to submit feedback"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFeedbackResponses(sessionId: String, onComplete: (Result<List<FeedbackResponse>>) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getFeedbackResponses(sessionId)
                when (result) {
                    is Result.Success -> {
                        // Update the local states with the loaded responses
                        val responsesMap = result.data.associate { it.questionId to it.answer }
                        _responses.value = responsesMap
                        _responsesList.value = result.data
                        onComplete(Result.Success(result.data))
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to load responses"
                        onComplete(Result.Error(result.exception))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to load responses"
                _error.value = errorMessage
                onComplete(Result.Error(Exception(errorMessage)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates a single response in the current feedback session
     */
    fun updateResponse(questionId: String, answer: String) {
        _responses.update { current ->
            current.toMutableMap().apply {
                this[questionId] = answer
            }
        }
    }

    /**
     * Clears all current responses
     */
    fun clearResponses() {
        _responses.value = emptyMap()
    }
    
    /**
     * Loads a specific feedback session by ID
     */
    fun loadFeedbackSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val result = repository.getFeedbackSession(sessionId)) {
                    is Result.Success -> {
                        _session.value = result.data
                        // Load questions for this session
                        loadFeedbackQuestionsForSession(sessionId)
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to load session"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load session"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFeedbackQuestionsForSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val result = repository.getFeedbackQuestionsForSession(sessionId)) {
                    is Result.Success -> _questions.value = result.data
                    is Result.Error -> _error.value = result.exception.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load questions for session"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Generates a unique ID with an optional prefix
     */
    private fun generateId(prefix: String = ""): String {
        return "${prefix}${UUID.randomUUID().toString().substring(0, 8)}"
    }
    
    /**
     * Creates a default feedback question for a teacher
     */
    fun createDefaultQuestion(teacherId: String): FeedbackQuestion {
        return FeedbackQuestion(
            questionId = generateId("fq_"),
            questionText = "",
            type = FeedbackQuestionType.RATING,
            options = emptyList(),
            isRequired = true,
            section = "General",
            createdBy = teacherId,
            createdAt = System.currentTimeMillis()
        )
    }
}