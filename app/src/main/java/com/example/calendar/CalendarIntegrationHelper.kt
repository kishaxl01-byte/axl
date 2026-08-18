package com.example.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.FileProvider
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.Course
import com.example.data.local.entity.ScheduleBlock
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CalendarIntegrationHelper {

    /**
     * Opens the device Calendar app (Google Calendar, Samsung Calendar, etc.) with pre-filled event details
     */
    fun openAddEventToCalendarIntent(
        context: Context,
        assignment: Assignment,
        course: Course?
    ): Intent {
        val coursePrefix = course?.code?.let { "[$it] " } ?: ""
        val title = "$coursePrefix${assignment.title} (${assignment.type.displayName})"
        val description = buildString {
            append("Type: ").append(assignment.type.displayName).append("\n")
            if (course != null) append("Course: ").append(course.name).append(" (").append(course.code).append(")\n")
            append("Priority: ").append(assignment.priority.displayName).append("\n")
            append("Est. Duration: ").append(assignment.estimatedMinutes).append(" mins\n")
            if (assignment.notes.isNotBlank()) append("\nNotes:\n").append(assignment.notes)
        }

        val startTime = assignment.dueDateMillis - (assignment.estimatedMinutes * 60 * 1000L)
        val endTime = assignment.dueDateMillis

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            if (course?.roomLocation?.isNotBlank() == true) {
                putExtra(CalendarContract.Events.EVENT_LOCATION, course.roomLocation)
            }
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            putExtra(CalendarContract.Events.ALL_DAY, false)
        }
    }

    /**
     * Opens intent to add a recurring or one-time class schedule block to Calendar
     */
    fun openAddScheduleToCalendarIntent(
        context: Context,
        block: ScheduleBlock,
        course: Course?
    ): Intent {
        val coursePrefix = course?.code?.let { "[$it] " } ?: ""
        val title = "$coursePrefix${block.title}"

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, block.startHour)
            set(Calendar.MINUTE, block.startMinute)
            set(Calendar.SECOND, 0)
        }
        val startTime = cal.timeInMillis

        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, block.endHour)
            set(Calendar.MINUTE, block.endMinute)
            set(Calendar.SECOND, 0)
        }
        val endTime = endCal.timeInMillis

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, "Instructor: ${block.instructor}\nNotes: ${block.notes}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, block.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
        }
    }

    /**
     * Generates a standard .ics (iCalendar) text file for Google Calendar, Apple Calendar, Canvas, Outlook import
     */
    fun generateIcsFile(
        context: Context,
        assignments: List<Assignment>,
        courses: Map<Long, Course>,
        scheduleBlocks: List<ScheduleBlock>
    ): File? {
        val utcFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//StudyFlow Student Planner//EN\r\n")
        sb.append("CALSCALE:GREGORIAN\r\n")
        sb.append("METHOD:PUBLISH\r\n")
        sb.append("X-WR-CALNAME:StudyFlow Student Schedule & Deadlines\r\n")

        val nowStr = utcFormat.format(Date())

        // Export Assignments
        for (assignment in assignments) {
            val course = assignment.courseId?.let { courses[it] }
            val courseCode = course?.code ?: "StudyFlow"
            val title = "[$courseCode] ${escapeIcs(assignment.title)}"
            val startTime = assignment.dueDateMillis - (assignment.estimatedMinutes * 60 * 1000L)
            val endTime = assignment.dueDateMillis

            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:studyflow-assign-${assignment.id}-${assignment.createdAtMillis}@studyflow.app\r\n")
            sb.append("DTSTAMP:$nowStr\r\n")
            sb.append("DTSTART:${utcFormat.format(Date(startTime))}\r\n")
            sb.append("DTEND:${utcFormat.format(Date(endTime))}\r\n")
            sb.append("SUMMARY:$title\r\n")
            sb.append("DESCRIPTION:Priority: ${assignment.priority.displayName}\\nType: ${assignment.type.displayName}\\nNotes: ${escapeIcs(assignment.notes)}\r\n")
            if (course?.roomLocation?.isNotBlank() == true) {
                sb.append("LOCATION:${escapeIcs(course.roomLocation)}\r\n")
            }
            sb.append("STATUS:CONFIRMED\r\n")
            sb.append("BEGIN:VALARM\r\n")
            sb.append("TRIGGER:-PT${assignment.reminderAdvanceMinutes}M\r\n")
            sb.append("ACTION:DISPLAY\r\n")
            sb.append("DESCRIPTION:Reminder: $title is due soon!\r\n")
            sb.append("END:VALARM\r\n")
            sb.append("END:VEVENT\r\n")
        }

        sb.append("END:VCALENDAR\r\n")

        return try {
            val exportDir = File(context.cacheDir, "calendar_exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, "StudyFlow_Schedule.ics")
            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            file
        } catch (_: Exception) {
            null
        }
    }

    private fun escapeIcs(text: String): String {
        return text.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    /**
     * Creates an Android Share Sheet Intent for the .ics file
     */
    fun createShareIcsIntent(context: Context, icsFile: File): Intent {
        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                icsFile
            )
        } catch (_: Exception) {
            Uri.fromFile(icsFile)
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "StudyFlow Calendar Export (.ics)")
            putExtra(Intent.EXTRA_TEXT, "Import this .ics file into Google Calendar, Apple Calendar, Canvas, Outlook, or Notion to sync your student schedule and deadlines!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
