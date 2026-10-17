package com.example.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.local.ScheduleDatabase
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.ScheduleItem
import java.util.Calendar

class ScheduleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ScheduleRemoteViewsFactory(applicationContext)
    }
}

class ScheduleRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<ScheduleItem> = emptyList()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        val identityToken = Binder.clearCallingIdentity()
        try {
            loadData()
        } finally {
            Binder.restoreCallingIdentity(identityToken)
        }
    }

    private fun loadData() {
        try {
            val db = ScheduleDatabase.getInstance(context)
            val calendar = Calendar.getInstance()
            val todayEnum = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> DayOfWeekEnum.SATURDAY
                Calendar.SUNDAY -> DayOfWeekEnum.SUNDAY
                Calendar.MONDAY -> DayOfWeekEnum.MONDAY
                Calendar.TUESDAY -> DayOfWeekEnum.TUESDAY
                Calendar.WEDNESDAY -> DayOfWeekEnum.WEDNESDAY
                Calendar.THURSDAY -> DayOfWeekEnum.THURSDAY
                Calendar.FRIDAY -> DayOfWeekEnum.FRIDAY
                else -> DayOfWeekEnum.SUNDAY
            }

            // First query today's classes
            val todayClasses = db.scheduleDao().getItemsForDaySync(todayEnum.name)
            if (todayClasses.isNotEmpty()) {
                items = todayClasses
            } else {
                // If no classes today, show full week schedule sorted by weekday order then start time
                val all = db.scheduleDao().getAllItemsSync()
                items = all.sortedWith(
                    compareBy({ DayOfWeekEnum.fromString(it.dayOfWeek).order }, { it.startTime })
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            items = emptyList()
        }
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= items.size) return null
        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.widget_schedule_item)

        // Day label
        val dayEnum = DayOfWeekEnum.fromString(item.dayOfWeek)
        views.setTextViewText(R.id.widget_item_day, "${dayEnum.shortArabic} | ${dayEnum.shortEnglish}")

        views.setTextViewText(R.id.widget_item_subject, item.subject)
        views.setTextViewText(R.id.widget_item_time, "${item.startTime} - ${item.endTime}")

        val groupText = if (item.groupName.isNotBlank()) item.groupName else item.classType
        views.setTextViewText(R.id.widget_item_group, groupText)

        if (item.place.isNotBlank()) {
            views.setTextViewText(R.id.widget_item_place, item.place)
            views.setViewVisibility(R.id.widget_item_place, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_item_place, View.GONE)
        }

        // Color bar indicator
        try {
            val colorInt = Color.parseColor(item.colorHex)
            views.setInt(R.id.widget_item_color_bar, "setBackgroundColor", colorInt)
        } catch (_: Exception) {
            views.setInt(R.id.widget_item_color_bar, "setBackgroundColor", Color.parseColor("#38BDF8"))
        }

        // Fill in intent for click action to open app
        val fillInIntent = Intent().apply {
            putExtra("selected_day", item.dayOfWeek)
            putExtra("item_id", item.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = items.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
