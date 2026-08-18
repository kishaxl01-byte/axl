package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AssignmentType
import com.example.data.local.entity.Course
import com.example.data.local.entity.Priority
import com.example.data.local.entity.Subtask
import com.example.ui.theme.CleanAmber
import com.example.ui.theme.CleanAmberContainer
import com.example.ui.theme.CleanCoral
import com.example.ui.theme.CleanCoralContainer
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.CleanCyanContainer
import com.example.ui.theme.CleanEmerald
import com.example.ui.theme.CleanEmeraldContainer
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val (bg, textColor, label) = when (priority) {
        Priority.URGENT -> Triple(CleanCoralContainer, CleanCoral, "Urgent")
        Priority.HIGH -> Triple(CleanAmberContainer, CleanAmber, "High")
        Priority.MEDIUM -> Triple(CleanPrimaryContainer, CleanPrimary, "Medium")
        Priority.LOW -> Triple(CleanEmeraldContainer, CleanEmerald, "Low")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TypeBadge(type: AssignmentType, modifier: Modifier = Modifier) {
    val icon: ImageVector = when (type) {
        AssignmentType.HOMEWORK -> Icons.Default.MenuBook
        AssignmentType.PROJECT -> Icons.Default.FolderSpecial
        AssignmentType.EXAM -> Icons.Default.Quiz
        AssignmentType.ESSAY -> Icons.Default.EditNote
        AssignmentType.READING -> Icons.Default.AutoStories
        AssignmentType.QUIZ -> Icons.Default.Timer
        AssignmentType.LAB -> Icons.Default.Biotech
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = type.displayName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CountdownBadge(dueDateMillis: Long, isCompleted: Boolean, modifier: Modifier = Modifier) {
    if (isCompleted) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CleanEmeraldContainer)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "✓ Done",
                color = CleanEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        return
    }

    val now = System.currentTimeMillis()
    val diff = dueDateMillis - now
    val diffHours = diff / (1000 * 60 * 60)
    val diffDays = diff / (1000 * 60 * 60 * 24)

    val (bg, textColor, text) = when {
        diff < 0 -> {
            val overdueDays = (-diff) / (1000 * 60 * 60 * 24)
            val overdueText = if (overdueDays > 0) "Overdue ($overdueDays d)" else "Overdue"
            Triple(CleanCoralContainer, CleanCoral, overdueText)
        }
        diffHours < 3 -> Triple(CleanCoralContainer, CleanCoral, "Due in ${diffHours}h")
        diffHours < 24 -> {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            Triple(CleanPrimaryContainer, CleanPrimary, "Due Today ${timeFormat.format(Date(dueDateMillis))}")
        }
        diffDays == 1L -> Triple(CleanCyanContainer, CleanCyan, "Tomorrow")
        diffDays < 7 -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "In $diffDays days")
        else -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, dateFormat.format(Date(dueDateMillis)))
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = textColor
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CourseTag(course: Course?, modifier: Modifier = Modifier) {
    if (course == null) return

    val color = try {
        Color(android.graphics.Color.parseColor(course.colorHex))
    } catch (_: Exception) {
        CleanPrimary
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = course.code,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SubtaskProgressBar(
    subtasks: List<Subtask>,
    modifier: Modifier = Modifier
) {
    if (subtasks.isEmpty()) return
    val doneCount = subtasks.count { it.isCompleted }
    val totalCount = subtasks.size
    val progress = doneCount.toFloat() / totalCount.toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Checklist ($doneCount/$totalCount)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (doneCount == totalCount) CleanEmerald else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (doneCount == totalCount) CleanEmerald else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    icon: ImageVector,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
