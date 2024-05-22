package com.example.study.presentation.task

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.model.Task
import com.example.study.domain.repository.SubjectRepository
import com.example.study.domain.repository.TaskRepository
import com.example.study.presentation.navArgs
import com.example.study.util.Priority
import com.example.study.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(TaskState())
    private val navArgs: TaskScreenNavArgs = savedStateHandle.navArgs()

    val state = combine(
        _state,
        subjectRepository.getAllSubject(),
    ) { state, subjects ->
        state.copy(
            subjects = subjects
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskState()
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    init {
        fetchTask()
        fetchSubject()
    }

    fun onEvent(event: TaskEvent){
        when(event) {
            is TaskEvent.OnTitleChange -> {
                _state.update {
                    it.copy(
                        title = event.title
                    )
                }
            }
            is TaskEvent.OnDescriptionChange -> {
                _state.update {
                    it.copy(
                        description = event.description
                    )
                }
            }
            is TaskEvent.OnDateChange -> {
                _state.update {
                    it.copy(
                        dueDate = event.millis
                    )
                }
            }
            is TaskEvent.OnPriorityChange -> {
                _state.update {
                    it.copy(
                        priority = event.priority
                    )
                }
            }
            is TaskEvent.OnRelatedSubjectSelect -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }
            TaskEvent.OnIsTaskCompleteChange -> {
                _state.update {
                    it.copy(
                        isTaskComplete = !_state.value.isTaskComplete
                    )
                }
            }
            TaskEvent.DeleteTask -> deleteTask()
            TaskEvent.SaveTask -> saveTask()
        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            try {
                val currentTaskId = state.value.currentTaskId
                if (currentTaskId != null) {
                    withContext(Dispatchers.IO){
                        taskRepository.deleteTask(taskId = currentTaskId)
                    }
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackBar(
                            message = "Task deleted successfully"
                        )
                    )
                    _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
                } else {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackBar(
                            message = "No task found"
                        )
                    )
                }


            } catch (e: Exception){
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        message = "Couldn't delete task. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun saveTask() {
        viewModelScope.launch {
                val state = _state.value
                if(state.subjectId == null || state.relatedToSubject == null){
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackBar(
                            message = "Kindly select subject"
                        )
                    )
                    return@launch
                }
            try {

                taskRepository.insertTask(
                    task = Task(
                        title = state.title,
                        description = state.description,
                        dueDate = state.dueDate ?: Instant.now().toEpochMilli(),
                        priority = state.priority.value,
                        subject = state.relatedToSubject,
                        isComplete = state.isTaskComplete,
                        taskId = state.currentTaskId,
                        taskSubjectId = state.subjectId
                    )
                )

                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        message = "Task saved successfully"
                    )
                )

                _snackbarEventFlow.emit(
                    SnackbarEvent.NavigateUp
                )

            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackBar(
                        message = "Couldn't save task. ${e.message}"
                    )
                )
            }
        }
    }

    private fun fetchTask() {
        viewModelScope.launch {
            navArgs.taskId?.let { id ->
                taskRepository.getTaskById(id)?.let { task ->
                    _state.update {
                        it.copy(
                            title = task.title,
                            description = task.description,
                            dueDate = task.dueDate,
                            isTaskComplete = task.isComplete,
                            relatedToSubject = task.subject,
                            priority = Priority.fromInt(task.priority),
                            subjectId = task.taskSubjectId,
                            currentTaskId = task.taskId
                        )
                    }
                }
            }
        }
    }

    private fun fetchSubject() {
        viewModelScope.launch {
            navArgs.subjectId?.let { id ->
                subjectRepository.getSubjectById(id)?.let { subject ->
                    _state.update {
                        it.copy(
                            subjectId = subject.subjectId,
                            relatedToSubject = subject.name
                        )
                    }
                }
            }
        }
    }

}