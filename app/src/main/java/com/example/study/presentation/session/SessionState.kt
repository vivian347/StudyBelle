package com.example.study.presentation.session

import com.example.study.domain.model.Session
import com.example.study.domain.model.Subject

data class SessionState(
    val subjects: List<Subject> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val relatedSubject: String? = null,
    val subjectId: Int? = null,
    val session: Session? = null
)
