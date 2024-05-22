package com.example.study.domain.repository

import com.example.study.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    suspend fun insertSubject(subject: Subject)

    fun getTotalSubjectCount(): Flow<Int>

    fun getTotalGoalHours(): Flow<Float>

    suspend fun getSubjectById(subjectId: Int): Subject?

    suspend fun deleteSubject(subjectId: Int)

    fun getAllSubject(): Flow<List<Subject>>
}