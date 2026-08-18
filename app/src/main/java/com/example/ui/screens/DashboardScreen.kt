package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.calendar.CalendarIntegrationHelper
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.Course
import com.example.data.local.entity.ScheduleBlock
import com.example.ui.components.CountdownBadge
import com.example.ui.components.CourseTag
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.ui.theme.CleanTertiaryContainer
import com.example.viewmodel.StudyFlowViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: StudyFlowViewModel,
    onNavigateToAssignments: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToCalendarSync: () -> Unit,
    onOpenAddAssignment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val scheduleBlocks by viewModel.scheduleBlocks.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()

    val coursesMap = remember(courses) { courses.associateBy { it.id } }

    val todayCalendar = Calendar.getInstance()
    val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)

    // Today's schedule
    val todaySchedule = remember(scheduleBlocks, todayDayOfWeek) {
        scheduleBlocks.filter { block ->
            val days = block.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            days.contains(todayDayOfWeek)
        }.sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }

    // Pending assignments
    val urgentDeadlines = remember(assignments) {
        assignments.filter { it.status != AssignmentStatus.COMPLETED }
            .sortedBy { it.dueDateMillis }
            .take(4)
    }

    val remainingTasksCount = remember(assignments) {
        assignments.count { it.status != AssignmentStatus.COMPLETED }
    }

    // Stats
    val completedCount = remember(assignments) { assignments.count { it.status == AssignmentStatus.COMPLETED } }
    val totalCount = remember(assignments) { assignments.size }
    val gpa = remember(courses) { viewModel.calculateGPA(courses) }
    val totalFocusMinsToday = remember(studySessions) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        studySessions.filter { it.timestampMillis >= todayStart }.sumOf { it.durationMinutes }
    }

    val todayDateString = remember {
        val format = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        format.format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Clean Minimalism Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayDateString,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Student Planner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onNavigateToCourses() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎓",
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Hero Status Card (Clean Minimalism Spec: rounded-3xl #e8def8 container with IN SYNC pill)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_status_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CALENDAR SYNCED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Text(
                        text = if (remainingTasksCount == 0) "All Caught Up!" else "$remainingTasksCount Tasks Remaining",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Integrated with Device, Google & Canvas Calendars",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Upcoming Study Session Banner (Clean Minimalism Spec: rounded-3xl #ffd8e4 blush rose card)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFocus() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFB2BE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⏰", fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FOCUS SESSION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF31111D)
                        )
                        Text(
                            text = "Pomodoro Study • 25 Mins",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF31111D)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = Color(0xFF31111D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Target GPA",
                    value = "%.2f".format(gpa),
                    subtitle = "${courses.size} Enrolled Courses",
                    icon = Icons.Default.AutoGraph,
                    iconColor = CleanPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Focus Time",
                    value = "${totalFocusMinsToday}m",
                    subtitle = "Today's Study Log",
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Tasks Done",
                    value = "$completedCount/$totalCount",
                    subtitle = if (totalCount > 0) "${((completedCount.toFloat() / totalCount) * 100).toInt()}% Done" else "0%",
                    icon = Icons.Default.CheckCircle,
                    iconColor = CleanEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Class & Activity Timeline Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SCHEDULE (${todaySchedule.size})",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToSchedule) {
                    Text("Full Week →", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        if (todaySchedule.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No scheduled classes or activities today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToSchedule,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add to Schedule")
                        }
                    }
                }
            }
        } else {
            items(todaySchedule) { block ->
                val course = block.courseId?.let { coursesMap[it] }
                TodayScheduleItemCard(
                    block = block,
                    course = course,
                    onOpenCalendar = {
                        val intent = CalendarIntegrationHelper.openAddScheduleToCalendarIntent(context, block, course)
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
        }

        // Assignments Section (Clean Minimalism Spec: uppercase section header, rounded-2xl cards)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASSIGNMENTS & DEADLINES",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToAssignments) {
                    Text("View All →", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        if (urgentDeadlines.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CleanEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "All tasks completed!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You're all caught up on assignments and exams.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(urgentDeadlines) { assignment ->
                val course = assignment.courseId?.let { coursesMap[it] }
                DashboardAssignmentCard(
                    assignment = assignment,
                    course = course,
                    onToggleComplete = { viewModel.toggleAssignmentStatus(assignment) },
                    onOpenCalendar = {
                        val intent = CalendarIntegrationHelper.openAddEventToCalendarIntent(context, assignment, course)
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
        }

        // Calendar Sync Hub Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCalendarSync() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Calendar Integration & Export",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Export .ics file for Google, Apple & Canvas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Sync",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TodayScheduleItemCard(
    block: ScheduleBlock,
    course: Course?,
    onOpenCalendar: () -> Unit
) {
    fun formatTime(h: Int, m: Int): String {
        val period = if (h >= 12) "PM" else "AM"
        val dh = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format(Locale.getDefault(), "%d:%02d %s", dh, m, period)
    }

    val courseColor = try {
        Color(android.graphics.Color.parseColor(block.colorHex))
    } catch (_: Exception) {
        CleanPrimary
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(courseColor)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${formatTime(block.startHour, block.startMinute)} - ${formatTime(block.endHour, block.endMinute)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (block.location.isNotBlank()) {
                            Text(
                                text = "• ${block.location}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onOpenCalendar,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Add to Calendar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardAssignmentCard(
    assignment: Assignment,
    course: Course?,
    onToggleComplete: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val isDone = assignment.status == AssignmentStatus.COMPLETED

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Clean Minimal checkbox target
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = 2.dp,
                            color = if (isDone) CleanEmerald else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .background(if (isDone) CleanEmerald else Color.Transparent)
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CourseTag(course = course)
                        PriorityBadge(priority = assignment.priority)
                        CountdownBadge(dueDateMillis = assignment.dueDateMillis, isCompleted = isDone)
                    }
                }
            }

            IconButton(
                onClick = onOpenCalendar,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Add to Calendar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
