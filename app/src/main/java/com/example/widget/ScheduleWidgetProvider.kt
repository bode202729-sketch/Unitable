package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.DayOfWeekEnum
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_SCHEDULE_DATA_CHANGED ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == Intent.ACTION_TIME_TICK ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScheduleWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)
            }
        }
    }

    companion object {
        const val ACTION_SCHEDULE_DATA_CHANGED = "com.example.widget.SCHEDULE_DATA_CHANGED"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_schedule)

            // Current day representation
            val calendar = Calendar.getInstance()
            val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)
            val todayEnum = when (dayOfWeekInt) {
                Calendar.SATURDAY -> DayOfWeekEnum.SATURDAY
                Calendar.SUNDAY -> DayOfWeekEnum.SUNDAY
                Calendar.MONDAY -> DayOfWeekEnum.MONDAY
                Calendar.TUESDAY -> DayOfWeekEnum.TUESDAY
                Calendar.WEDNESDAY -> DayOfWeekEnum.WEDNESDAY
                Calendar.THURSDAY -> DayOfWeekEnum.THURSDAY
                Calendar.FRIDAY -> DayOfWeekEnum.FRIDAY
                else -> DayOfWeekEnum.SUNDAY
            }

            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val dateStr = dateFormat.format(calendar.time)

            views.setTextViewText(R.id.widget_title, "UniSchedule • جدول المحاضرات")
            views.setTextViewText(R.id.widget_subtitle, "$dateStr • ${todayEnum.arabicName}")
            views.setTextViewText(R.id.widget_day_badge, todayEnum.englishName.uppercase())

            // Launch app when tapping header
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_title, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_subtitle, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty_text, openAppPendingIntent)

            // Refresh button PendingIntent
            val refreshIntent = Intent(context, ScheduleWidgetProvider::class.java).apply {
                action = ACTION_SCHEDULE_DATA_CHANGED
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

            // Service intent for list items
            val serviceIntent = Intent(context, ScheduleWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse("content://com.example.collegeschedule/widget_$appWidgetId")
            }
            views.setRemoteAdapter(R.id.widget_list, serviceIntent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty_text)

            // Item click pending intent: MUST HAVE FLAG_MUTABLE because it is a template for fillInIntent!
            val itemClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val itemClickPendingIntent = PendingIntent.getActivity(
                context,
                100 + appWidgetId,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
            )
            views.setPendingIntentTemplate(R.id.widget_list, itemClickPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ScheduleWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (id in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)
                }
                val intent = Intent(context, ScheduleWidgetProvider::class.java).apply {
                    action = ACTION_SCHEDULE_DATA_CHANGED
                }
                context.sendBroadcast(intent)
            } catch (_: Exception) {
            }
        }
    }
}
