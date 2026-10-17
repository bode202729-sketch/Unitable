package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.ScheduleItem
import com.example.data.model.TableDesignTheme

@Composable
fun CompactListView(
    items: List<ScheduleItem>,
    theme: TableDesignTheme,
    isArabic: Boolean,
    onItemClick: (ScheduleItem) -> Unit
) {
    val groupedByDay = items.groupBy { DayOfWeekEnum.fromString(it.dayOfWeek) }
        .toSortedMap(compareBy { it.order })

    val cardBg = try { Color(android.graphics.Color.parseColor(theme.cardBackgroundHex)) } catch (_: Exception) { Color.White }
    val borderColor = try { Color(android.graphics.Color.parseColor(theme.cardBorderHex)) } catch (_: Exception) { Color.LightGray }

    Column(
        modifier = Modifier.fillMaxWidth().testTag("compact_list_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        groupedByDay.forEach { (day, dayItems) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                // Day Title Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${day.displayName(isArabic)} (${dayItems.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                ) {
                    dayItems.forEachIndexed { index, item ->
                        val itemColor = try {
                            Color(android.graphics.Color.parseColor(item.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(itemColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.subject,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = listOfNotNull(
                                            item.groupName.ifBlank { null },
                                            item.place.ifBlank { null }
                                        ).joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Time Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(itemColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${item.startTime} - ${item.endTime}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor
                                )
                            }
                        }

                        if (index < dayItems.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(borderColor.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}
