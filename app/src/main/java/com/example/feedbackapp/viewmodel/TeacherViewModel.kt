package com.example.feedbackapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedbackapp.data.TeacherRepository
import com.example.feedbackapp.data.models.FeedbackSession
import com.example.feedbackapp.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TeacherUiState {
    object Idle : TeacherUiState()
    object Loading : TeacherUiState()
    data class Success(val message: String = "") : TeacherUiState()
    data class SessionCreated(val sessionId: String, val code: String) : TeacherUiState()
    data class Error(val message: String) : TeacherUiState()
    data class Sessions(val sessions: List<FeedbackSession>) : TeacherUiState()
    data class QuestionBank(val questions: List<Question>) : TeacherUiState()
}

class TeacherViewModel(private val repository: TeacherRepository = TeacherRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<TeacherUiState>(TeacherUiState.Idle)
    val uiState: StateFlow<TeacherUiState> = _uiState

    fun createSession(teacherId: String, section: String, concept: String, questionIds: List<String>) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.createSession(teacherId, section, concept, questionIds)
            _uiState.value = result.fold(
                onSuccess = { sessionData ->
                    TeacherUiState.SessionCreated(sessionData.first, sessionData.second)
                },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to create session") }
            )
        }
    }

    fun startSession(sessionId: String, durationMinutes: Int) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.startSession(sessionId, durationMinutes)
            _uiState.value = result.fold(
                onSuccess = { TeacherUiState.Success("Session started") },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to start session") }
            )
        }
    }

    fun getSessions(teacherId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.getSessions(teacherId)
            _uiState.value = result.fold(
                onSuccess = { TeacherUiState.Sessions(it) },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to fetch sessions") }
            )
        }
    }

    fun getQuestionBank(teacherId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.getQuestionBank(teacherId)
            _uiState.value = result.fold(
                onSuccess = { TeacherUiState.QuestionBank(it) },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to fetch questions") }
            )
        }
    }

    fun addOrUpdateQuestion(teacherId: String, question: Question) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.addOrUpdateQuestion(teacherId, question)
            _uiState.value = result.fold(
                onSuccess = { TeacherUiState.Success("Question saved") },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to save question") }
            )
        }
    }

    fun deleteQuestion(teacherId: String, questionId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.deleteQuestion(teacherId, questionId)
            _uiState.value = result.fold(
                onSuccess = { TeacherUiState.Success("Question deleted") },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to delete question") }
            )
        }
    }

    fun deleteSession(sessionId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.deleteSession(sessionId)
            _uiState.value = result.fold(
                onSuccess = { TeacherUiState.Success("Session deleted") },
                onFailure = { TeacherUiState.Error(it.message ?: "Failed to delete session") }
            )
        }
    }
} 