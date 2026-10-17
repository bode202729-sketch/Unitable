package com.example.data.model

enum class DayOfWeekEnum(
    val englishName: String,
    val arabicName: String,
    val shortEnglish: String,
    val shortArabic: String,
    val order: Int
) {
    SATURDAY("Saturday", "السبت", "Sat", "سبت", 1),
    SUNDAY("Sunday", "الأحد", "Sun", "أحد", 2),
    MONDAY("Monday", "الإثنين", "Mon", "إثنين", 3),
    TUESDAY("Tuesday", "الثلاثاء", "Tue", "ثلاثاء", 4),
    WEDNESDAY("Wednesday", "الأربعاء", "Wed", "أربعاء", 5),
    THURSDAY("Thursday", "الخميس", "Thu", "خميس", 6),
    FRIDAY("Friday", "الجمعة", "Fri", "جمعة", 7);

    fun displayName(isArabic: Boolean): String = if (isArabic) arabicName else englishName
    fun shortName(isArabic: Boolean): String = if (isArabic) shortArabic else shortEnglish

    companion object {
        fun fromString(str: String?): DayOfWeekEnum {
            if (str == null) return SUNDAY
            val normalized = str.trim().uppercase()
            // Try exact enum match
            entries.find { it.name == normalized }?.let { return it }

            val lower = str.trim().lowercase()
            return when {
                lower.contains("sat") || lower.contains("سبت") -> SATURDAY
                lower.contains("sun") || lower.contains("أحد") || lower.contains("احد") -> SUNDAY
                lower.contains("mon") || lower.contains("إثنين") || lower.contains("اثنين") || lower.contains("تنين") -> MONDAY
                lower.contains("tue") || lower.contains("ثلاثاء") || lower.contains("تلات") -> TUESDAY
                lower.contains("wed") || lower.contains("أربعاء") || lower.contains("اربعاء") -> WEDNESDAY
                lower.contains("thu") || lower.contains("خميس") -> THURSDAY
                lower.contains("fri") || lower.contains("جمعة") || lower.contains("جمعه") -> FRIDAY
                else -> SUNDAY
            }
        }
    }
}
