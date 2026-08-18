package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyFlowApp
import com.example.calendar.CalendarIntegrationHelper
import com.example.data.local.Converters
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.AssignmentType
import com.example.data.local.entity.Course
import com.example.data.local.entity.Priority
import com.example.data.local.entity.ScheduleBlock
import com.example.data.local.entity.StudySession
import com.example.data.local.entity.Subtask
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class AssignmentFilterTab(val title: String) {
    UPCOMING("Upcoming"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    OVERDUE("Overdue"),
    COMPLETED("Completed"),
    ALL("All Tasks")
}

enum class AssignmentSort(val title: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority (High to Low)"),
    ESTIMATED_TIME("Estimated Time"),
    COURSE("Course")
}

enum class TimerMode(val title: String, val defaultMinutes: Int) {
    POMODORO("Pomodoro (25m)", 25),
    DEEP_WORK("Deep Focus (50m)", 50),
    SHORT_BREAK("Short Break (5m)", 5),
    LONG_BREAK("Long Break (15m)", 15),
    CUSTOM("Custom", 30)
}

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val mode: TimerMode = TimerMode.POMODORO,
    val selectedCourseId: Long? = null,
    val selectedAssignmentId: Long? = null,
    val focusScore: Int = 5,
    val sessionNotes: String = ""
)

private data class FilterParams(
    val tab: AssignmentFilterTab,
    val courseId: Long?,
    val priority: Priority?,
    val query: String,
    val sort: AssignmentSort
)

class StudyFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as StudyFlowApp).repository
    private val context: Context get() = getApplication()

    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<Assignment>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleBlocks: StateFlow<List<ScheduleBlock>> = repository.allScheduleBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySession>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Filters for Assignments
    private val _selectedTab = MutableStateFlow(AssignmentFilterTab.UPCOMING)
    val selectedTab: StateFlow<AssignmentFilterTab> = _selectedTab.asStateFlow()

    private val _selectedCourseFilterId = MutableStateFlow<Long?>(null)
    val selectedCourseFilterId: StateFlow<Long?> = _selectedCourseFilterId.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<Priority?>(null)
    val selectedPriorityFilter: StateFlow<Priority?> = _selectedPriorityFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(AssignmentSort.DUE_DATE)
    val sortBy: StateFlow<AssignmentSort> = _sortBy.asStateFlow()

    // Schedule UI State
    private val _selectedScheduleDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
    val selectedScheduleDay: StateFlow<Int> = _selectedScheduleDay.asStateFlow()

    // Timer State
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // Combined filter params
    private val filterParamsFlow = combine(
        selectedTab,
        selectedCourseFilterId,
        selectedPriorityFilter,
        searchQuery,
        sortBy
    ) { tab, courseId, priority, query, sort ->
        FilterParams(tab, courseId, priority, query, sort)
    }

    // Filtered Assignments Flow
    val filteredAssignments: StateFlow<List<Assignment>> = combine(
        assignments,
        filterParamsFlow
    ) { list, params ->
        val now = System.currentTimeMillis()

        // Today start/end
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // End of week
        val weekEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }.timeInMillis

        var filtered = list.filter { item ->
            val matchesCourse = (params.courseId == null || item.courseId == params.courseId)
            val matchesPriority = (params.priority == null || item.priority == params.priority)
            val matchesQuery = params.query.isBlank() || item.title.contains(params.query, ignoreCase = true) || item.notes.contains(params.query, ignoreCase = true)
            matchesCourse && matchesPriority && matchesQuery
        }

        filtered = when (params.tab) {
            AssignmentFilterTab.UPCOMING -> filtered.filter { it.status != AssignmentStatus.COMPLETED && it.dueDateMillis >= now }
            AssignmentFilterTab.TODAY -> filtered.filter { it.dueDateMillis in todayStart..todayEnd }
            AssignmentFilterTab.THIS_WEEK -> filtered.filter { it.dueDateMillis in todayStart..weekEnd }
            AssignmentFilterTab.OVERDUE -> filtered.filter { it.status != AssignmentStatus.COMPLETED && it.dueDateMillis < now }
            AssignmentFilterTab.COMPLETED -> filtered.filter { it.status == AssignmentStatus.COMPLETED }
            AssignmentFilterTab.ALL -> filtered
        }

        when (params.sort) {
            AssignmentSort.DUE_DATE -> filtered.sortedBy { it.dueDateMillis }
            AssignmentSort.PRIORITY -> filtered.sortedByDescending { it.priority.level }
            AssignmentSort.ESTIMATED_TIME -> filtered.sortedByDescending { it.estimatedMinutes }
            AssignmentSort.COURSE -> filtered.sortedBy { it.courseId ?: Long.MAX_VALUE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Setters for filters
    fun setSelectedTab(tab: AssignmentFilterTab) { _selectedTab.value = tab }
    fun setCourseFilter(courseId: Long?) { _selectedCourseFilterId.value = courseId }
    fun setPriorityFilter(priority: Priority?) { _selectedPriorityFilter.value = priority }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortBy(sort: AssignmentSort) { _sortBy.value = sort }
    fun setSelectedScheduleDay(dayOfWeek: Int) { _selectedScheduleDay.value = dayOfWeek }

    // Assignment Operations
    fun saveAssignment(
        id: Long = 0,
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
    ) {
        viewModelScope.launch {
            val assignment = Assignment(
                id = id,
                title = title.trim(),
                courseId = courseId,
                type = type,
                priority = priority,
                status = status,
                dueDateMillis = dueDateMillis,
                estimatedMinutes = estimatedMinutes,
                subtasksJson = Converters.subtasksToJson(subtasks),
                notes = notes.trim(),
                reminderEnabled = reminderEnabled,
                reminderAdvanceMinutes = reminderAdvanceMinutes
            )

            val savedId = if (id == 0L) {
                repository.insertAssignment(assignment)
            } else {
                repository.updateAssignment(assignment)
                id
            }

            // Update Notification Alarm
            val course = courseId?.let { repository.getCourseById(it) }
            val updated = assignment.copy(id = savedId)
            if (reminderEnabled && status != AssignmentStatus.COMPLETED) {
                NotificationHelper.scheduleAssignmentReminder(context, updated, course?.code ?: course?.name)
            } else {
                NotificationHelper.cancelAssignmentReminder(context, savedId)
            }
        }
    }

    fun toggleAssignmentStatus(assignment: Assignment) {
        viewModelScope.launch {
            val newStatus = when (assignment.status) {
                AssignmentStatus.COMPLETED -> AssignmentStatus.TODO
                AssignmentStatus.TODO -> AssignmentStatus.IN_PROGRESS
                AssignmentStatus.IN_PROGRESS -> AssignmentStatus.COMPLETED
            }
            val completionTime = if (newStatus == AssignmentStatus.COMPLETED) System.currentTimeMillis() else null
            repository.updateAssignmentStatus(assignment.id, newStatus, completionTime)

            if (newStatus == AssignmentStatus.COMPLETED) {
                NotificationHelper.cancelAssignmentReminder(context, assignment.id)
            } else if (assignment.reminderEnabled) {
                val course = assignment.courseId?.let { repository.getCourseById(it) }
                NotificationHelper.scheduleAssignmentReminder(context, assignment.copy(status = newStatus), course?.code ?: course?.name)
            }
        }
    }

    fun toggleSubtask(assignment: Assignment, subtaskId: String) {
        viewModelScope.launch {
            val subtasks = Converters.jsonToSubtasks(assignment.subtasksJson).map {
                if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            val allDone = subtasks.isNotEmpty() && subtasks.all { it.isCompleted }
            val newStatus = if (allDone) AssignmentStatus.COMPLETED else if (subtasks.any { it.isCompleted }) AssignmentStatus.IN_PROGRESS else assignment.status

            val updated = assignment.copy(
                subtasksJson = Converters.subtasksToJson(subtasks),
                status = newStatus,
                completionDateMillis = if (allDone) System.currentTimeMillis() else assignment.completionDateMillis
            )
            repository.updateAssignment(updated)
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            NotificationHelper.cancelAssignmentReminder(context, assignment.id)
            repository.deleteAssignment(assignment)
        }
    }

    // Course Operations
    fun saveCourse(course: Course) {
        viewModelScope.launch {
            if (course.id == 0L) {
                repository.insertCourse(course)
            } else {
                repository.updateCourse(course)
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    // Schedule Block Operations
    fun saveScheduleBlock(block: ScheduleBlock) {
        viewModelScope.launch {
            if (block.id == 0L) {
                repository.insertScheduleBlock(block)
            } else {
                repository.updateScheduleBlock(block)
            }
        }
    }

    fun deleteScheduleBlock(block: ScheduleBlock) {
        viewModelScope.launch {
            repository.deleteScheduleBlock(block)
        }
    }

    // Timer Operations
    fun setTimerMode(mode: TimerMode, customMinutes: Int = 25) {
        val minutes = if (mode == TimerMode.CUSTOM) customMinutes else mode.defaultMinutes
        val seconds = minutes * 60
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            mode = mode,
            totalSeconds = seconds,
            remainingSeconds = seconds
        )
    }

    fun setTimerCourse(courseId: Long?) {
        _timerState.value = _timerState.value.copy(selectedCourseId = courseId)
    }

    fun setTimerAssignment(assignmentId: Long?) {
        _timerState.value = _timerState.value.copy(selectedAssignmentId = assignmentId)
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isRunning = true, isPaused = false)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000)
                val newRemaining = _timerState.value.remainingSeconds - 1
                _timerState.value = _timerState.value.copy(remainingSeconds = newRemaining)
            }
            if (_timerState.value.remainingSeconds <= 0 && _timerState.value.isRunning) {
                finishTimerSession()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false, isPaused = true)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = _timerState.value.totalSeconds
        )
    }

    fun finishTimerSession(notes: String = "", focusScore: Int = 5) {
        timerJob?.cancel()
        val durationMins = (_timerState.value.totalSeconds - _timerState.value.remainingSeconds) / 60
        val actualDuration = if (durationMins > 0) durationMins else _timerState.value.totalSeconds / 60

        if (actualDuration > 0 && _timerState.value.mode != TimerMode.SHORT_BREAK && _timerState.value.mode != TimerMode.LONG_BREAK) {
            viewModelScope.launch {
                repository.insertStudySession(
                    StudySession(
                        courseId = _timerState.value.selectedCourseId,
                        assignmentId = _timerState.value.selectedAssignmentId,
                        durationMinutes = actualDuration,
                        focusScore = focusScore,
                        notes = notes.ifBlank { _timerState.value.sessionNotes }
                    )
                )
            }
        }

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = _timerState.value.totalSeconds
        )
    }

    fun deleteStudySession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteStudySession(session)
        }
    }

    // Calendar Export
    fun exportIcsFile(): File? {
        val allAssignmentsList = assignments.value
        val allCoursesMap = courses.value.associateBy { it.id }
        val allBlocks = scheduleBlocks.value
        return CalendarIntegrationHelper.generateIcsFile(context, allAssignmentsList, allCoursesMap, allBlocks)
    }

    // Calculate GPA & Stats
    fun calculateGPA(courseList: List<Course>): Float {
        if (courseList.isEmpty()) return 4.0f
        var totalPoints = 0.0f
        var totalCredits = 0.0f
        for (c in courseList) {
            val gradePoints = when (c.targetGrade.uppercase().trim()) {
                "A+", "A" -> 4.0f
                "A-" -> 3.7f
                "B+" -> 3.3f
                "B" -> 3.0f
                "B-" -> 2.7f
                "C+" -> 2.3f
                "C" -> 2.0f
                "C-" -> 1.7f
                "D+" -> 1.3f
                "D" -> 1.0f
                else -> 0.0f
            }
            totalPoints += gradePoints * c.credits
            totalCredits += c.credits
        }
        return if (totalCredits > 0) totalPoints / totalCredits else 4.0f
    }
}
