package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StudySession
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.ui.theme.CleanTertiaryContainer
import com.example.viewmodel.StudyFlowViewModel
import com.example.viewmodel.TimerMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()

    val coursesMap = remember(courses) { courses.associateBy { it.id } }
    val assignmentsMap = remember(assignments) { assignments.associateBy { it.id } }

    var courseDropdownExpanded by remember { mutableStateOf(false) }
    var assignmentDropdownExpanded by remember { mutableStateOf(false) }

    val remainingMinutes = timerState.remainingSeconds / 60
    val remainingSecondsInMin = timerState.remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", remainingMinutes, remainingSecondsInMin)

    val progress = if (timerState.totalSeconds > 0) {
        1f - (timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat())
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

    val totalStudyMinutes = remember(studySessions) { studySessions.sumOf { it.durationMinutes } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOCUS ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Study Timer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🔥", fontSize = 14.sp)
                        Text(
                            text = "${totalStudyMinutes}m Total",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF31111D)
                        )
                    }
                }
            }
        }

        // Mode Selector Pills
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimerMode.values()) { mode ->
                    val isSelected = timerState.mode == mode
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { viewModel.setTimerMode(mode) }
                    ) {
                        Text(
                            text = mode.title,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Circular Timer Display (Clean Minimal styling)
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    // Track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress Arc
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (timerState.isRunning) "In Focus" else if (timerState.isPaused) "Paused" else "Ready",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Timer Control Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset Timer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause Primary Button
                Button(
                    onClick = {
                        if (timerState.isRunning) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .height(56.dp)
                        .width(140.dp)
                        .testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) "Pause" else "Start",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerState.isRunning) "Pause" else "Start",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Complete Session Early Button
                IconButton(
                    onClick = { viewModel.finishTimerSession() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Complete Session", tint = CleanEmerald)
                }
            }
        }

        // Tag Study Session to Course / Task
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SESSION TAGGING (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Course Dropdown
                    if (courses.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = courseDropdownExpanded,
                            onExpandedChange = { courseDropdownExpanded = !courseDropdownExpanded }
                        ) {
                            val selectedCourse = courses.find { it.id == timerState.selectedCourseId }
                            OutlinedTextField(
                                value = selectedCourse?.let { "${it.code}: ${it.name}" } ?: "Select Course / Subject",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Course Tag") },
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
                                    text = { Text("None (General Study)") },
                                    onClick = {
                                        viewModel.setTimerCourse(null)
                                        courseDropdownExpanded = false
                                    }
                                )
                                courses.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.code}: ${c.name}") },
                                        onClick = {
                                            viewModel.setTimerCourse(c.id)
                                            courseDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Assignment Dropdown
                    if (assignments.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = assignmentDropdownExpanded,
                            onExpandedChange = { assignmentDropdownExpanded = !assignmentDropdownExpanded }
                        ) {
                            val selectedAssignment = assignments.find { it.id == timerState.selectedAssignmentId }
                            OutlinedTextField(
                                value = selectedAssignment?.title ?: "Select Assignment / Task",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assignment Tag") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assignmentDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = assignmentDropdownExpanded,
                                onDismissRequest = { assignmentDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (General Review)") },
                                    onClick = {
                                        viewModel.setTimerAssignment(null)
                                        assignmentDropdownExpanded = false
                                    }
                                )
                                assignments.forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a.title) },
                                        onClick = {
                                            viewModel.setTimerAssignment(a.id)
                                            assignmentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Study History Logs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDY LOGS (${studySessions.size})",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (studySessions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Complete your first focus session to log study hours!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(studySessions.take(5)) { session ->
                val course = session.courseId?.let { coursesMap[it] }
                val assignment = session.assignmentId?.let { assignmentsMap[it] }
                val dateStr = remember(session.timestampMillis) {
                    val format = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    format.format(Date(session.timestampMillis))
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⏱", fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    text = course?.code ?: assignment?.title ?: "Focus Study Session",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$dateStr • ${session.durationMinutes} mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteStudySession(session) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
