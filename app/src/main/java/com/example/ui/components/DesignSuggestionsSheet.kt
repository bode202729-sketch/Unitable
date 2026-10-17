package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LayoutMode
import com.example.data.model.ScheduleItem
import com.example.data.model.TableDesignTheme
import com.example.network.AiDesignSuggestion

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignSuggestionsSheet(
    currentTheme: TableDesignTheme,
    currentLayout: LayoutMode,
    timetableTitle: String,
    isArabic: Boolean,
    isAiSuggesting: Boolean,
    aiSuggestion: AiDesignSuggestion?,
    onDismiss: () -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectLayout: (LayoutMode) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onRequestAiSuggestions: () -> Unit,
    onApplyAiSuggestion: (AiDesignSuggestion) -> Unit,
    parsedCandidate: ScheduleItem? = null,
    onSaveCandidateWithDesign: ((String, LayoutMode) -> Unit)? = null
) {
    var titleInput by remember { mutableStateOf(timetableTitle) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("design_suggestions_dialog"),
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
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "اقتراحات تصميم الجدول" else "Table Design Suggestions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_design_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (parsedCandidate != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "المحاضرة المستخرجة من كلامك:" else "Extracted From Your Speech/Text:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${parsedCandidate.subject} • ${parsedCandidate.dayEnum.displayName(isArabic)} (${parsedCandidate.startTime} - ${parsedCandidate.endTime})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (parsedCandidate.groupName.isNotBlank() || parsedCandidate.place.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = listOfNotNull(
                                        parsedCandidate.groupName.ifBlank { null },
                                        parsedCandidate.place.ifBlank { null }
                                    ).joinToString(" • "),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic) "اختر النمط المناسب لجدولك بالأسفل ثم اضغط 'إنشاء الجدول':" else "Select your table style below then tap 'Make Table':",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // AI Suggestions Feature Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_suggestion_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "مستشار التصميم الذكي (Gemini AI)" else "Gemini AI Design Advisor",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic)
                                "يقوم الذكاء الاصطناعي بتحليل عدد موادك ومواعيدها وتوزيع المعامل لاقتراح أفضل نسق ألوان وتصميم شبكي يناسب دراستك."
                            else
                                "AI analyzes your course load, lab vs lecture balance, and day density to suggest optimal contrast and grid ergonomics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (aiSuggestion != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = aiSuggestion.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = aiSuggestion.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onApplyAiSuggestion(aiSuggestion) },
                                        modifier = Modifier.fillMaxWidth().testTag("apply_ai_design_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isArabic) "تطبيق هذا الاقتراح فوراً ✨" else "Apply This AI Design ✨")
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = onRequestAiSuggestions,
                                enabled = !isAiSuggesting,
                                modifier = Modifier.fillMaxWidth().testTag("request_ai_suggestions_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isAiSuggesting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isArabic) "جاري التحليل..." else "Consulting AI...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isArabic) "اقترح تصميماً لجدولي الآن" else "Suggest Design For My Schedule")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table Title Field
                Text(
                    text = if (isArabic) "عنوان الجدول:" else "Timetable Title:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        modifier = Modifier.weight(1f).testTag("table_title_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdateTitle(titleInput) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_title_button")
                    ) {
                        Text(if (isArabic) "حفظ" else "Save")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Layout Mode Selector
                Text(
                    text = if (isArabic) "طريقة العرض:" else "Layout Presentation:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayoutMode.entries.forEach { mode ->
                        val isSelected = currentLayout == mode
                        val icon = when (mode) {
                            LayoutMode.WEEK_MATRIX -> Icons.Default.ViewWeek
                            LayoutMode.DAY_CARDS -> Icons.Default.ViewAgenda
                            LayoutMode.COMPACT_LIST -> Icons.Default.ViewCompact
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectLayout(mode) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic) mode.arabicName else mode.englishName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Presets Gallery
                Text(
                    text = if (isArabic) "اختر سمة وتنسيق الألوان:" else "Color Theme & Aesthetics:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TableDesignTheme.allThemes.forEach { theme ->
                        val isSelected = currentTheme.id == theme.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    try { Color(android.graphics.Color.parseColor(theme.backgroundHex)) } catch (_: Exception) { Color.White }
                                )
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(android.graphics.Color.parseColor(theme.cardBorderHex)),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectTheme(theme.id) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isArabic) theme.nameAr else theme.nameEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = try { Color(android.graphics.Color.parseColor(theme.textPrimaryHex)) } catch (_: Exception) { Color.Black }
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isArabic) theme.descriptionAr else theme.descriptionEn,
                                        fontSize = 11.sp,
                                        color = try { Color(android.graphics.Color.parseColor(theme.textSecondaryHex)) } catch (_: Exception) { Color.Gray },
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Color Palette Preview Dots
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    theme.previewPalette.forEach { hex ->
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                                                )
                                                .border(0.5.dp, Color.White, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (parsedCandidate != null && onSaveCandidateWithDesign != null) {
                    Button(
                        onClick = {
                            onSaveCandidateWithDesign(currentTheme.id, currentLayout)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("make_table_and_add_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "✨ إنشاء الجدول بهذا التصميم وإضافة المادة" else "✨ Make Table with This Design & Add",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().testTag("close_design_sheet_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isArabic) "إتمام وتطبيق" else "Done & Apply")
                    }
                }
            }
        }
    }
}
