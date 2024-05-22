package com.example.study.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.study.domain.model.Session
import com.example.study.domain.model.Subject
import com.example.study.domain.model.Task

@Database(
    entities = [Subject::class, Task::class, Session::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(ColorListConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
}