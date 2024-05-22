package com.example.study.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.study.presentation.theme.blue
import com.example.study.presentation.theme.green
import com.example.study.presentation.theme.salmon
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


enum class Priority(val title: String, val color: Color, val value: Int) {
    LOW(title="Low", color = green, value = 0),
    MEDIUM(title="Medium", color = blue, value = 1),
    HIGH(title="High", color = salmon, value = 2);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull {it.value == value} ?: MEDIUM
    }
}

fun Long?.changeMillisToDateString(): String {
    val date: LocalDate = this?.let {
        Instant
            .ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } ?: LocalDate.now()
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

fun Long.toHours(): Float {
    val hours = this.toFloat() / 3600f
    return "%.2f".format(hours).toFloat()
}

sealed class SnackbarEvent {
    data class  ShowSnackBar(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarEvent()

    data object NavigateUp: SnackbarEvent()
}

fun Int.pad(): String{
    return this.toString().padStart(length = 2, padChar = '0')
}