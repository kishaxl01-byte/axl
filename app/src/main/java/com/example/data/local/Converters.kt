package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.ActivityType
import com.example.data.local.entity.AssignmentStatus
import com.example.data.local.entity.AssignmentType
import com.example.data.local.entity.Priority
import com.example.data.local.entity.Subtask
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromAssignmentType(value: AssignmentType?): String = value?.name ?: AssignmentType.HOMEWORK.name

    @TypeConverter
    fun toAssignmentType(value: String?): AssignmentType = try {
        AssignmentType.valueOf(value ?: AssignmentType.HOMEWORK.name)
    } catch (_: Exception) {
        AssignmentType.HOMEWORK
    }

    @TypeConverter
    fun fromPriority(value: Priority?): String = value?.name ?: Priority.MEDIUM.name

    @TypeConverter
    fun toPriority(value: String?): Priority = try {
        Priority.valueOf(value ?: Priority.MEDIUM.name)
    } catch (_: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromAssignmentStatus(value: AssignmentStatus?): String = value?.name ?: AssignmentStatus.TODO.name

    @TypeConverter
    fun toAssignmentStatus(value: String?): AssignmentStatus = try {
        AssignmentStatus.valueOf(value ?: AssignmentStatus.TODO.name)
    } catch (_: Exception) {
        AssignmentStatus.TODO
    }

    @TypeConverter
    fun fromActivityType(value: ActivityType?): String = value?.name ?: ActivityType.LECTURE.name

    @TypeConverter
    fun toActivityType(value: String?): ActivityType = try {
        ActivityType.valueOf(value ?: ActivityType.LECTURE.name)
    } catch (_: Exception) {
        ActivityType.LECTURE
    }

    companion object {
        fun subtasksToJson(subtasks: List<Subtask>): String {
            val jsonArray = JSONArray()
            subtasks.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("text", it.text)
                obj.put("isCompleted", it.isCompleted)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun jsonToSubtasks(json: String?): List<Subtask> {
            if (json.isNullOrBlank()) return emptyList()
            val list = mutableListOf<Subtask>()
            try {
                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        Subtask(
                            id = obj.optString("id", i.toString()),
                            text = obj.optString("text", ""),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            } catch (_: Exception) {
                // Return empty or fallback
            }
            return list
        }
    }
}
