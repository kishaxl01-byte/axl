package com.example.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.Assignment
import com.example.receiver.DeadlineAlarmReceiver

object NotificationHelper {
    const val CHANNEL_DEADLINES_ID = "studyflow_deadlines"
    const val CHANNEL_DEADLINES_NAME = "Assignment Deadlines & Exams"

    const val CHANNEL_SCHEDULE_ID = "studyflow_schedule"
    const val CHANNEL_SCHEDULE_NAME = "Class & Activity Reminders"

    const val CHANNEL_FOCUS_ID = "studyflow_focus"
    const val CHANNEL_FOCUS_NAME = "Study Session Alerts"

    const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
    const val EXTRA_ASSIGNMENT_TITLE = "extra_assignment_title"
    const val EXTRA_COURSE_NAME = "extra_course_name"
    const val EXTRA_DUE_DATE = "extra_due_date"
    const val EXTRA_TYPE = "extra_type"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINES_ID,
                CHANNEL_DEADLINES_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies before assignment deadlines, exams, and project submissions"
                enableVibration(true)
            }

            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE_ID,
                CHANNEL_SCHEDULE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds about upcoming classes, labs, and scheduled activities"
                enableVibration(true)
            }

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS_ID,
                CHANNEL_FOCUS_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Focus timer status and study session notifications"
            }

            notificationManager.createNotificationChannel(deadlineChannel)
            notificationManager.createNotificationChannel(scheduleChannel)
            notificationManager.createNotificationChannel(focusChannel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAssignmentReminder(
        context: Context,
        assignment: Assignment,
        courseName: String?
    ) {
        if (!assignment.reminderEnabled) return

        val triggerTime = assignment.dueDateMillis - (assignment.reminderAdvanceMinutes * 60 * 1000L)
        if (triggerTime <= System.currentTimeMillis()) return // Already in the past

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DeadlineAlarmReceiver::class.java).apply {
            putExtra(EXTRA_ASSIGNMENT_ID, assignment.id)
            putExtra(EXTRA_ASSIGNMENT_TITLE, assignment.title)
            putExtra(EXTRA_COURSE_NAME, courseName ?: "Course")
            putExtra(EXTRA_DUE_DATE, assignment.dueDateMillis)
            putExtra(EXTRA_TYPE, assignment.type.displayName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignment.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Exact alarm permission not granted; fallback to inexact
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAssignmentReminder(context: Context, assignmentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DeadlineAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    fun showDeadlineNotification(
        context: Context,
        assignmentId: Long,
        title: String,
        courseName: String,
        type: String
    ) {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            assignmentId.toInt(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $type Due Soon: $title")
            .setContentText("[$courseName] Don't forget your deadline! Tap to review or complete.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(assignmentId.toInt(), notification)
        } catch (_: Exception) {
            // Gracefully ignore if permissions disabled
        }
    }
}
