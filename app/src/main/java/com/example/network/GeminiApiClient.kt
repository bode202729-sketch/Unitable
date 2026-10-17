package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ClassType
import com.example.data.model.DayOfWeekEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedScheduleResult(
    val dayOfWeek: String,
    val subject: String,
    val startTime: String,
    val endTime: String,
    val groupName: String,
    val place: String,
    val classType: String,
    val colorHex: String,
    val confidence: String = "AI"
)

data class AiDesignSuggestion(
    val title: String,
    val description: String,
    val recommendedThemeId: String,
    val recommendedLayout: String,
    val subjectColorMappings: Map<String, String> = emptyMap()
)

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun hasApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun parseScheduleText(inputText: String): ParsedScheduleResult? = withContext(Dispatchers.IO) {
        if (!hasApiKey()) return@withContext null

        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            You are a university schedule extraction assistant. 
            The user said or typed a class schedule entry in either Arabic or English.
            Extract:
            1. dayOfWeek: One of [SATURDAY, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]
            2. subject: Name of the course / subject
            3. startTime: 24-hour time "HH:mm" (e.g. "09:00", "14:30")
            4. endTime: 24-hour time "HH:mm" (e.g. "11:00", "16:00"). If only duration or start is given, assume 2 hours.
            5. groupName: Group or section identifier (e.g. "Group 1", "Section B", "مجموعة أ", "سكشن 2")
            6. place: Hall, room, or lab location (e.g. "Room 302", "Hall B", "مدرج 1", "معمل الحاسب")
            7. classType: One of [LECTURE, LAB, SECTION, SEMINAR, EXAM, OTHER]
            8. colorHex: Harmonious hex color code (e.g. "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6")

            Input text: "$inputText"

            Respond strictly with a valid JSON object only (no markdown, no backticks, no comments):
            {
              "dayOfWeek": "SUNDAY",
              "subject": "Calculus",
              "startTime": "09:00",
              "endTime": "11:00",
              "groupName": "Group A",
              "place": "Hall 101",
              "classType": "LECTURE",
              "colorHex": "#3B82F6"
            }
        """.trimIndent()

        try {
            val responseText = executeGeminiRequest(apiKey, prompt) ?: return@withContext null
            val cleanJson = cleanJsonString(responseText)
            val json = JSONObject(cleanJson)

            ParsedScheduleResult(
                dayOfWeek = DayOfWeekEnum.fromString(json.optString("dayOfWeek", "SUNDAY")).name,
                subject = json.optString("subject", "Class"),
                startTime = normalizeTime(json.optString("startTime", "09:00")),
                endTime = normalizeTime(json.optString("endTime", "11:00")),
                groupName = json.optString("groupName", ""),
                place = json.optString("place", ""),
                classType = ClassType.fromString(json.optString("classType", "LECTURE")).name,
                colorHex = json.optString("colorHex", "#3B82F6"),
                confidence = "Gemini AI"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing schedule with Gemini", e)
            null
        }
    }

    suspend fun suggestTableDesign(coursesSummary: String, isArabic: Boolean): AiDesignSuggestion? = withContext(Dispatchers.IO) {
        if (!hasApiKey()) return@withContext null

        val apiKey = BuildConfig.GEMINI_API_KEY
        val lang = if (isArabic) "Arabic" else "English"
        val prompt = """
            You are an expert graphic designer and academic timetable planner.
            Analyze this college course list and suggest an optimal timetable visual design and layout:
            Courses:
            $coursesSummary

            Themes available in the app:
            - EMERALD (Emerald Academic, fresh teal and green)
            - ROYAL_INDIGO (Royal Indigo, prestigious navy and violet)
            - SUNSET_WARMTH (Sunset Coral, energetic coral and amber)
            - CYBER_DARK (Midnight Cyber, dark mode with neon accents)
            - NORDIC_PASTEL (Nordic Pastel, soft calm sage and lavender)
            - CLASSIC_CAMPUS (Classic Campus, traditional oxford and gold)

            Layouts available:
            - WEEK_MATRIX (Grid of days vs time slots)
            - DAY_CARDS (Day by day cards)
            - COMPACT_LIST (Compact categorized list)

            Respond in $lang strictly with a JSON object:
            {
              "title": "Design suggestion title",
              "description": "2-3 sentences explaining why this theme and layout fit the user's workload, labs vs lectures, and schedule density.",
              "recommendedThemeId": "EMERALD",
              "recommendedLayout": "WEEK_MATRIX"
            }
        """.trimIndent()

        try {
            val responseText = executeGeminiRequest(apiKey, prompt) ?: return@withContext null
            val cleanJson = cleanJsonString(responseText)
            val json = JSONObject(cleanJson)

            AiDesignSuggestion(
                title = json.optString("title", if (isArabic) "تصميم مقترح لجدولك" else "Suggested Schedule Design"),
                description = json.optString("description", ""),
                recommendedThemeId = json.optString("recommendedThemeId", "EMERALD"),
                recommendedLayout = json.optString("recommendedLayout", "WEEK_MATRIX")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting design suggestions from Gemini", e)
            null
        }
    }

    private fun executeGeminiRequest(apiKey: String, promptText: String): String? {
        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            }
            put("generationConfig", genConfig)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini request failed: ${response.code} ${response.message}")
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val resJson = JSONObject(responseBody)
        val candidates = resJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null
        return parts.getJSONObject(0).optString("text")
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        }
        if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun normalizeTime(time: String): String {
        val cleaned = time.trim()
        return if (cleaned.matches(Regex("""^\d:\d{2}$"""))) {
            "0$cleaned"
        } else if (cleaned.matches(Regex("""^\d{2}:\d{2}$"""))) {
            cleaned
        } else {
            "09:00"
        }
    }
}
