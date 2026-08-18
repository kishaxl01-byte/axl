package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.NotificationHelper

class DeadlineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val assignmentId = intent.getLongExtra(NotificationHelper.EXTRA_ASSIGNMENT_ID, -1L)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_ASSIGNMENT_TITLE) ?: "Upcoming Task"
        val courseName = intent.getStringExtra(NotificationHelper.EXTRA_COURSE_NAME) ?: "Course"
        val type = intent.getStringExtra(NotificationHelper.EXTRA_TYPE) ?: "Assignment"

        if (assignmentId != -1L) {
            NotificationHelper.showDeadlineNotification(
                context = context,
                assignmentId = assignmentId,
                title = title,
                courseName = courseName,
                type = type
            )
        }
    }
}
