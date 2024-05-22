package com.example.study.presentation.session

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.model.Session
import com.example.study.domain.repository.SessionRepository
import com.example.study.domain.repository.SubjectRepository
import com.example.study.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())

    val state = combine(
        _state,
        subjectRepository.getAllSubject(),
        sessionRepository.getAllSessions()
    ){ state, subjects, sessions ->
        state.copy(
            subjects = subjects,
            sessions = sessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SessionState()
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    fun onEvent(event: SessionEvent){
        when(event) {
            SessionEvent.CheckSubjectId -> notifyToUpdateSubject()
            SessionEvent.DeleteSession -> deleteSession()
            is SessionEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }
            is SessionEvent.OnRelatedSubjectChange -> {
                _state.update {
                    it.copy(
                        relatedSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }
            is SessionEvent.SaveSession -> insertSession(event.duration)
            is SessionEvent.UpdateSubjectIdAndRelatedSubject -> {
                _state.update {
                    it.copy(
                        relatedSubject = event.relatedSubject,
                        subjectId = event.subjectId
                    )
                }
            }
        }
    }

    private fun notifyToUpdateSubject() {
        viewModelScope.launch {
            if(state.value.subjectId == null || state.value.relatedSubject == null) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        message = "Please select subject related to the session."
                    )
                )
            }
        }
    }

    private fun insertSession(duration: Long) {
        viewModelScope.launch {
            if (duration < 36){
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        "A session cannot be less than 36 seconds"
                    )
                )
                return@launch
            }
            try {
                sessionRepository.insertSession(
                    session = Session(
                        sessionSubjectId = state.value.subjectId ?: -1,
                        subject = state.value.relatedSubject ?: "",
                        date = Instant.now().toEpochMilli(),
                        duration = duration
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        "Session saved successfully"
                    )
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        "Couldn't save session. ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun deleteSession() {
        viewModelScope.launch {
            try {
                state.value.session?.let {
                    sessionRepository.deleteSession(it)
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackBar(
                            "Session deleted successfully"
                        )
                    )
                }

            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        "Couldn't delete session. ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

}