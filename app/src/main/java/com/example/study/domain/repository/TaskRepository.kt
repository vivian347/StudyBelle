package com.example.study.domain.repository

import com.example.study.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun insertTask(task: Task)

    suspend fun deleteTask(taskId: Int)

    suspend fun getTaskById(taskId: Int): Task?

    fun getUpcomingTasksForSubject(subjectId: Int): Flow<List<Task>>

    fun getCompletedTasksForSubject(subjectId: Int): Flow<List<Task>>

    fun getAllUpcomingTasks(): Flow<List<Task>>
}