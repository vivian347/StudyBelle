package com.example.study.domain.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.study.presentation.theme.coralToLightGreyGradient
import com.example.study.presentation.theme.coralToLightYellowGradient
import com.example.study.presentation.theme.coralToPeachGradient

@Entity
data class Subject(
    val name: String,
    val goalHours: Float,
    val colors: List<Int>,
    @PrimaryKey(autoGenerate = true)
    val subjectId: Int? = null
){
    companion object{
        val subjectCardColors = listOf(coralToLightGreyGradient, coralToPeachGradient, coralToLightYellowGradient)
    }
}
