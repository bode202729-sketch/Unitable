package com.example.data.model

enum class ClassType(
    val englishName: String,
    val arabicName: String,
    val defaultColorHex: String
) {
    LECTURE("Lecture", "محاضرة", "#3B82F6"),     // Blue
    LAB("Lab", "معمل / تدريب", "#10B981"),        // Emerald
    SECTION("Section", "سكشن / حلقة", "#F59E0B"),  // Amber
    SEMINAR("Seminar", "ندوة", "#8B5CF6"),        // Purple
    EXAM("Exam", "اختبار", "#EF4444"),            // Red
    OTHER("Other", "أخرى", "#64748B");            // Slate

    fun displayName(isArabic: Boolean): String = if (isArabic) arabicName else englishName

    companion object {
        fun fromString(str: String?): ClassType {
            if (str == null) return LECTURE
            val lower = str.trim().lowercase()
            return when {
                lower.contains("lab") || lower.contains("معمل") || lower.contains("ورشة") || lower.contains("تدريب") -> LAB
                lower.contains("sec") || lower.contains("سكشن") || lower.contains("حلقة") || lower.contains("تمرين") -> SECTION
                lower.contains("exam") || lower.contains("امتحان") || lower.contains("اختبار") -> EXAM
                lower.contains("sem") || lower.contains("ندوة") -> SEMINAR
                lower.contains("lec") || lower.contains("محاضرة") || lower.contains("نظر") -> LECTURE
                else -> LECTURE
            }
        }
    }
}
