package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.TableDesignTheme
import com.example.util.PdfExportOptions
import com.example.util.SchedulePdfExporter
import java.io.File

@Composable
fun PdfExportDialog(
    items: List<ScheduleItem>,
    timetableTitle: String,
    theme: TableDesignTheme,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isLandscape by remember { mutableStateOf(false) }
    var includeSummary by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }

    fun buildPdf(onReady: ((File) -> Unit)? = null) {
        if (items.isEmpty()) {
            Toast.makeText(
                context,
                if (isArabic) "لا توجد مواد لتصديرها! أضف مواد أولاً" else "No classes to export! Add classes first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        isGenerating = true
        try {
            val file = SchedulePdfExporter.generatePdf(
                context = context,
                items = items,
                title = timetableTitle,
                theme = theme,
                options = PdfExportOptions(
                    isLandscape = isLandscape,
                    includeSummary = includeSummary,
                    isArabic = isArabic
                )
            )
            generatedFile = file
            isGenerating = false
            onReady?.invoke(file)
        } catch (e: Exception) {
            isGenerating = false
            Toast.makeText(
                context,
                if (isArabic) "خطأ في إنشاء ملف PDF: ${e.message}" else "Error creating PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Auto-generate on open
    LaunchedEffect(isLandscape, includeSummary) {
        if (items.isNotEmpty()) {
            buildPdf()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("pdf_export_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "تصدير وطباعة الجدول PDF" else "Export & Print Schedule PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "${items.size} مادة دراسية" else "${items.size} scheduled classes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PDF Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "ملف PDF جاهز للطباعة والمشاركة" else "Print-ready PDF Timetable Document",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic)
                                "• تنسيق رسمي أنيق يتضمن أسماء القاعات، المجموعات، وأوقات المحاضرات.\n• يدعم الخطوط العربية والإنجليزية وتدرجات الألوان المناسبة للطباعة الورقية."
                            else
                                "• High-resolution vector document ready for A4 printing or saving.\n• Clean bilingual layout with room allocations, groups & subject types.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options Section
                Text(
                    text = if (isArabic) "خيارات التنسيق والطباعة:" else "Export Options:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Orientation Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isLandscape,
                        onClick = { isLandscape = false },
                        label = { Text(if (isArabic) "عمودي (Portrait)" else "Portrait A4") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isLandscape,
                        onClick = { isLandscape = true },
                        label = { Text(if (isArabic) "أفقي (Landscape)" else "Landscape A4") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle summary stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "إضافة ملخص الإحصائيات (عدد الساعات والمواد)" else "Include KPI stats summary block",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = includeSummary,
                        onCheckedChange = { includeSummary = it }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Action: Open / View PDF
                Button(
                    onClick = {
                        val file = generatedFile
                        if (file != null && file.exists()) {
                            SchedulePdfExporter.openPdf(context, file, isArabic)
                        } else {
                            buildPdf { readyFile ->
                                SchedulePdfExporter.openPdf(context, readyFile, isArabic)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("open_pdf_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "عرض ملف الـ PDF" else "Open / View PDF",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Actions: Share & Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val file = generatedFile
                            if (file != null && file.exists()) {
                                SchedulePdfExporter.sharePdf(context, file, isArabic)
                            } else {
                                buildPdf { readyFile ->
                                    SchedulePdfExporter.sharePdf(context, readyFile, isArabic)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("share_pdf_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "مشاركة" else "Share")
                    }

                    OutlinedButton(
                        onClick = {
                            val file = generatedFile
                            if (file != null && file.exists()) {
                                SchedulePdfExporter.printPdf(context, file, isArabic)
                            } else {
                                buildPdf { readyFile ->
                                    SchedulePdfExporter.printPdf(context, readyFile, isArabic)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("print_pdf_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "طباعة" else "Print")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
