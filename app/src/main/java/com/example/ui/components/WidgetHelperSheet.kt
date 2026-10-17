package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ScheduleItem
import com.example.widget.ScheduleWidgetProvider

@Composable
fun WidgetHelperSheet(
    todayItems: List<ScheduleItem>,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun pinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, ScheduleWidgetProvider::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                Toast.makeText(
                    context,
                    if (isArabic) "تم إرسال طلب تثبيت الويدجت إلى الشاشة الرئيسية" else "Widget pin request sent to your Home Screen!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    if (isArabic) "مشغّل جهازك لا يدعم التثبيت المباشر، يرجى إضافته يدوياً بالضغط المطول" else "Pinning not supported by your launcher. Long-press your home screen to add.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                if (isArabic) "اضغط مطولاً على الشاشة الرئيسية ثم اختر 'تطبيقات مصغرة / Widgets'" else "Long-press your home screen to add the widget",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("widget_helper_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "ويدجت الجدول للشاشة الرئيسية" else "Home Screen Timetable Widget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_widget_helper_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isArabic)
                        "يمكنك تثبيت ويدجت أنيق على شاشة هاتفك يعرض مواعيد محاضراتك اليومية والقاعات والمجموعات بشكل فوري ومباشر."
                    else
                        "Add a sleek glanceable timetable widget to your phone's home screen showing your classes, times, groups, and halls in real time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Live Preview of Widget
                Text(
                    text = if (isArabic) "معاينة شكل الويدجت في هاتفك:" else "Live Widget Preview:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Realistic Widget Card Mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        // Widget Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "UniSchedule • جدول المحاضرات",
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isArabic) "جدول اليوم • محدّث تلقائياً" else "Today's Schedule • Auto-Synced",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF38BDF8))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "TODAY",
                                    color = Color(0xFF0F172A),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF334155)))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Preview Rows
                        val previewList = if (todayItems.isNotEmpty()) todayItems.take(3) else listOf(
                            ScheduleItem(subject = "Calculus III", startTime = "09:00", endTime = "11:00", groupName = "Group A", place = "Hall 101", dayOfWeek = "SUNDAY", colorHex = "#38BDF8"),
                            ScheduleItem(subject = "Data Structures Lab", startTime = "11:30", endTime = "13:30", groupName = "Sec 2", place = "Lab 4B", dayOfWeek = "SUNDAY", colorHex = "#10B981")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            previewList.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (_: Exception) { Color(0xFF38BDF8) }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.subject,
                                                color = Color(0xFFF1F5F9),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${item.startTime} - ${item.endTime}",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (item.groupName.isNotBlank()) {
                                                    Text(text = " • ", color = Color(0xFF64748B), fontSize = 10.sp)
                                                    Text(
                                                        text = item.groupName,
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                        if (item.place.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF334155))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = item.place,
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // One-tap Add Widget Button
                Button(
                    onClick = { pinWidget() },
                    modifier = Modifier.fillMaxWidth().testTag("pin_widget_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "إضافة الويدجت إلى الشاشة الرئيسية" else "Add Widget to Home Screen",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Refresh widgets immediately
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        ScheduleWidgetProvider.updateAllWidgets(context)
                        Toast.makeText(
                            context,
                            if (isArabic) "تم تحديث ومزامنة بيانات الويدجت بنجاح!" else "Widget updated and synchronized successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("refresh_widget_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تحديث ومزامنة الويدجت الآن" else "Sync / Refresh Widget Now",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isArabic) "طريقة الإضافة اليدوية:" else "Manual Step-by-Step Guide:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic)
                                "1. اخرج إلى الشاشة الرئيسية لهاتفك.\n2. اضغط مطولاً في أي مساحة فارغة.\n3. اختر التطبيقات المصغرة (Widgets).\n4. ابحث عن \"UniSchedule\" واسحب الويدجت للشاشة!"
                            else
                                "1. Go to your phone's Home Screen.\n2. Touch and hold any empty area.\n3. Tap 'Widgets'.\n4. Search for 'UniSchedule' and drag it to your screen!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (isArabic) "إغلاق" else "Close")
                }
            }
        }
    }
}
