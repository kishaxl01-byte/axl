package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AssignmentDialog
import com.example.ui.screens.AssignmentsScreen
import com.example.ui.screens.CalendarSyncScreen
import com.example.ui.screens.CoursesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.StudyFlowTheme
import com.example.viewmodel.StudyFlowViewModel

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    TODAY("Today", Icons.Default.Home, "nav_today"),
    TASKS("Tasks", Icons.Default.Assignment, "nav_tasks"),
    SCHEDULE("Schedule", Icons.Default.Schedule, "nav_schedule"),
    FOCUS("Focus", Icons.Default.Timer, "nav_focus"),
    COURSES("Courses", Icons.Default.Class, "nav_courses"),
    SYNC("Sync", Icons.Default.CalendarMonth, "nav_sync")
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudyFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyFlowTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: StudyFlowViewModel) {
    var currentTab by remember { mutableStateOf(NavigationTab.TODAY) }
    var isQuickAddAssignmentOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            CleanMinimalismBottomNav(
                currentTab = currentTab,
                onSelectTab = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                NavigationTab.TODAY -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAssignments = { currentTab = NavigationTab.TASKS },
                    onNavigateToSchedule = { currentTab = NavigationTab.SCHEDULE },
                    onNavigateToFocus = { currentTab = NavigationTab.FOCUS },
                    onNavigateToCourses = { currentTab = NavigationTab.COURSES },
                    onNavigateToCalendarSync = { currentTab = NavigationTab.SYNC },
                    onOpenAddAssignment = { isQuickAddAssignmentOpen = true }
                )
                NavigationTab.TASKS -> AssignmentsScreen(viewModel = viewModel)
                NavigationTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                NavigationTab.FOCUS -> FocusTimerScreen(viewModel = viewModel)
                NavigationTab.COURSES -> CoursesScreen(viewModel = viewModel)
                NavigationTab.SYNC -> CalendarSyncScreen(viewModel = viewModel)
            }
        }
    }

    if (isQuickAddAssignmentOpen) {
        val courses = viewModel.courses.value
        AssignmentDialog(
            courses = courses,
            onDismiss = { isQuickAddAssignmentOpen = false },
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
                isQuickAddAssignmentOpen = false
            }
        )
    }
}

@Composable
fun CleanMinimalismBottomNav(
    currentTab: NavigationTab,
    onSelectTab: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (tab in NavigationTab.values()) {
                val isSelected = currentTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .clickable { onSelectTab(tab) }
                        .testTag(tab.tag)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Active Pill indicator (Clean Minimalism Spec: rounded-full #e8def8)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
