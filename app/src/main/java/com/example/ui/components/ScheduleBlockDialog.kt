package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.ActivityType
import com.example.data.local.entity.Course
import com.example.data.local.entity.ScheduleBlock
import com.example.ui.theme.CleanPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleBlockDialog(
    initialBlock: ScheduleBlock? = null,
    courses: List<Course>,
    onDismiss: () -> Unit,
    onSave: (ScheduleBlock) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialBlock?.title ?: "") }
    var selectedCourseId by remember { mutableStateOf<Long?>(initialBlock?.courseId ?: courses.firstOrNull()?.id) }
    var activityType by remember { mutableStateOf(initialBlock?.activityType ?: ActivityType.LECTURE) }

    // Day of week checkboxes (1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat)
    val selectedDays = remember {
        val initialDays = initialBlock?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: listOf(2, 4, 6)
        mutableStateListOf<Int>().apply { addAll(initialDays) }
    }

    var startHour by remember { mutableIntStateOf(initialBlock?.startHour ?: 10) }
    var startMinute by remember { mutableIntStateOf(initialBlock?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialBlock?.endHour ?: 11) }
    var endMinute by remember { mutableIntStateOf(initialBlock?.endMinute ?: 15) }

    var location by remember { mutableStateOf(initialBlock?.location ?: "") }
    var instructor by remember { mutableStateOf(initialBlock?.instructor ?: "") }
    var notes by remember { mutableStateOf(initialBlock?.notes ?: "") }

    var courseDropdownExpanded by remember { mutableStateOf(false) }

    val daysList = listOf(
        Pair("Mon", 2),
        Pair("Tue", 3),
        Pair("Wed", 4),
        Pair("Thu", 5),
        Pair("Fri", 6),
        Pair("Sat", 7),
        Pair("Sun", 1)
    )

    fun formatTime(hour: Int, min: Int): String {
        val period = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, min, period)
    }

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
                    text = if (initialBlock == null) "Schedule Class / Activity" else "Edit Schedule Block",
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity / Class Title *") },
                    placeholder = { Text("e.g. Physics 101 Lecture, Chem Lab, Library Study") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Course association (optional)
                if (courses.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = courseDropdownExpanded,
                        onExpandedChange = { courseDropdownExpanded = !courseDropdownExpanded }
                    ) {
                        val currentCourse = courses.find { it.id == selectedCourseId }
                        OutlinedTextField(
                            value = currentCourse?.let { "${it.code} - ${it.name}" } ?: "General / Extracurricular (No course)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Associated Course (Optional)") },
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
                            DropdownMenuItem(
                                text = { Text("None (General Study / Activity)") },
                                onClick = {
                                    selectedCourseId = null
                                    courseDropdownExpanded = false
                                }
                            )
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text("${course.code}: ${course.name}") },
                                    onClick = {
                                        selectedCourseId = course.id
                                        courseDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Activity Type
                Text(
                    text = "Type of Activity",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActivityType.values().forEach { type ->
                        FilterChip(
                            selected = activityType == type,
                            onClick = { activityType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Days of week multi-select
                Text(
                    text = "Days of Week *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysList.forEach { (label, dayInt) ->
                        val isSelected = selectedDays.contains(dayInt)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    if (isSelected) selectedDays.remove(dayInt) else selectedDays.add(dayInt)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Start & End Time Pickers
                Text(
                    text = "Start & End Time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    startHour = h
                                    startMinute = m
                                },
                                startHour,
                                startMinute,
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(formatTime(startHour, startMinute), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    endHour = h
                                    endMinute = m
                                },
                                endHour,
                                endMinute,
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(formatTime(endHour, endMinute), fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Room") },
                        placeholder = { Text("e.g. Room 204 / Zoom") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor / Host") },
                        placeholder = { Text("e.g. Dr. Vance") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Class Links") },
                    placeholder = { Text("Zoom link, passcode, textbook requirements...") },
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
                    if (title.isNotBlank() && selectedDays.isNotEmpty()) {
                        val daysString = selectedDays.sorted().joinToString(",")
                        val currentCourse = courses.find { it.id == selectedCourseId }
                        val block = (initialBlock ?: ScheduleBlock(title = "", daysOfWeek = "")).copy(
                            title = title.trim(),
                            courseId = selectedCourseId,
                            activityType = activityType,
                            daysOfWeek = daysString,
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            location = location.trim(),
                            instructor = instructor.trim(),
                            colorHex = currentCourse?.colorHex ?: "#6750A4",
                            notes = notes.trim()
                        )
                        onSave(block)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && selectedDays.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_schedule_button")
            ) {
                Text(if (initialBlock == null) "Add to Schedule" else "Save Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
