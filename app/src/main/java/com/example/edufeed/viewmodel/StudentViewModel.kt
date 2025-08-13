package com.example.edufeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edufeed.data.StudentRepository
import com.example.edufeed.data.models.FeedbackResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.example.edufeed.data.models.FeedbackSession
import com.example.edufeed.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class StudentUiState {
    object Idle : StudentUiState()
    object Loading : StudentUiState()
    data class SessionLoaded(val session: FeedbackSession, val questions: List<Question>) : StudentUiState()
    data class FeedbackSubmitted(val message: String = "Feedback submitted") : StudentUiState()
    data class Error(val message: String) : StudentUiState()
    data class AlreadySubmitted(val message: String = "Feedback already submitted") : StudentUiState()
}


class StudentViewModel(
    private val repository: StudentRepository = StudentRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudentUiState>(StudentUiState.Idle)
    val uiState: StateFlow<StudentUiState> = _uiState

    fun joinSession(code: String, studentSection: String) {
        _uiState.value = StudentUiState.Loading
        viewModelScope.launch {
            val sessionResult = repository.validateAndFetchSession(code, studentSection)
            sessionResult.fold(
                onSuccess = { session ->
                    val questionsResult = repository.fetchQuestions(session)
                    questionsResult.fold(
                        onSuccess = { questions ->
                            _uiState.value = StudentUiState.SessionLoaded(session, questions)
                        },
                        onFailure = { _uiState.value = StudentUiState.Error(it.message ?: "Failed to load questions") }
                    )
                },
                onFailure = { _uiState.value = StudentUiState.Error(it.message ?: "Invalid session") }
            )
        }
    }

    fun hasSubmitted(sessionId: String, studentId: String) {
        _uiState.value = StudentUiState.Loading
        viewModelScope.launch {
            val result = repository.hasSubmitted(sessionId, studentId)
            _uiState.value = result.fold(
                onSuccess = { if (it) StudentUiState.AlreadySubmitted() else StudentUiState.Idle },
                onFailure = { StudentUiState.Error(it.message ?: "Failed to check submission") }
            )
        }
    }

    fun submitFeedback(responses: List<FeedbackResponse>) {
        _uiState.value = StudentUiState.Loading
        viewModelScope.launch {
            val result = repository.submitFeedback(responses)
            _uiState.value = result.fold(
                onSuccess = { StudentUiState.FeedbackSubmitted() },
                onFailure = { StudentUiState.Error(it.message ?: "Failed to submit feedback") }
            )
        }
    }

    fun joinQuizSession(code: String, studentId: String) {
        _uiState.value = StudentUiState.Loading
        viewModelScope.launch {
            // For now, just set to idle - the quiz will be handled in the UI
            _uiState.value = StudentUiState.Idle
        }
    }
} 