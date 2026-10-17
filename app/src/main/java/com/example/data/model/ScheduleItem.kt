package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: String, // Key from DayOfWeekEnum.name
    val subject: String,
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "11:00"
    val groupName: String = "", // e.g. "Group 1" or "مجموعة أ"
    val place: String = "",     // e.g. "Hall 101" or "مدرج د"
    val classType: String = ClassType.LECTURE.name,
    val colorHex: String = "#3B82F6",
    val instructor: String = "",
    val notes: String = ""
) {
    val dayEnum: DayOfWeekEnum
        get() = DayOfWeekEnum.fromString(dayOfWeek)

    val typeEnum: ClassType
        get() = ClassType.fromString(classType)
}
