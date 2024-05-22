package com.example.study.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.study.domain.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Upsert
    suspend fun insertSubject(subject: Subject)

    @Query("SELECT COUNT(*) FROM SUBJECT")
    fun getTotalSubjectCount(): Flow<Int>

    @Query("SELECT SUM(goalHours) FROM SUBJECT")
    fun getTotalGoalHours(): Flow<Float>

    @Query("SELECT * FROM SUBJECT WHERE subjectId = :subjectId")
    suspend fun getSubjectById(subjectId: Int): Subject?

    @Query("DELETE FROM SUBJECT WHERE subjectId = :subjectId")
    suspend fun deleteSubject(subjectId: Int)

    @Query("SELECT * FROM SUBJECT")
    fun getAllSubject(): Flow<List<Subject>>
}