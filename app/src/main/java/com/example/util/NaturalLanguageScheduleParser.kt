package com.example.util

import com.example.data.model.ClassType
import com.example.data.model.DayOfWeekEnum
import com.example.network.GeminiApiClient
import com.example.network.ParsedScheduleResult

object NaturalLanguageScheduleParser {

    suspend fun parse(input: String): ParsedScheduleResult {
        // Try Gemini AI first if configured
        if (GeminiApiClient.hasApiKey()) {
            val aiResult = GeminiApiClient.parseScheduleText(input)
            if (aiResult != null && aiResult.subject.isNotBlank()) {
                return aiResult
            }
        }

        // Fallback to offline rule-based parser
        return parseOffline(input)
    }

    fun parseOffline(input: String): ParsedScheduleResult {
        val normalized = normalizeArabicDigits(input.trim())

        // 1. Detect Day
        val detectedDay = detectDay(normalized)

        // 2. Detect Times (start and end)
        val (startTime, endTime) = detectTimes(normalized)

        // 3. Detect Group
        val groupName = detectGroup(normalized)

        // 4. Detect Place
        val place = detectPlace(normalized)

        // 5. Detect Class Type
        val classType = detectClassType(normalized)

        // 6. Extract Subject
        val subject = extractSubject(normalized, detectedDay, startTime, endTime, groupName, place)

        // Pick color according to class type
        val colorHex = when (classType) {
            ClassType.LAB -> "#10B981"      // Emerald
            ClassType.SECTION -> "#F59E0B"  // Amber
            ClassType.SEMINAR -> "#8B5CF6"  // Purple
            ClassType.EXAM -> "#EF4444"     // Red
            else -> "#3B82F6"               // Blue
        }

        return ParsedScheduleResult(
            dayOfWeek = detectedDay.name,
            subject = if (subject.isNotBlank()) subject else "Course",
            startTime = startTime,
            endTime = endTime,
            groupName = groupName,
            place = place,
            classType = classType.name,
            colorHex = colorHex,
            confidence = "Smart Rule Parser"
        )
    }

    private fun normalizeArabicDigits(input: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var result = input
        for (i in arabicDigits.indices) {
            result = result.replace(arabicDigits[i], ('0' + i))
        }
        return result
    }

    private fun detectDay(text: String): DayOfWeekEnum {
        val lower = text.lowercase()
        return when {
            lower.contains("سبت") || lower.contains("saturday") || lower.contains("sat") -> DayOfWeekEnum.SATURDAY
            lower.contains("أحد") || lower.contains("احد") || lower.contains("sunday") || lower.contains("sun") -> DayOfWeekEnum.SUNDAY
            lower.contains("إثنين") || lower.contains("اثنين") || lower.contains("تنين") || lower.contains("monday") || lower.contains("mon") -> DayOfWeekEnum.MONDAY
            lower.contains("ثلاثاء") || lower.contains("تلات") || lower.contains("tuesday") || lower.contains("tue") -> DayOfWeekEnum.TUESDAY
            lower.contains("أربعاء") || lower.contains("اربعاء") || lower.contains("wednesday") || lower.contains("wed") -> DayOfWeekEnum.WEDNESDAY
            lower.contains("خميس") || lower.contains("thursday") || lower.contains("thu") -> DayOfWeekEnum.THURSDAY
            lower.contains("جمعة") || lower.contains("جمعه") || lower.contains("friday") || lower.contains("fri") -> DayOfWeekEnum.FRIDAY
            else -> DayOfWeekEnum.SUNDAY
        }
    }

    private fun detectTimes(text: String): Pair<String, String> {
        // Regex patterns for times:
        // "from 9 to 11", "9:00 - 11:00", "من 9 ل 11", "من 10:00 الى 12:00", "10am to 12pm"
        val rangePattern = Regex(
            """(?:من|from)?\s*(\d{1,2})(?::(\d{2}))?\s*(?:-|–|to|الى|إلى|ل|حتى)\s*(\d{1,2})(?::(\d{2}))?""",
            RegexOption.IGNORE_CASE
        )
        val match = rangePattern.find(text)

        if (match != null) {
            var startH = match.groupValues[1].toIntOrNull() ?: 9
            val startM = match.groupValues[2].ifEmpty { "00" }
            var endH = match.groupValues[3].toIntOrNull() ?: (startH + 2)
            val endM = match.groupValues[4].ifEmpty { "00" }

            val hasPm = Regex("""(?:\b(?:pm|p\.m\.)\b|(?:\s|^)(?:مساءً|مساء|م)(?:\s|$))""", RegexOption.IGNORE_CASE).containsMatchIn(text)
            val hasAm = Regex("""(?:\b(?:am|a\.m\.)\b|(?:\s|^)(?:صباحاً|صباحا|ص)(?:\s|$))""", RegexOption.IGNORE_CASE).containsMatchIn(text)

            if (hasPm && !hasAm) {
                if (endH in 1..11) endH += 12
                if (startH in 1..11 && startH < endH) startH += 12
            } else if (!hasAm && startH in 1..7) {
                // College classes in 1..7 without explicit AM are usually afternoon (13:00 to 19:00)
                startH += 12
                if (endH in 1..7) endH += 12
            }

            val startStr = "%02d:%s".format(startH.coerceIn(0, 23), startM)
            val endStr = "%02d:%s".format(endH.coerceIn(0, 23), endM)
            return Pair(startStr, endStr)
        }

        // Single time pattern e.g. "الساعة 10" or "at 10"
        val singlePattern = Regex("""(?:الساعة|at|time)\s*(\d{1,2})(?::(\d{2}))?""", RegexOption.IGNORE_CASE)
        val singleMatch = singlePattern.find(text)
        if (singleMatch != null) {
            var h = singleMatch.groupValues[1].toIntOrNull() ?: 9
            val m = singleMatch.groupValues[2].ifEmpty { "00" }
            if (h in 1..7) h += 12
            val startStr = "%02d:%s".format(h.coerceIn(0, 23), m)
            val endStr = "%02d:%s".format((h + 2).coerceIn(0, 23), m)
            return Pair(startStr, endStr)
        }

        return Pair("09:00", "11:00")
    }

    private fun detectGroup(text: String): String {
        val groupRegex = Regex("""(?:مجموعة|مجموعه|سكشن|group|sec|section|grp)\s*([a-zA-Z0-9أ-ي]+)""", RegexOption.IGNORE_CASE)
        val match = groupRegex.find(text)
        if (match != null) {
            val grpVal = match.groupValues[1]
            return if (match.value.contains("مجموع") || match.value.contains("group", true)) {
                "Group $grpVal"
            } else {
                "Sec $grpVal"
            }
        }
        return ""
    }

    private fun detectPlace(text: String): String {
        val placeRegex = Regex("""(?:مدرج|قاعة|قاعه|معمل|مبنى|hall|room|lab|building|auditorium)\s*([a-zA-Z0-9أ-ي\-]+)""", RegexOption.IGNORE_CASE)
        val match = placeRegex.find(text)
        if (match != null) {
            return match.value.trim()
        }
        return ""
    }

    private fun detectClassType(text: String): ClassType {
        val lower = text.lowercase()
        return when {
            lower.contains("معمل") || lower.contains("lab") || lower.contains("ورشة") || lower.contains("تدريب") -> ClassType.LAB
            lower.contains("سكشن") || lower.contains("section") || lower.contains("sec") || lower.contains("حلقة") || lower.contains("تمارين") -> ClassType.SECTION
            lower.contains("امتحان") || lower.contains("اختبار") || lower.contains("quiz") || lower.contains("exam") -> ClassType.EXAM
            lower.contains("ندوة") || lower.contains("seminar") -> ClassType.SEMINAR
            else -> ClassType.LECTURE
        }
    }

    private fun extractSubject(
        text: String,
        day: DayOfWeekEnum,
        start: String,
        end: String,
        group: String,
        place: String
    ): String {
        var clean = text
        // Remove day names
        val dayNames = listOf(
            "saturday", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday",
            "sat", "sun", "mon", "tue", "wed", "thu", "fri",
            "السبت", "الأحد", "الاحد", "الإثنين", "الاثنين", "الثلاثاء", "التلات", "الأربعاء", "الاربعاء", "الخميس", "الجمعة", "الجمعه",
            "يوم"
        )
        for (d in dayNames) {
            clean = clean.replace(Regex("""\b$d\b""", RegexOption.IGNORE_CASE), " ")
            clean = clean.replace(d, " ")
        }

        // Remove time words and patterns
        clean = clean.replace(Regex("""(?:من|from|to|الى|إلى|ل|الساعة|time|at)\s*\d{1,2}(?::\d{2})?\s*(?:ص|م|am|pm)?""", RegexOption.IGNORE_CASE), " ")
        clean = clean.replace(Regex("""\d{1,2}:\d{2}"""), " ")
        clean = clean.replace(Regex("""\b\d{1,2}\s*(?:-|–|to)\s*\d{1,2}\b"""), " ")

        // Remove group words
        if (group.isNotBlank()) {
            clean = clean.replace(group, " ", ignoreCase = true)
            clean = clean.replace(Regex("""(?:مجموعة|مجموعه|سكشن|group|sec|section)\s*[a-zA-Z0-9أ-ي]+""", RegexOption.IGNORE_CASE), " ")
        }

        // Remove place words
        if (place.isNotBlank()) {
            clean = clean.replace(place, " ", ignoreCase = true)
            clean = clean.replace(Regex("""(?:مدرج|قاعة|قاعه|معمل|مبنى|hall|room|lab|building)\s*[a-zA-Z0-9أ-ي\-]+""", RegexOption.IGNORE_CASE), " ")
        }

        // Remove common filler words in Arabic and English
        val fillers = listOf(
            "عندي", "محاضرة", "عايز", "سجل", "ضيف", "مادة", "كورس", "في", "مع", "على", "ب", "من",
            "i have", "have", "add", "class", "lecture", "course", "subject", "in", "at", "with", "for"
        )
        for (f in fillers) {
            clean = clean.replace(Regex("""\b$f\b""", RegexOption.IGNORE_CASE), " ")
        }

        clean = clean.replace(Regex("""[+\-–,:;]"""), " ")
        clean = clean.trim().replace(Regex("""\s+"""), " ")

        return if (clean.length in 2..40) clean else "College Course"
    }
}
