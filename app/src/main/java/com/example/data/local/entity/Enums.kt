package com.example.data.local.entity

enum class AssignmentType(val displayName: String, val iconName: String) {
    HOMEWORK("Homework", "menu_book"),
    PROJECT("Project", "folder_special"),
    EXAM("Exam / Test", "quiz"),
    ESSAY("Essay / Paper", "edit_note"),
    READING("Reading", "auto_stories"),
    QUIZ("Quiz", "timer"),
    LAB("Lab Report", "biotech")
}

enum class Priority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

enum class AssignmentStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

enum class ActivityType(val displayName: String) {
    LECTURE("Lecture / Class"),
    LAB("Laboratory"),
    DISCUSSION("Discussion / Seminar"),
    STUDY_SESSION("Study Session"),
    EXTRACURRICULAR("Extracurricular / Club"),
    WORK("Work / Tutoring"),
    EXAM("Exam"),
    OTHER("Activity")
}

data class Subtask(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false
)
