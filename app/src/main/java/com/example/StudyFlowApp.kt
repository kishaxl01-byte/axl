package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.Converters
import com.example.data.local.entity.ActivityType
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.AssignmentType
import com.example.data.local.entity.Course
import com.example.data.local.entity.Priority
import com.example.data.local.entity.ScheduleBlock
import com.example.data.local.entity.StudySession
import com.example.data.local.entity.Subtask
import com.example.data.repository.StudyFlowRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class StudyFlowApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: StudyFlowRepository by lazy {
        StudyFlowRepository(
            database.courseDao(),
            database.assignmentDao(),
            database.scheduleDao(),
            database.studySessionDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        seedSampleDataIfEmpty()
    }

    private fun seedSampleDataIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val existingCourses = repository.allCourses.first()
            if (existingCourses.isEmpty()) {
                // Seed Courses
                val cs = Course(
                    code = "CS 201",
                    name = "Data Structures & Algorithms",
                    instructor = "Prof. Martinez",
                    colorHex = "#4F46E5", // Indigo
                    roomLocation = "Turing Hall 302",
                    credits = 4.0f,
                    targetGrade = "A",
                    term = "Fall 2026",
                    notes = "Office Hours: Tue/Thu 3-5 PM. Midterms in Oct/Nov."
                )
                val math = Course(
                    code = "MATH 240",
                    name = "Calculus III & Linear Algebra",
                    instructor = "Dr. Katherine Lee",
                    colorHex = "#0EA5E9", // Cyan
                    roomLocation = "Science Ctr 110",
                    credits = 4.0f,
                    targetGrade = "A-",
                    term = "Fall 2026",
                    notes = "Homework due every Friday 11:59 PM on Canvas."
                )
                val bio = Course(
                    code = "BIO 150",
                    name = "Cellular Biology & Genetics",
                    instructor = "Dr. Robert Vance",
                    colorHex = "#10B981", // Emerald
                    roomLocation = "BioLab B4",
                    credits = 3.0f,
                    targetGrade = "A",
                    term = "Fall 2026",
                    notes = "Lab coat and safety goggles required."
                )
                val lit = Course(
                    code = "ENG 115",
                    name = "Modern Academic Writing & Rhetoric",
                    instructor = "Prof. Sarah Jenkins",
                    colorHex = "#F59E0B", // Amber
                    roomLocation = "Humanities 208",
                    credits = 3.0f,
                    targetGrade = "A",
                    term = "Fall 2026",
                    notes = "Draft peer reviews on Thursdays."
                )
                val econ = Course(
                    code = "ECON 102",
                    name = "Principles of Microeconomics",
                    instructor = "Dr. Alan Grant",
                    colorHex = "#8B5CF6", // Violet
                    roomLocation = "Auditorium A",
                    credits = 3.0f,
                    targetGrade = "B+",
                    term = "Fall 2026",
                    notes = "Problem sets on web portal."
                )

                val csId = repository.insertCourse(cs)
                val mathId = repository.insertCourse(math)
                val bioId = repository.insertCourse(bio)
                val litId = repository.insertCourse(lit)
                val econId = repository.insertCourse(econ)

                // Schedule Helper times
                val now = Calendar.getInstance()

                // Seed Schedule
                // CS 201: Mon/Wed/Fri 10:00 - 11:15 AM
                repository.insertScheduleBlock(
                    ScheduleBlock(
                        title = "CS 201 Lecture",
                        courseId = csId,
                        activityType = ActivityType.LECTURE,
                        daysOfWeek = "2,4,6", // Mon, Wed, Fri (Java Calendar: 2=Mon, 4=Wed, 6=Fri)
                        startHour = 10,
                        startMinute = 0,
                        endHour = 11,
                        endMinute = 15,
                        location = "Turing Hall 302",
                        instructor = "Prof. Martinez",
                        colorHex = "#4F46E5"
                    )
                )
                // MATH 240: Tue/Thu 9:00 - 10:30 AM
                repository.insertScheduleBlock(
                    ScheduleBlock(
                        title = "MATH 240 Lecture & Quiz",
                        courseId = mathId,
                        activityType = ActivityType.LECTURE,
                        daysOfWeek = "3,5", // Tue, Thu
                        startHour = 9,
                        startMinute = 0,
                        endHour = 10,
                        endMinute = 30,
                        location = "Science Ctr 110",
                        instructor = "Dr. Katherine Lee",
                        colorHex = "#0EA5E9"
                    )
                )
                // BIO 150 Lab: Wednesday 2:00 - 4:30 PM
                repository.insertScheduleBlock(
                    ScheduleBlock(
                        title = "Bio Genetics Lab",
                        courseId = bioId,
                        activityType = ActivityType.LAB,
                        daysOfWeek = "4", // Wed
                        startHour = 14,
                        startMinute = 0,
                        endHour = 16,
                        endMinute = 30,
                        location = "BioLab B4",
                        instructor = "Dr. Vance",
                        colorHex = "#10B981"
                    )
                )
                // ENG 115: Mon/Wed 1:00 - 2:15 PM
                repository.insertScheduleBlock(
                    ScheduleBlock(
                        title = "ENG 115 Seminar",
                        courseId = litId,
                        activityType = ActivityType.DISCUSSION,
                        daysOfWeek = "2,4", // Mon, Wed
                        startHour = 13,
                        startMinute = 0,
                        endHour = 14,
                        endMinute = 15,
                        location = "Humanities 208",
                        instructor = "Prof. Jenkins",
                        colorHex = "#F59E0B"
                    )
                )
                // Study Session Block: Daily 4:30 - 6:30 PM
                repository.insertScheduleBlock(
                    ScheduleBlock(
                        title = "Library Deep Study Block",
                        courseId = null,
                        activityType = ActivityType.STUDY_SESSION,
                        daysOfWeek = "2,3,4,5,6", // Mon-Fri
                        startHour = 16,
                        startMinute = 30,
                        endHour = 18,
                        endMinute = 30,
                        location = "Main Campus Library 3F",
                        instructor = "Self-Paced",
                        colorHex = "#6366F1"
                    )
                )

                // Seed Assignments
                val cal = Calendar.getInstance()
                // Due tomorrow 11:59 PM
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 0)
                val dueTomorrow = cal.timeInMillis

                // Due in 3 days
                cal.add(Calendar.DAY_OF_YEAR, 2)
                cal.set(Calendar.HOUR_OF_DAY, 17)
                cal.set(Calendar.MINUTE, 0)
                val dueIn3Days = cal.timeInMillis

                // Due next week
                cal.add(Calendar.DAY_OF_YEAR, 5)
                val dueNextWeek = cal.timeInMillis

                // Due Today evening
                val dueTodayCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 22)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val dueToday = dueTodayCal.timeInMillis

                val assign1 = Assignment(
                    title = "Binary Search Tree & Graph Traversal Project",
                    courseId = csId,
                    type = AssignmentType.PROJECT,
                    priority = Priority.URGENT,
                    status = AssignmentStatus.IN_PROGRESS,
                    dueDateMillis = dueTomorrow,
                    estimatedMinutes = 180,
                    subtasksJson = Converters.subtasksToJson(
                        listOf(
                            Subtask("1", "Implement Node and BST class", true),
                            Subtask("2", "Write BFS & DFS traversal algorithms", true),
                            Subtask("3", "Benchmark search performance with 10k nodes", false),
                            Subtask("4", "Format documentation & write README.md", false)
                        )
                    ),
                    notes = "Submit GitHub repository link & zip archive on Canvas.",
                    reminderEnabled = true,
                    reminderAdvanceMinutes = 1440
                )

                val assign2 = Assignment(
                    title = "Vector Calculus Problem Set #4",
                    courseId = mathId,
                    type = AssignmentType.HOMEWORK,
                    priority = Priority.HIGH,
                    status = AssignmentStatus.TODO,
                    dueDateMillis = dueIn3Days,
                    estimatedMinutes = 90,
                    subtasksJson = Converters.subtasksToJson(
                        listOf(
                            Subtask("1", "Problems 1-5 (Partial Derivatives)", false),
                            Subtask("2", "Problems 6-10 (Gradient & Tangent Planes)", false),
                            Subtask("3", "Scan solutions as single PDF", false)
                        )
                    ),
                    notes = "Show all detailed step-by-step arithmetic.",
                    reminderEnabled = true,
                    reminderAdvanceMinutes = 720
                )

                val assign3 = Assignment(
                    title = "Enzyme Kinetics Lab Report",
                    courseId = bioId,
                    type = AssignmentType.LAB,
                    priority = Priority.MEDIUM,
                    status = AssignmentStatus.TODO,
                    dueDateMillis = dueToday,
                    estimatedMinutes = 120,
                    subtasksJson = Converters.subtasksToJson(
                        listOf(
                            Subtask("1", "Plot spectrophotometer calibration curves", true),
                            Subtask("2", "Calculate Michaelis-Menten constant (Km)", false),
                            Subtask("3", "Write Discussion & Conclusion", false)
                        )
                    ),
                    notes = "Include error bar charts from Excel.",
                    reminderEnabled = true,
                    reminderAdvanceMinutes = 120
                )

                val assign4 = Assignment(
                    title = "Comparative Rhetoric Essay (1500 words)",
                    courseId = litId,
                    type = AssignmentType.ESSAY,
                    priority = Priority.HIGH,
                    status = AssignmentStatus.IN_PROGRESS,
                    dueDateMillis = dueNextWeek,
                    estimatedMinutes = 240,
                    subtasksJson = Converters.subtasksToJson(
                        listOf(
                            Subtask("1", "Outline thesis & 3 primary supporting arguments", true),
                            Subtask("2", "Draft introduction & literature review", true),
                            Subtask("3", "Draft body paragraphs & MLA citations", false),
                            Subtask("4", "Grammar check & finalize bibliography", false)
                        )
                    ),
                    notes = "MLA 9th edition format. Minimum 4 academic peer-reviewed sources.",
                    reminderEnabled = true,
                    reminderAdvanceMinutes = 2880
                )

                val assign5 = Assignment(
                    title = "Microeconomics Midterm Exam",
                    courseId = econId,
                    type = AssignmentType.EXAM,
                    priority = Priority.URGENT,
                    status = AssignmentStatus.TODO,
                    dueDateMillis = dueNextWeek,
                    estimatedMinutes = 150,
                    subtasksJson = Converters.subtasksToJson(
                        listOf(
                            Subtask("1", "Review Chapters 1-6 lecture slides", false),
                            Subtask("2", "Practice elasticity & supply-demand curves", false),
                            Subtask("3", "Solve 2025 sample midterm exam", false)
                        )
                    ),
                    notes = "Exam starts promptly at 8:30 AM in Auditorium A. Bring #2 pencils and calculator.",
                    reminderEnabled = true,
                    reminderAdvanceMinutes = 1440
                )

                val a1Id = repository.insertAssignment(assign1)
                val a2Id = repository.insertAssignment(assign2)
                val a3Id = repository.insertAssignment(assign3)
                val a4Id = repository.insertAssignment(assign4)
                val a5Id = repository.insertAssignment(assign5)

                // Schedule notifications for initial items
                NotificationHelper.scheduleAssignmentReminder(this@StudyFlowApp, assign1.copy(id = a1Id), "CS 201")
                NotificationHelper.scheduleAssignmentReminder(this@StudyFlowApp, assign2.copy(id = a2Id), "MATH 240")
                NotificationHelper.scheduleAssignmentReminder(this@StudyFlowApp, assign3.copy(id = a3Id), "BIO 150")

                // Seed some study sessions for streak and focus stats
                repository.insertStudySession(
                    StudySession(
                        courseId = csId,
                        assignmentId = a1Id,
                        durationMinutes = 50,
                        timestampMillis = System.currentTimeMillis() - 86400000L,
                        focusScore = 5,
                        notes = "Completed binary tree nodes and test cases"
                    )
                )
                repository.insertStudySession(
                    StudySession(
                        courseId = litId,
                        assignmentId = a4Id,
                        durationMinutes = 45,
                        timestampMillis = System.currentTimeMillis() - 43200000L,
                        focusScore = 4,
                        notes = "Wrote essay outline and thesis statement"
                    )
                )
                repository.insertStudySession(
                    StudySession(
                        courseId = mathId,
                        assignmentId = a2Id,
                        durationMinutes = 30,
                        timestampMillis = System.currentTimeMillis() - 7200000L,
                        focusScore = 5,
                        notes = "Reviewed lecture notes on tangent planes"
                    )
                )
            }
        }
    }
}
