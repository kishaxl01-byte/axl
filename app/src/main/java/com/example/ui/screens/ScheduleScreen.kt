package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calendar.CalendarIntegrationHelper
import com.example.data.local.entity.Course
import com.example.data.local.entity.ScheduleBlock
import com.example.ui.components.CourseTag
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ScheduleBlockDialog
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.viewmodel.StudyFlowViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduleBlocks by viewModel.scheduleBlocks.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedScheduleDay.collectAsStateWithLifecycle()

    val coursesMap = remember(courses) { courses.associateBy { it.id } }

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingBlock by remember { mutableStateOf<ScheduleBlock?>(null) }

    val days = listOf(
        Pair("Mon", 2),
        Pair("Tue", 3),
        Pair("Wed", 4),
        Pair("Thu", 5),
        Pair("Fri", 6),
        Pair("Sat", 7),
        Pair("Sun", 1)
    )

    val todayDayOfWeek = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }

    // Filtered schedule for selected day
    val dayBlocks = remember(scheduleBlocks, selectedDay) {
        scheduleBlocks.filter { block ->
            val blockDays = block.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            blockDays.contains(selectedDay)
        }.sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WEEKLY TIMETABLE",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Classes & Activities",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${dayBlocks.size} Events",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Day Selector Pills Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { (name, dayInt) ->
                    val isSelected = selectedDay == dayInt
                    val isToday = todayDayOfWeek == dayInt

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.setSelectedScheduleDay(dayInt) }
                            .padding(vertical = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }

            // Timeline Blocks List
            if (dayBlocks.isEmpty()) {
                EmptyStateCard(
                    title = "No classes or activities",
                    message = "No recurring blocks scheduled for this day.",
                    icon = Icons.Default.EventNote,
                    actionLabel = "+ Add Schedule Block",
                    onAction = { isAddDialogOpen = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(dayBlocks, key = { it.id }) { block ->
                        val course = block.courseId?.let { coursesMap[it] }
                        ScheduleBlockCard(
                            block = block,
                            course = course,
                            onEdit = { editingBlock = block },
                            onDelete = { viewModel.deleteScheduleBlock(block) },
                            onAddToCalendar = {
                                val intent = CalendarIntegrationHelper.openAddScheduleToCalendarIntent(context, block, course)
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Clean Minimalism FAB
        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_schedule_fab"),
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule", modifier = Modifier.size(26.dp))
        }
    }

    if (isAddDialogOpen || editingBlock != null) {
        ScheduleBlockDialog(
            initialBlock = editingBlock,
            courses = courses,
            onDismiss = {
                isAddDialogOpen = false
                editingBlock = null
            },
            onSave = { block ->
                viewModel.saveScheduleBlock(block)
                isAddDialogOpen = false
                editingBlock = null
            }
        )
    }
}

@Composable
fun ScheduleBlockCard(
    block: ScheduleBlock,
    course: Course?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit
) {
    fun formatTime(h: Int, m: Int): String {
        val period = if (h >= 12) "PM" else "AM"
        val dh = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format(Locale.getDefault(), "%d:%02d %s", dh, m, period)
    }

    val blockColor = try {
        Color(android.graphics.Color.parseColor(block.colorHex))
    } catch (_: Exception) {
        CleanPrimary
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
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
                // Color Bar Accent
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(blockColor)
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CourseTag(course = course)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = block.activityType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "${formatTime(block.startHour, block.startMinute)} - ${formatTime(block.endHour, block.endMinute)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (block.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = block.location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAddToCalendar, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Add to Calendar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
