package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ClassType
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.ScheduleItem
import com.example.data.model.TableDesignTheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PdfExportOptions(
    val isLandscape: Boolean = false,
    val includeSummary: Boolean = true,
    val isArabic: Boolean = true
)

object SchedulePdfExporter {

    fun generatePdf(
        context: Context,
        items: List<ScheduleItem>,
        title: String,
        theme: TableDesignTheme,
        options: PdfExportOptions = PdfExportOptions()
    ): File {
        val pageWidth = if (options.isLandscape) 842 else 595
        val pageHeight = if (options.isLandscape) 595 else 842
        val margin = 32f

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas

        val bgPaint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        fun drawStaticText(
            text: String,
            x: Float,
            y: Float,
            width: Int,
            size: Float,
            color: Int,
            isBold: Boolean = false,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
        ): Int {
            val textPaint = TextPaint().apply {
                isAntiAlias = true
                this.color = color
                textSize = size
                typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            }
            val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxOf(10, width))
                    .setAlignment(alignment)
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(text, textPaint, maxOf(10, width), alignment, 1.0f, 0.0f, false)
            }
            canvas.save()
            canvas.translate(x, y)
            layout.draw(canvas)
            canvas.restore()
            return layout.height
        }

        var curY = margin

        // --- DRAW HEADER ---
        val headerHeight = 72f
        val headerRect = RectF(margin, curY, pageWidth - margin, curY + headerHeight)
        bgPaint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(headerRect, 10f, 10f, bgPaint)

        // Header accent stripe
        bgPaint.color = Color.parseColor("#38BDF8")
        val accentRect = RectF(margin, curY + headerHeight - 4f, pageWidth - margin, curY + headerHeight)
        canvas.drawRoundRect(accentRect, 0f, 0f, bgPaint)

        // Timetable Title
        val fullTitle = if (options.isArabic) {
            if (title.contains("جدول")) title else "$title • جدول المحاضرات الأكاديمي"
        } else {
            "$title • Academic Timetable"
        }
        drawStaticText(
            text = fullTitle,
            x = margin + 16f,
            y = curY + 14f,
            width = (pageWidth - 2 * margin - 32).toInt(),
            size = 18f,
            color = Color.WHITE,
            isBold = true
        )

        // Generation Date subtitle
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())
        val dateText = if (options.isArabic) {
            "تاريخ الإصدار: ${dateFormat.format(Date())} • السنة الجامعية الحالية"
        } else {
            "Exported: ${dateFormat.format(Date())} • Current Academic Term"
        }
        drawStaticText(
            text = dateText,
            x = margin + 16f,
            y = curY + 42f,
            width = (pageWidth - 2 * margin - 32).toInt(),
            size = 10f,
            color = Color.parseColor("#94A3B8")
        )

        curY += headerHeight + 14f

        // --- SUMMARY STATS CARDS ---
        if (options.includeSummary) {
            val totalClasses = items.size
            val distinctDays = items.map { it.dayOfWeek }.distinct().size
            val lecturesCount = items.count { it.classType == ClassType.LECTURE.name }
            val labsCount = items.count { it.classType == ClassType.LAB.name || it.classType == ClassType.SECTION.name }

            val cardWidth = (pageWidth - 2 * margin - 30f) / 4f
            val cardHeight = 44f

            val stats = listOf(
                Pair(if (options.isArabic) "إجمالي المواد" else "Total Classes", totalClasses.toString()),
                Pair(if (options.isArabic) "أيام الدراسة" else "Study Days", distinctDays.toString()),
                Pair(if (options.isArabic) "المحاضرات" else "Lectures", lecturesCount.toString()),
                Pair(if (options.isArabic) "المعامل / السكاشن" else "Labs / Sections", labsCount.toString())
            )

            stats.forEachIndexed { i, stat ->
                val cardX = margin + i * (cardWidth + 10f)
                val rect = RectF(cardX, curY, cardX + cardWidth, curY + cardHeight)
                bgPaint.color = Color.parseColor("#F1F5F9")
                canvas.drawRoundRect(rect, 8f, 8f, bgPaint)
                strokePaint.color = Color.parseColor("#CBD5E1")
                canvas.drawRoundRect(rect, 8f, 8f, strokePaint)

                drawStaticText(
                    text = stat.first,
                    x = cardX + 8f,
                    y = curY + 6f,
                    width = (cardWidth - 16f).toInt(),
                    size = 9f,
                    color = Color.parseColor("#475569")
                )
                drawStaticText(
                    text = stat.second,
                    x = cardX + 8f,
                    y = curY + 22f,
                    width = (cardWidth - 16f).toInt(),
                    size = 14f,
                    color = Color.parseColor("#0F172A"),
                    isBold = true
                )
            }

            curY += cardHeight + 16f
        }

        // Group items by Day
        val daysOrdered = DayOfWeekEnum.entries.sortedBy { it.order }
        val groupedByDay = items.groupBy { DayOfWeekEnum.fromString(it.dayOfWeek) }

        val contentWidth = pageWidth - 2 * margin

        // Helper to check for page break
        fun checkNewPage(neededHeight: Float) {
            if (curY + neededHeight > pageHeight - margin - 30f) {
                // Draw footer for current page
                drawStaticText(
                    text = if (options.isArabic) "صفحة $pageNumber • UniSchedule" else "Page $pageNumber • UniSchedule",
                    x = margin,
                    y = pageHeight - margin - 14f,
                    width = contentWidth.toInt(),
                    size = 9f,
                    color = Color.parseColor("#94A3B8"),
                    alignment = Layout.Alignment.ALIGN_CENTER
                )
                pdfDocument.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                curY = margin

                // Mini header on continuation page
                val miniHeaderRect = RectF(margin, curY, pageWidth - margin, curY + 26f)
                bgPaint.color = Color.parseColor("#1E293B")
                canvas.drawRoundRect(miniHeaderRect, 6f, 6f, bgPaint)
                drawStaticText(
                    text = fullTitle,
                    x = margin + 12f,
                    y = curY + 7f,
                    width = contentWidth.toInt() - 24,
                    size = 11f,
                    color = Color.WHITE,
                    isBold = true
                )
                curY += 36f
            }
        }

        // Table column widths
        val colTimeW = 85f
        val colSubjW = contentWidth - 365f
        val colTypeW = 65f
        val colGroupW = 65f
        val colPlaceW = 75f
        val colInstW = 75f

        for (day in daysOrdered) {
            val dayItems = groupedByDay[day] ?: continue
            if (dayItems.isEmpty()) continue

            val sortedItems = dayItems.sortedBy { it.startTime }

            // Day Header block
            checkNewPage(60f)
            val dayHeaderRect = RectF(margin, curY, pageWidth - margin, curY + 28f)
            bgPaint.color = Color.parseColor("#1E293B")
            canvas.drawRoundRect(dayHeaderRect, 6f, 6f, bgPaint)

            val dayTitleText = "${day.arabicName} • ${day.englishName} (${sortedItems.size} ${if (options.isArabic) "محاضرات" else "classes"})"
            drawStaticText(
                text = dayTitleText,
                x = margin + 12f,
                y = curY + 7f,
                width = (contentWidth - 24).toInt(),
                size = 12f,
                color = Color.parseColor("#38BDF8"),
                isBold = true
            )
            curY += 32f

            // Table Header row
            val theadRect = RectF(margin, curY, pageWidth - margin, curY + 22f)
            bgPaint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(theadRect, bgPaint)

            var headX = margin + 6f
            val headers = listOf(
                Pair(if (options.isArabic) "الوقت" else "Time", colTimeW),
                Pair(if (options.isArabic) "المادة الدراسية" else "Subject", colSubjW),
                Pair(if (options.isArabic) "النوع" else "Type", colTypeW),
                Pair(if (options.isArabic) "المجموعة" else "Group", colGroupW),
                Pair(if (options.isArabic) "المكان / القاعة" else "Room / Hall", colPlaceW)
            )

            headers.forEach { (name, width) ->
                drawStaticText(
                    text = name,
                    x = headX,
                    y = curY + 5f,
                    width = width.toInt() - 4,
                    size = 9.5f,
                    color = Color.parseColor("#334155"),
                    isBold = true
                )
                headX += width
            }
            curY += 24f

            // Table Rows
            sortedItems.forEachIndexed { rowIndex, item ->
                val rowHeight = 30f
                checkNewPage(rowHeight + 4f)

                val rowRect = RectF(margin, curY, pageWidth - margin, curY + rowHeight)
                bgPaint.color = if (rowIndex % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                canvas.drawRect(rowRect, bgPaint)

                // Row bottom border
                strokePaint.color = Color.parseColor("#E2E8F0")
                canvas.drawLine(margin, curY + rowHeight, pageWidth - margin, curY + rowHeight, strokePaint)

                // Left color stripe
                try {
                    bgPaint.color = Color.parseColor(item.colorHex)
                } catch (_: Exception) {
                    bgPaint.color = Color.parseColor("#38BDF8")
                }
                canvas.drawRect(margin, curY + 2f, margin + 4f, curY + rowHeight - 2f, bgPaint)

                var cellX = margin + 10f

                // 1. Time
                drawStaticText(
                    text = "${item.startTime} - ${item.endTime}",
                    x = cellX,
                    y = curY + 8f,
                    width = colTimeW.toInt() - 6,
                    size = 9.5f,
                    color = Color.parseColor("#0284C7"),
                    isBold = true
                )
                cellX += colTimeW

                // 2. Subject
                drawStaticText(
                    text = item.subject,
                    x = cellX,
                    y = curY + 8f,
                    width = colSubjW.toInt() - 6,
                    size = 10f,
                    color = Color.parseColor("#0F172A"),
                    isBold = true
                )
                cellX += colSubjW

                // 3. Type Pill
                val typeEnum = ClassType.fromString(item.classType)
                val typeName = if (options.isArabic) typeEnum.arabicName else typeEnum.englishName
                val pillRect = RectF(cellX, curY + 6f, cellX + colTypeW - 10f, curY + rowHeight - 6f)
                bgPaint.color = Color.parseColor("#E0F2FE")
                canvas.drawRoundRect(pillRect, 4f, 4f, bgPaint)
                drawStaticText(
                    text = typeName,
                    x = cellX + 3f,
                    y = curY + 8f,
                    width = colTypeW.toInt() - 14,
                    size = 8.5f,
                    color = Color.parseColor("#0369A1"),
                    isBold = true,
                    alignment = Layout.Alignment.ALIGN_CENTER
                )
                cellX += colTypeW

                // 4. Group
                val groupText = if (item.groupName.isNotBlank()) item.groupName else "—"
                drawStaticText(
                    text = groupText,
                    x = cellX,
                    y = curY + 8f,
                    width = colGroupW.toInt() - 6,
                    size = 9.5f,
                    color = Color.parseColor("#475569")
                )
                cellX += colGroupW

                // 5. Place / Room
                val placeText = if (item.place.isNotBlank()) item.place else "—"
                val placeRect = RectF(cellX, curY + 6f, cellX + colPlaceW - 8f, curY + rowHeight - 6f)
                bgPaint.color = Color.parseColor("#F1F5F9")
                canvas.drawRoundRect(placeRect, 4f, 4f, bgPaint)
                drawStaticText(
                    text = placeText,
                    x = cellX + 3f,
                    y = curY + 8f,
                    width = colPlaceW.toInt() - 12,
                    size = 9f,
                    color = Color.parseColor("#1E293B"),
                    isBold = true,
                    alignment = Layout.Alignment.ALIGN_CENTER
                )

                curY += rowHeight
            }

            curY += 12f
        }

        // Draw footer on last page
        drawStaticText(
            text = if (options.isArabic)
                "تم إنشاء هذا الجدول بواسطة UniSchedule • منظم الجداول الجامعية الذكي | صفحة $pageNumber"
            else
                "Generated by UniSchedule • Smart College Timetable Organizer | Page $pageNumber",
            x = margin,
            y = pageHeight - margin - 14f,
            width = contentWidth.toInt(),
            size = 9f,
            color = Color.parseColor("#94A3B8"),
            alignment = Layout.Alignment.ALIGN_CENTER
        )

        pdfDocument.finishPage(currentPage)

        // Write output to application cache directory
        val outputDir = File(context.cacheDir, "timetables").apply { mkdirs() }
        val fileName = if (options.isArabic) "جدول_المحاضرات_الجامعي.pdf" else "University_Timetable.pdf"
        val outputFile = File(outputDir, fileName)

        val fos = FileOutputStream(outputFile)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        return outputFile
    }

    fun openPdf(context: Context, file: File, isArabic: Boolean) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(
                intent,
                if (isArabic) "عرض جدول المحاضرات (PDF)" else "Open Timetable PDF"
            )
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isArabic) "يرجى تثبيت تطبيق قارئ PDF لعرض الملف" else "Please install a PDF viewer app to view the file",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun sharePdf(context: Context, file: File, isArabic: Boolean) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    if (isArabic) "جدول المحاضرات الجامعي" else "University Schedule Timetable"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (isArabic) "مرفق جدول المحاضرات الأكاديمي بتنسيق PDF" else "Attached is the university academic timetable in PDF format."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(
                intent,
                if (isArabic) "مشاركة جدول المحاضرات PDF" else "Share Timetable PDF"
            )
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isArabic) "فشلت المشاركة: ${e.message}" else "Share failed: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun printPdf(context: Context, file: File, isArabic: Boolean) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val printAdapter = object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = PrintDocumentInfo.Builder(file.name)
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val input = FileInputStream(file)
                            val output = FileOutputStream(destination?.fileDescriptor)
                            val buf = ByteArray(1024)
                            var bytesRead: Int
                            while (input.read(buf).also { bytesRead = it } > 0) {
                                output.write(buf, 0, bytesRead)
                            }
                            input.close()
                            output.close()
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }
                printManager.print(
                    if (isArabic) "طباعة جدول المحاضرات" else "Print Timetable",
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            } else {
                openPdf(context, file, isArabic)
            }
        } catch (e: Exception) {
            openPdf(context, file, isArabic)
        }
    }
}
