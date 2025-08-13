package com.example.edufeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edufeed.data.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TeacherUiState {
    object Idle : TeacherUiState()
    object Loading : TeacherUiState()
    data class Success(val message: String = "") : TeacherUiState()
    data class SessionCreated(val sessionId: String, val code: String) : TeacherUiState()
    data class Error(val message: String) : TeacherUiState()
    data class Sessions(val sessions: List<com.example.edufeed.data.models.FeedbackSession>) : TeacherUiState()
    data class QuestionBank(val questions: List<com.example.edufeed.data.models.Question>) : TeacherUiState()
}

class TeacherViewModel(
    private val repository: TeacherRepository = TeacherRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<TeacherUiState>(TeacherUiState.Idle)
    val uiState: StateFlow<TeacherUiState> = _uiState

    fun createSession(teacherId: String, section: String, concept: String, questionIds: List<String>, isAnonymous: Boolean, onSuccess: (Pair<String, String>) -> Unit = {}) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.createSession(teacherId, section, concept, questionIds, isAnonymous)
            result.getOrNull()?.let { sessionInfo ->
                onSuccess(sessionInfo)
                _uiState.value = TeacherUiState.Success("Session created successfully")
            } ?: run {
                _uiState.value = TeacherUiState.Error("Failed to create session")
            }
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
            _uiState.value = if (result.isSuccess) TeacherUiState.Sessions(result.getOrNull() ?: emptyList()) else TeacherUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun getQuestionBank(teacherId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.getQuestionBank(teacherId)
            _uiState.value = if (result.isSuccess) TeacherUiState.QuestionBank(result.getOrNull() ?: emptyList()) else TeacherUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun addOrUpdateQuestion(teacherId: String, question: com.example.edufeed.data.models.Question) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.addOrUpdateQuestion(teacherId, question)
            _uiState.value = if (result.isSuccess) TeacherUiState.Success("Question saved") else TeacherUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun deleteQuestion(teacherId: String, questionId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.deleteQuestion(teacherId, questionId)
            _uiState.value = if (result.isSuccess) TeacherUiState.Success("Question deleted") else TeacherUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun deleteSession(sessionId: String) {
        _uiState.value = TeacherUiState.Loading
        viewModelScope.launch {
            val result = repository.deleteSession(sessionId)
            _uiState.value = if (result.isSuccess) TeacherUiState.Success("Session deleted") else TeacherUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }
}