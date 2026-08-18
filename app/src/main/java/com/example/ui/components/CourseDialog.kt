package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Course
import com.example.ui.theme.CourseColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseDialog(
    initialCourse: Course? = null,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var code by remember { mutableStateOf(initialCourse?.code ?: "") }
    var name by remember { mutableStateOf(initialCourse?.name ?: "") }
    var instructor by remember { mutableStateOf(initialCourse?.instructor ?: "") }
    var roomLocation by remember { mutableStateOf(initialCourse?.roomLocation ?: "") }
    var creditsText by remember { mutableStateOf((initialCourse?.credits ?: 3.0f).toString()) }
    var targetGrade by remember { mutableStateOf(initialCourse?.targetGrade ?: "A") }
    var term by remember { mutableStateOf(initialCourse?.term ?: "Fall 2026") }
    var notes by remember { mutableStateOf(initialCourse?.notes ?: "") }
    var selectedColorHex by remember { mutableStateOf(initialCourse?.colorHex ?: "#4F46E5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialCourse == null) "Add Course / Subject" else "Edit Course",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code *") },
                        placeholder = { Text("e.g. CS 101") },
                        modifier = Modifier
                            .weight(0.4f)
                            .testTag("course_code_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it },
                        label = { Text("Credits") },
                        placeholder = { Text("3.0") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = targetGrade,
                        onValueChange = { targetGrade = it },
                        label = { Text("Target") },
                        placeholder = { Text("A") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course / Subject Name *") },
                    placeholder = { Text("e.g. Introduction to Computer Science") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor / Professor") },
                        placeholder = { Text("Dr. Smith") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = roomLocation,
                        onValueChange = { roomLocation = it },
                        label = { Text("Room / Hall") },
                        placeholder = { Text("Hall 204") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text(
                    text = "Theme Tag Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CourseColors.forEach { color ->
                        val hex = String.format("#%06X", (0xFFFFFF and color.value.toInt()))
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Syllabus / Office Hours / Notes") },
                    placeholder = { Text("e.g. Office hours on Tuesdays 3-5 PM on Zoom...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && name.isNotBlank()) {
                        val parsedCredits = creditsText.toFloatOrNull() ?: 3.0f
                        val course = (initialCourse ?: Course(code = "", name = "")).copy(
                            code = code.trim(),
                            name = name.trim(),
                            instructor = instructor.trim(),
                            roomLocation = roomLocation.trim(),
                            credits = parsedCredits,
                            targetGrade = targetGrade.trim(),
                            term = term.trim(),
                            colorHex = selectedColorHex,
                            notes = notes.trim()
                        )
                        onSave(course)
                        onDismiss()
                    }
                },
                enabled = code.isNotBlank() && name.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_course_button")
            ) {
                Text(if (initialCourse == null) "Add Course" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
