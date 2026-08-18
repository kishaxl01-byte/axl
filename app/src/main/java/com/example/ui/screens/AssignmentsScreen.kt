package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calendar.CalendarIntegrationHelper
import com.example.data.local.Converters
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.Course
import com.example.data.local.entity.Priority
import com.example.ui.components.AssignmentDialog
import com.example.ui.components.CountdownBadge
import com.example.ui.components.CourseTag
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SubtaskProgressBar
import com.example.ui.components.TypeBadge
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.viewmodel.AssignmentFilterTab
import com.example.viewmodel.AssignmentSort
import com.example.viewmodel.StudyFlowViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssignmentsScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assignments by viewModel.filteredAssignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCourseFilterId by viewModel.selectedCourseFilterId.collectAsStateWithLifecycle()
    val selectedPriorityFilter by viewModel.selectedPriorityFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    val coursesMap = remember(courses) { courses.associateBy { it.id } }

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = "TASK MANAGER",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Assignments & Exams",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box {
                    IconButton(
                        onClick = { isSortMenuOpen = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Sort", modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = isSortMenuOpen,
                        onDismissRequest = { isSortMenuOpen = false }
                    ) {
                        AssignmentSort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.title, fontWeight = if (sortBy == sort) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setSortBy(sort)
                                    isSortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search assignments, notes, topics...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Filter Tabs (Clean Minimalism Tabs)
            ScrollableTabRow(
                selectedTabIndex = AssignmentFilterTab.values().indexOf(selectedTab),
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[AssignmentFilterTab.values().indexOf(selectedTab)]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                containerColor = Color.Transparent
            ) {
                AssignmentFilterTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Course Filter Chips Row
            if (courses.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCourseFilterId == null,
                            onClick = { viewModel.setCourseFilter(null) },
                            label = { Text("All Courses", fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    items(courses) { course ->
                        FilterChip(
                            selected = selectedCourseFilterId == course.id,
                            onClick = { viewModel.setCourseFilter(if (selectedCourseFilterId == course.id) null else course.id) },
                            label = { Text(course.code, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Assignment List
            if (assignments.isEmpty()) {
                EmptyStateCard(
                    title = "No assignments found",
                    message = if (searchQuery.isNotBlank()) "No tasks match your search filter." else "You have no tasks in this tab.",
                    icon = Icons.Default.Assignment,
                    actionLabel = "+ Create Assignment",
                    onAction = { isAddDialogOpen = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(assignments, key = { it.id }) { assignment ->
                        val course = assignment.courseId?.let { coursesMap[it] }
                        AssignmentCardItem(
                            assignment = assignment,
                            course = course,
                            onToggleStatus = { viewModel.toggleAssignmentStatus(assignment) },
                            onToggleSubtask = { subtaskId -> viewModel.toggleSubtask(assignment, subtaskId) },
                            onEdit = { editingAssignment = assignment },
                            onDelete = { viewModel.deleteAssignment(assignment) },
                            onAddToCalendar = {
                                val intent = CalendarIntegrationHelper.openAddEventToCalendarIntent(context, assignment, course)
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

        // Clean Minimalism Floating Action Button (#e8def8 container, rounded-2xl)
        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_assignment_fab"),
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Assignment", modifier = Modifier.size(26.dp))
        }
    }

    // Add / Edit Assignment Dialog
    if (isAddDialogOpen || editingAssignment != null) {
        AssignmentDialog(
            initialAssignment = editingAssignment,
            courses = courses,
            onDismiss = {
                isAddDialogOpen = false
                editingAssignment = null
            },
            onSave = { id, title, courseId, type, priority, status, dueDate, estMins, subtasks, notes, reminder, advanceMins ->
                viewModel.saveAssignment(
                    id = id,
                    title = title,
                    courseId = courseId,
                    type = type,
                    priority = priority,
                    status = status,
                    dueDateMillis = dueDate,
                    estimatedMinutes = estMins,
                    subtasks = subtasks,
                    notes = notes,
                    reminderEnabled = reminder,
                    reminderAdvanceMinutes = advanceMins
                )
                isAddDialogOpen = false
                editingAssignment = null
            }
        )
    }
}

@Composable
fun AssignmentCardItem(
    assignment: Assignment,
    course: Course?,
    onToggleStatus: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit
) {
    val isDone = assignment.status == AssignmentStatus.COMPLETED
    val subtasks = remember(assignment.subtasksJson) { Converters.jsonToSubtasks(assignment.subtasksJson) }
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Custom Checkbox
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = 2.dp,
                                color = if (isDone) CleanEmerald else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(if (isDone) CleanEmerald else Color.Transparent)
                            .clickable { onToggleStatus() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = assignment.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CourseTag(course = course)
                            TypeBadge(type = assignment.type)
                            PriorityBadge(priority = assignment.priority)
                        }
                    }
                }

                CountdownBadge(dueDateMillis = assignment.dueDateMillis, isCompleted = isDone)
            }

            // Subtasks Progress Bar
            if (subtasks.isNotEmpty()) {
                SubtaskProgressBar(subtasks = subtasks)
            }

            // Expandable details (Subtasks checklist items, notes, reminder info, action buttons)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Subtask Items
                    if (subtasks.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subtasks.forEach { subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleSubtask(subtask.id) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(
                                                width = 1.5.dp,
                                                color = if (subtask.isCompleted) CleanEmerald else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .background(if (subtask.isCompleted) CleanEmerald else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (subtask.isCompleted) {
                                            Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text(
                                        text = subtask.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }

                    // Notes
                    if (assignment.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📝 ${assignment.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Estimated Duration & Reminder Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱ Est: ${assignment.estimatedMinutes} mins",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (assignment.reminderEnabled) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Alarm Active (${assignment.reminderAdvanceMinutes / 60}h before)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

            // Expand / Collapse row toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide details" else "View details (${subtasks.size} subtasks)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
