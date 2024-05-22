package com.example.study.presentation.task

import com.example.study.domain.model.Subject
import com.example.study.util.Priority

sealed class TaskEvent {
    data class OnTitleChange(val title: String): TaskEvent()

    data class OnDescriptionChange(val description: String): TaskEvent()

    data class OnDateChange(val millis: Long?): TaskEvent()

    data class OnPriorityChange(val priority: Priority): TaskEvent()

    data class OnRelatedSubjectSelect(val subject: Subject): TaskEvent()

    data object OnIsTaskCompleteChange: TaskEvent()

    data object SaveTask: TaskEvent()

    data object DeleteTask: TaskEvent()
}