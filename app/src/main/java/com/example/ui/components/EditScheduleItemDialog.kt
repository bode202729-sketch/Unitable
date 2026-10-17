package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ClassType
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.ScheduleItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScheduleItemDialog(
    item: ScheduleItem,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit,
    onDelete: ((ScheduleItem) -> Unit)? = null
) {
    var subject by remember { mutableStateOf(item.subject) }
    var selectedDay by remember { mutableStateOf(DayOfWeekEnum.fromString(item.dayOfWeek)) }
    var startTime by remember { mutableStateOf(item.startTime) }
    var endTime by remember { mutableStateOf(item.endTime) }
    var groupName by remember { mutableStateOf(item.groupName) }
    var place by remember { mutableStateOf(item.place) }
    var classType by remember { mutableStateOf(ClassType.fromString(item.classType)) }
    var selectedColor by remember { mutableStateOf(item.colorHex) }
    var instructor by remember { mutableStateOf(item.instructor) }

    var dayExpanded by remember { mutableStateOf(false) }

    val palette = listOf(
        "#3B82F6", // Blue
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#06B6D4", // Cyan
        "#EF4444", // Red
        "#14B8A6", // Teal
        "#64748B"  // Slate
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("edit_schedule_dialog"),
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
                    Text(
                        text = if (item.id == 0L)
                            (if (isArabic) "تأكيد أو تعديل تفاصيل المحاضرة" else "Confirm Class Details")
                        else
                            (if (isArabic) "تعديل المحاضرة" else "Edit Class"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (onDelete != null && item.id != 0L) {
                        IconButton(
                            onClick = { onDelete(item) },
                            modifier = Modifier.testTag("delete_item_icon_button")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Name
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(if (isArabic) "اسم المادة / المقرر" else "Course / Subject") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Day Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedDay.displayName(isArabic),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isArabic) "اليوم" else "Day of the Week") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            .testTag("day_dropdown_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        DayOfWeekEnum.entries.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day.displayName(isArabic)) },
                                onClick = {
                                    selectedDay = day
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Times (Start and End)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text(if (isArabic) "البداية (09:00)" else "Start (09:00)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_time_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text(if (isArabic) "النهاية (11:00)" else "End (11:00)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("end_time_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Group & Place
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text(if (isArabic) "المجموعة / سكشن" else "Group / Sec") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = place,
                        onValueChange = { place = it },
                        label = { Text(if (isArabic) "المكان / مدرج / قاعة" else "Place / Room") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("place_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Class Type Chips
                Text(
                    text = if (isArabic) "نوع الحصة:" else "Class Type:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ClassType.entries.forEach { type ->
                        val isSelected = classType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    classType = type
                                    selectedColor = type.defaultColorHex
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.displayName(isArabic),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color Swatches
                Text(
                    text = if (isArabic) "لون البطاقة:" else "Color Tag:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { hex ->
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            Color.Blue
                        }
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Instructor
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text(if (isArabic) "اسم الدكتور / المحاضر (اختياري)" else "Instructor (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instructor_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                onSave(
                                    item.copy(
                                        subject = subject.trim(),
                                        dayOfWeek = selectedDay.name,
                                        startTime = startTime.trim(),
                                        endTime = endTime.trim(),
                                        groupName = groupName.trim(),
                                        place = place.trim(),
                                        classType = classType.name,
                                        colorHex = selectedColor,
                                        instructor = instructor.trim()
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_item_button")
                    ) {
                        Text(if (isArabic) "حفظ في الجدول" else "Save to Timetable")
                    }
                }
            }
        }
    }
}
