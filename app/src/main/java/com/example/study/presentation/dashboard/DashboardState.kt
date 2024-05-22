package com.example.study.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.example.study.domain.model.Session
import com.example.study.domain.model.Subject

data class DashboardState(
    val totalSubjectCount: Int = 0,
    val totalHoursStudied: Float = 0f,
    val totalStudyHoursGoal: Float = 0f,
    val subjects: List<Subject> = emptyList(),
    val subjectName: String = "",
    val studyHoursGoal: String = "",
    val subjectCardColors: List<Color> = Subject.subjectCardColors.random(),
    val session: Session? = null
)
