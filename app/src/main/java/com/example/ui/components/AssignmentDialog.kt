package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.Converters
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.AssignmentType
import com.example.data.local.entity.Course
import com.example.data.local.entity.Priority
import com.example.data.local.entity.Subtask
import com.example.ui.theme.CleanAmber
import com.example.ui.theme.CleanCoral
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssignmentDialog(
    initialAssignment: Assignment? = null,
    courses: List<Course>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        courseId: Long?,
        type: AssignmentType,
        priority: Priority,
        status: AssignmentStatus,
        dueDateMillis: Long,
        estimatedMinutes: Int,
        subtasks: List<Subtask>,
        notes: String,
        reminderEnabled: Boolean,
        reminderAdvanceMinutes: Int
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialAssignment?.title ?: "") }
    var selectedCourseId by remember { mutableStateOf<Long?>(initialAssignment?.courseId ?: courses.firstOrNull()?.id) }
    var selectedType by remember { mutableStateOf(initialAssignment?.type ?: AssignmentType.HOMEWORK) }
    var selectedPriority by remember { mutableStateOf(initialAssignment?.priority ?: Priority.MEDIUM) }
    var selectedStatus by remember { mutableStateOf(initialAssignment?.status ?: AssignmentStatus.TODO) }

    // Date & Time
    val defaultDueDate = remember {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 2)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
        }
        initialAssignment?.dueDateMillis ?: cal.timeInMillis
    }
    var dueDateMillis by remember { mutableLongStateOf(defaultDueDate) }

    var estimatedMinutes by remember { mutableFloatStateOf((initialAssignment?.estimatedMinutes ?: 60).toFloat()) }
    var notes by remember { mutableStateOf(initialAssignment?.notes ?: "") }
    var reminderEnabled by remember { mutableStateOf(initialAssignment?.reminderEnabled ?: true) }
    var reminderAdvanceMinutes by remember { mutableStateOf(initialAssignment?.reminderAdvanceMinutes ?: 1440) }

    // Subtasks checklist builder
    val subtasks = remember {
        val initialList = if (initialAssignment != null) Converters.jsonToSubtasks(initialAssignment.subtasksJson) else emptyList()
        mutableStateListOf<Subtask>().apply { addAll(initialList) }
    }
    var newSubtaskText by remember { mutableStateOf("") }

    var courseDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialAssignment == null) "New Assignment / Exam" else "Edit Assignment",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task / Assignment Title *") },
                    placeholder = { Text("e.g. Chapter 4 Problem Set, Midterm Exam...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assignment_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Course Selector
                if (courses.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = courseDropdownExpanded,
                        onExpandedChange = { courseDropdownExpanded = !courseDropdownExpanded }
                    ) {
                        val currentCourse = courses.find { it.id == selectedCourseId }
                        OutlinedTextField(
                            value = currentCourse?.let { "${it.code} - ${it.name}" } ?: "Select Subject / Course",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Course / Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = courseDropdownExpanded,
                            onDismissRequest = { courseDropdownExpanded = false }
                        ) {
                            courses.forEach { course ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(course.colorHex))
                                } catch (_: Exception) {
                                    CleanPrimary
                                }
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text("${course.code}: ${course.name}")
                                        }
                                    },
                                    onClick = {
                                        selectedCourseId = course.id
                                        courseDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Type Chips
                Text(
                    text = "Assignment Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssignmentType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Priority Selection
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(Priority.LOW, CleanEmerald, "Low"),
                        Triple(Priority.MEDIUM, CleanPrimary, "Medium"),
                        Triple(Priority.HIGH, CleanAmber, "High"),
                        Triple(Priority.URGENT, CleanCoral, "Urgent")
                    ).forEach { (priority, pColor, pLabel) ->
                        val isSelected = selectedPriority == priority

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) pColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) pColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPriority = priority }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pLabel,
                                color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Due Date & Time Pickers
                Text(
                    text = "Due Date & Time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Button
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dueDateMillis
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    dueDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateFormat.format(Date(dueDateMillis)), fontSize = 12.sp, maxLines = 1)
                    }

                    // Time Button
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dueDateMillis
                                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                    }
                                    dueDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(timeFormat.format(Date(dueDateMillis)), fontSize = 12.sp, maxLines = 1)
                    }
                }

                // Estimated Study Time Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimated Study / Work Time",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${estimatedMinutes.toInt()} mins (${"%.1f".format(estimatedMinutes / 60f)} hrs)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = estimatedMinutes,
                        onValueChange = { estimatedMinutes = it },
                        valueRange = 15f..360f,
                        steps = 22
                    )
                }

                // Subtask Checklist Builder
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Subtasks / Action Checklist (${subtasks.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    subtasks.forEachIndexed { index, subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. ${subtask.text}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { subtasks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove subtask",
                                    tint = CleanCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            placeholder = { Text("Add step (e.g. Draft Intro, Revise)", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(
                            onClick = {
                                if (newSubtaskText.isNotBlank()) {
                                    subtasks.add(Subtask(id = UUID.randomUUID().toString(), text = newSubtaskText.trim()))
                                    newSubtaskText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add subtask", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Notification Reminder Toggle & Advance selection
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Deadline Alarm & Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it }
                            )
                        }

                        if (reminderEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Remind me before deadline:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    Pair("15 mins", 15),
                                    Pair("1 hour", 60),
                                    Pair("2 hours", 120),
                                    Pair("1 day", 1440),
                                    Pair("2 days", 2880)
                                ).forEach { (label, mins) ->
                                    FilterChip(
                                        selected = reminderAdvanceMinutes == mins,
                                        onClick = { reminderAdvanceMinutes = mins },
                                        label = { Text(label, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Submission Instructions") },
                    placeholder = { Text("Rubric requirements, Zoom link, or submission portal details...") },
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
                    if (title.isNotBlank()) {
                        onSave(
                            initialAssignment?.id ?: 0L,
                            title,
                            selectedCourseId,
                            selectedType,
                            selectedPriority,
                            selectedStatus,
                            dueDateMillis,
                            estimatedMinutes.toInt(),
                            subtasks.toList(),
                            notes,
                            reminderEnabled,
                            reminderAdvanceMinutes
                        )
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_assignment_button")
            ) {
                Text(if (initialAssignment == null) "Create Assignment" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
