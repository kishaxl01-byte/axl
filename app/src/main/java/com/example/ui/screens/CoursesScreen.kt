package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.Course
import com.example.ui.components.CourseDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatCard
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.viewmodel.StudyFlowViewModel

@Composable
fun CoursesScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }

    val totalCredits = remember(courses) { courses.sumOf { it.credits.toDouble() }.toFloat() }
    val gpa = remember(courses) { viewModel.calculateGPA(courses) }

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
                        text = "ACADEMICS",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Courses & GPA",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Academic Summary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Target GPA",
                    value = "%.2f".format(gpa),
                    subtitle = "Semester Estimate",
                    icon = Icons.Default.AutoGraph,
                    iconColor = CleanPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Credits",
                    value = "%.1f".format(totalCredits),
                    subtitle = "Enrolled Units",
                    icon = Icons.Default.School,
                    iconColor = CleanEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Courses",
                    value = "${courses.size}",
                    subtitle = "Active Subjects",
                    icon = Icons.Default.Class,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Course Cards List
            if (courses.isEmpty()) {
                EmptyStateCard(
                    title = "No courses added",
                    message = "Add your academic subjects to track assignments and schedule.",
                    icon = Icons.Default.Class,
                    actionLabel = "+ Add Course",
                    onAction = { isAddDialogOpen = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(courses, key = { it.id }) { course ->
                        val pendingCount = assignments.count { it.courseId == course.id && it.status != AssignmentStatus.COMPLETED }
                        CourseCardItem(
                            course = course,
                            pendingCount = pendingCount,
                            onEdit = { editingCourse = course },
                            onDelete = { viewModel.deleteCourse(course) }
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
                .testTag("add_course_fab"),
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Course", modifier = Modifier.size(26.dp))
        }
    }

    if (isAddDialogOpen || editingCourse != null) {
        CourseDialog(
            initialCourse = editingCourse,
            onDismiss = {
                isAddDialogOpen = false
                editingCourse = null
            },
            onSave = { course ->
                viewModel.saveCourse(course)
                isAddDialogOpen = false
                editingCourse = null
            }
        )
    }
}

@Composable
fun CourseCardItem(
    course: Course,
    pendingCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val courseColor = try {
        Color(android.graphics.Color.parseColor(course.colorHex))
    } catch (_: Exception) {
        CleanPrimary
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(courseColor)
                    )
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${course.credits} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CleanEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Target: ${course.targetGrade}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CleanEmerald
                    )
                }
            }

            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (course.instructor.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(course.instructor, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (course.roomLocation.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(course.roomLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (course.notes.isNotBlank()) {
                Text(
                    text = course.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (pendingCount == 0) "All tasks done" else "$pendingCount pending tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (pendingCount > 0) MaterialTheme.colorScheme.primary else CleanEmerald,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
