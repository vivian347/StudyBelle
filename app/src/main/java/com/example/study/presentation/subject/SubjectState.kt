package com.example.study.presentation.subject

import androidx.compose.ui.graphics.Color
import com.example.study.domain.model.Session
import com.example.study.domain.model.Subject
import com.example.study.domain.model.Task

data class SubjectState(
    val currentSubjectId: Int? = null,
    val subjectName: String = "",
    val studyHoursGoal: String = "",
    val hoursStudied: Float = 0f,
    val subjectCardColors: List<Color> = Subject.subjectCardColors.random(),
    val recentSessions: List<Session> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val session: Session? = null,
    val progress: Float = 0f,
)
