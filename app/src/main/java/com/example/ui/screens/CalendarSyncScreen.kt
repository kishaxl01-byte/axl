package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calendar.CalendarIntegrationHelper
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.viewmodel.StudyFlowViewModel

data class PlatformSyncInfo(
    val name: String,
    val instructions: String
)

@Composable
fun CalendarSyncScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val scheduleBlocks by viewModel.scheduleBlocks.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()

    val pendingDeadlinesCount = remember(assignments) { assignments.count { it.status != com.example.data.local.entity.AssignmentStatus.COMPLETED } }
    val remindersActiveCount = remember(assignments) { assignments.count { it.reminderEnabled && it.status != com.example.data.local.entity.AssignmentStatus.COMPLETED } }

    val platforms = remember {
        listOf(
            PlatformSyncInfo("Google Calendar", "Tap 'Export All', select Google Calendar or save the .ics file and import via calendar.google.com → Settings → Import & Export."),
            PlatformSyncInfo("Apple Calendar / iCloud", "Open the exported .ics file on iOS or Mac to subscribe and merge events into your default iCloud study calendar."),
            PlatformSyncInfo("Canvas & Blackboard LMS", "Assignments with Course codes are tagged so you can match LMS coursework with your customized daily study schedule."),
            PlatformSyncInfo("Microsoft Outlook / Teams", "Open the exported .ics attachment in Outlook to add all lectures and homework deadlines to your school Microsoft account.")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CALENDAR INTEGRATION",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sync & Export",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Primary Sync Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Universal iCalendar (.ics)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Compatible with Google, Apple, Canvas & Outlook",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Export your entire academic schedule and all upcoming assignment deadlines into standard .ics format. Tap below to share directly into your favorite calendar app or email it to yourself.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Button(
                        onClick = {
                            val file = viewModel.exportIcsFile()
                            if (file != null) {
                                val shareIntent = CalendarIntegrationHelper.createShareIcsIntent(context, file)
                                try {
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open calendar app", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "No schedule data to export", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_ics_button")
                    ) {
                        Icon(imageVector = Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All to Calendar (.ics)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Notifications & Alarms Status
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Deadline Notifications",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CleanEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$remindersActiveCount Active Alarms",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleanEmerald
                            )
                        }
                    }

                    Text(
                        text = "Exact system alarms are scheduled for all pending assignments with notifications enabled. You will receive alerts at your chosen advance times (15m, 1h, 24h before due date).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Integration Instructions by Platform
        item {
            Text(
                text = "HOW TO IMPORT",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(platforms, key = { it.name }) { platform ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = platform.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = platform.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
