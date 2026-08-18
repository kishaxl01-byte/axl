package com.example.data.repository

import com.example.data.local.dao.AssignmentDao
import com.example.data.local.dao.CourseDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.Course
import com.example.data.local.entity.ScheduleBlock
import com.example.data.local.entity.StudySession
import kotlinx.coroutines.flow.Flow

class StudyFlowRepository(
    private val courseDao: CourseDao,
    private val assignmentDao: AssignmentDao,
    private val scheduleDao: ScheduleDao,
    private val studySessionDao: StudySessionDao
) {
    // Courses
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    suspend fun getCourseById(id: Long): Course? = courseDao.getCourseById(id)
    fun getCourseByIdFlow(id: Long): Flow<Course?> = courseDao.getCourseByIdFlow(id)
    suspend fun insertCourse(course: Course): Long = courseDao.insertCourse(course)
    suspend fun insertCourses(courses: List<Course>): List<Long> = courseDao.insertAll(courses)
    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course)
    suspend fun deleteCourseById(id: Long) = courseDao.deleteCourseById(id)

    // Assignments
    val allAssignments: Flow<List<Assignment>> = assignmentDao.getAllAssignments()
    val pendingAssignments: Flow<List<Assignment>> = assignmentDao.getPendingAssignments()
    fun getAssignmentsForCourse(courseId: Long): Flow<List<Assignment>> = assignmentDao.getAssignmentsForCourse(courseId)
    suspend fun getAssignmentById(id: Long): Assignment? = assignmentDao.getAssignmentById(id)
    fun getAssignmentByIdFlow(id: Long): Flow<Assignment?> = assignmentDao.getAssignmentByIdFlow(id)
    suspend fun insertAssignment(assignment: Assignment): Long = assignmentDao.insertAssignment(assignment)
    suspend fun insertAssignments(assignments: List<Assignment>): List<Long> = assignmentDao.insertAll(assignments)
    suspend fun updateAssignment(assignment: Assignment) = assignmentDao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: Assignment) = assignmentDao.deleteAssignment(assignment)
    suspend fun deleteAssignmentById(id: Long) = assignmentDao.deleteAssignmentById(id)
    suspend fun updateAssignmentStatus(id: Long, status: AssignmentStatus, completionTime: Long? = null) =
        assignmentDao.updateStatus(id, status, completionTime)
    suspend fun updateCalendarExportStatus(id: Long, isExported: Boolean) =
        assignmentDao.updateCalendarExportStatus(id, isExported)

    // Schedule Blocks
    val allScheduleBlocks: Flow<List<ScheduleBlock>> = scheduleDao.getAllScheduleBlocks()
    fun getScheduleForCourse(courseId: Long): Flow<List<ScheduleBlock>> = scheduleDao.getScheduleForCourse(courseId)
    suspend fun insertScheduleBlock(block: ScheduleBlock): Long = scheduleDao.insertScheduleBlock(block)
    suspend fun insertScheduleBlocks(blocks: List<ScheduleBlock>): List<Long> = scheduleDao.insertAll(blocks)
    suspend fun updateScheduleBlock(block: ScheduleBlock) = scheduleDao.updateScheduleBlock(block)
    suspend fun deleteScheduleBlock(block: ScheduleBlock) = scheduleDao.deleteScheduleBlock(block)
    suspend fun deleteScheduleBlockById(id: Long) = scheduleDao.deleteScheduleBlockById(id)

    // Study Sessions
    val allStudySessions: Flow<List<StudySession>> = studySessionDao.getAllStudySessions()
    fun getStudySessionsForCourse(courseId: Long): Flow<List<StudySession>> = studySessionDao.getStudySessionsForCourse(courseId)
    fun getTotalFocusMinutesSince(sinceMillis: Long): Flow<Int?> = studySessionDao.getTotalFocusMinutesSince(sinceMillis)
    suspend fun insertStudySession(session: StudySession): Long = studySessionDao.insertSession(session)
    suspend fun deleteStudySession(session: StudySession) = studySessionDao.deleteSession(session)
}
