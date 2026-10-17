package com.example.data.repository

import android.content.Context
import com.example.data.local.ScheduleDatabase
import com.example.data.model.ClassType
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.LayoutMode
import com.example.data.model.ScheduleItem
import com.example.data.model.TableDesignTheme
import com.example.widget.ScheduleWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScheduleRepository(private val context: Context) {

    private val db = ScheduleDatabase.getInstance(context)
    private val dao = db.scheduleDao()
    private val prefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)

    fun getAllItems(): Flow<List<ScheduleItem>> = dao.getAllItems()

    fun getItemsForDay(day: String): Flow<List<ScheduleItem>> = dao.getItemsForDay(day)

    suspend fun insertItem(item: ScheduleItem): Long = withContext(Dispatchers.IO) {
        val id = dao.insertItem(item)
        ScheduleWidgetProvider.updateAllWidgets(context)
        id
    }

    suspend fun updateItem(item: ScheduleItem) = withContext(Dispatchers.IO) {
        dao.updateItem(item)
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteItem(item: ScheduleItem) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    // --- Preferences for Design and Settings ---

    fun getSelectedThemeId(): String {
        return prefs.getString("selected_theme_id", TableDesignTheme.EMERALD_ACADEMIC.id)
            ?: TableDesignTheme.EMERALD_ACADEMIC.id
    }

    fun setSelectedThemeId(themeId: String) {
        prefs.edit().putString("selected_theme_id", themeId).apply()
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    fun getLayoutMode(): LayoutMode {
        val name = prefs.getString("layout_mode", LayoutMode.WEEK_MATRIX.name)
        return try {
            LayoutMode.valueOf(name ?: LayoutMode.WEEK_MATRIX.name)
        } catch (_: Exception) {
            LayoutMode.WEEK_MATRIX
        }
    }

    fun setLayoutMode(mode: LayoutMode) {
        prefs.edit().putString("layout_mode", mode.name).apply()
    }

    fun getTimetableTitle(): String {
        return prefs.getString("timetable_title", "College Timetable") ?: "College Timetable"
    }

    fun setTimetableTitle(title: String) {
        prefs.edit().putString("timetable_title", title).apply()
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    fun isArabic(): Boolean {
        return prefs.getBoolean("is_arabic", false)
    }

    fun setArabic(isArabic: Boolean) {
        prefs.edit().putBoolean("is_arabic", isArabic).apply()
        ScheduleWidgetProvider.updateAllWidgets(context)
    }

    companion object {
        val SAMPLE_SUBJECTS = listOf(
            "Calculus III",
            "Data Structures Lab",
            "Computer Architecture",
            "Linear Algebra",
            "Operating Systems Lab",
            "Database Systems",
            "Software Engineering"
        )
    }

    // Remove any sample/preloaded data from the database
    suspend fun removeSampleItems() = withContext(Dispatchers.IO) {
        dao.deleteBySubjects(SAMPLE_SUBJECTS)
        prefs.edit().putBoolean("samples_cleared", true).apply()
        ScheduleWidgetProvider.updateAllWidgets(context)
    }
}
