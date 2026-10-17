package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.ScheduleItem
import com.example.data.model.TableDesignTheme

@Composable
fun WeekMatrixTable(
    items: List<ScheduleItem>,
    theme: TableDesignTheme,
    isArabic: Boolean,
    onItemClick: (ScheduleItem) -> Unit,
    onCellAddClick: (DayOfWeekEnum, String) -> Unit
) {
    val days = listOf(
        DayOfWeekEnum.SATURDAY,
        DayOfWeekEnum.SUNDAY,
        DayOfWeekEnum.MONDAY,
        DayOfWeekEnum.TUESDAY,
        DayOfWeekEnum.WEDNESDAY,
        DayOfWeekEnum.THURSDAY
    )

    val timeSlots = listOf(
        "08:00 - 10:00" to (8 to 10),
        "10:00 - 12:00" to (10 to 12),
        "12:00 - 14:00" to (12 to 14),
        "14:00 - 16:00" to (14 to 16),
        "16:00 - 18:00" to (16 to 18)
    )

    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    val headerPrimary = try { Color(android.graphics.Color.parseColor(theme.primaryHeaderHex)) } catch (_: Exception) { Color.DarkGray }
    val borderColor = try { Color(android.graphics.Color.parseColor(theme.cardBorderHex)) } catch (_: Exception) { Color.LightGray }
    val cardBg = try { Color(android.graphics.Color.parseColor(theme.cardBackgroundHex)) } catch (_: Exception) { Color.White }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("week_matrix_table")
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
        ) {
            Column(
                modifier = Modifier.width(680.dp)
            ) {
                // Header Row with Days
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerPrimary)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time column header
                    Text(
                        text = if (isArabic) "الوقت / اليوم" else "Time / Day",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(80.dp)
                    )

                    days.forEach { day ->
                        Column(
                            modifier = Modifier.width(100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.displayName(isArabic),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = day.shortName(isArabic),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Table Rows
                timeSlots.forEachIndexed { index, (slotLabel, slotRange) ->
                    val isEvenRow = index % 2 == 0
                    val rowBg = if (isEvenRow) cardBg else cardBg.copy(alpha = 0.95f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(rowBg)
                            .border(0.5.dp, borderColor.copy(alpha = 0.6f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time Column
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = slotLabel.replace(" - ", "\n"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }

                        // Day Cells
                        days.forEach { day ->
                            // Find item that overlaps with this time slot on this day
                            val matchingItem = items.firstOrNull { item ->
                                if (item.dayOfWeek != day.name) return@firstOrNull false
                                val startHour = item.startTime.substringBefore(":").toIntOrNull() ?: -1
                                startHour in slotRange.first until slotRange.second
                            }

                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(96.dp)
                                    .border(0.5.dp, borderColor.copy(alpha = 0.4f))
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (matchingItem != null) {
                                    val blockColor = try {
                                        Color(android.graphics.Color.parseColor(matchingItem.colorHex))
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(blockColor.copy(alpha = 0.15f))
                                            .border(1.5.dp, blockColor, RoundedCornerShape(8.dp))
                                            .clickable { onItemClick(matchingItem) }
                                            .padding(5.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = matchingItem.subject,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 13.sp
                                            )

                                            Column {
                                                if (matchingItem.groupName.isNotBlank()) {
                                                    Text(
                                                        text = matchingItem.groupName,
                                                        fontSize = 9.sp,
                                                        color = blockColor,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1
                                                    )
                                                }
                                                if (matchingItem.place.isNotBlank()) {
                                                    Text(
                                                        text = matchingItem.place,
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Empty cell, clicking triggers add on this slot
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                val startTime = "%02d:00".format(slotRange.first)
                                                onCellAddClick(day, startTime)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
