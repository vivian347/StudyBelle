package com.example.study.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.study.domain.model.Subject

@Composable
fun AddSubjectDialog(
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
    title: String = "Add/Update Subject",
    isOpen: Boolean,
    selectedColors: List<Color>,
    onColorChange: (List<Color>) -> Unit,
    subjectName: String,
    goalHour: String,
    onSubjectNameChange: (String) -> Unit,
    onGoalHourChange: (String) -> Unit
) {

    var subjectNameError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var goalHourError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    subjectNameError = when{
        subjectName.isBlank() -> "Enter subject name"
        subjectName.length < 2 -> "Subject name is too short"
        subjectName.length > 20 -> "Subject name is too long"
        else -> null
    }

    goalHourError = when {
        goalHour.isBlank() -> "Enter study hours goal"
        goalHour.toFloatOrNull() == null -> "Invalid number"
        goalHour.toFloat() < 1f -> "Set at least 1 hour"
        goalHour.toFloat() > 100f -> "Set a maximum of 100 hours"
        else -> null
    }

    if(isOpen){
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Subject.subjectCardColors.forEach{ colors ->
                            Box(modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = if (colors == selectedColors) Color.DarkGray
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                                .background(brush = Brush.verticalGradient(colors))
                                .clickable { onColorChange(colors) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = { Text(text = "Subject Name") },
                        singleLine = true,
                        isError = subjectNameError != null && subjectName.isNotBlank(),
                        supportingText = {
                            Text(text = subjectNameError.orEmpty())
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = goalHour,
                        onValueChange = onGoalHourChange,
                        label = { Text(text = "Study Hours Goal") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = goalHourError != null && goalHour.isNotBlank(),
                        supportingText = {
                            Text(text = goalHourError.orEmpty())
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmButtonClicked,
                    enabled = subjectNameError == null && goalHourError == null
                ) {
                    Text(text = "Save")
                }
            }

        )
    }

}