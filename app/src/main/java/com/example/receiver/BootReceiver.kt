package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val assignments = db.assignmentDao().getAllAssignments().first()
                    val courses = db.courseDao().getAllCourses().first().associateBy { it.id }

                    for (assignment in assignments) {
                        if (assignment.reminderEnabled && assignment.dueDateMillis > System.currentTimeMillis()) {
                            val course = assignment.courseId?.let { courses[it] }
                            NotificationHelper.scheduleAssignmentReminder(
                                context,
                                assignment,
                                course?.code ?: course?.name
                            )
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
