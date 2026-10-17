package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.LayoutMode
import com.example.data.model.ScheduleItem
import com.example.ui.ScheduleViewModel
import com.example.ui.components.CompactListView
import com.example.ui.components.DailyCardsView
import com.example.ui.components.DesignSuggestionsSheet
import com.example.ui.components.EditScheduleItemDialog
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.VoiceInputDialog
import com.example.ui.components.WeekMatrixTable
import com.example.ui.components.WidgetHelperSheet
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showVoiceDialog by remember { mutableStateOf(false) }
    var showDesignSheet by remember { mutableStateOf(false) }
    var showWidgetHelper by remember { mutableStateOf(false) }
    var showPdfDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ScheduleItem?>(null) }
    var showMenu by remember { mutableStateOf(false) }

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

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfoMessage()
        }
    }

    LaunchedEffect(state.parsedCandidate) {
        if (state.parsedCandidate != null) {
            showDesignSheet = true
        }
    }

    val layoutDirection = if (state.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = modifier.fillMaxSize().testTag("home_screen_root"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = state.timetableTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = if (state.isArabic) "تنظيم الجدول بالصوت وتصميم ذكي" else "Voice Timetable & Smart Widget",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // Language Toggle
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (state.isArabic) "EN" else "عربي",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Design Suggestions Button
                        IconButton(
                            onClick = { showDesignSheet = true },
                            modifier = Modifier.testTag("design_suggestions_button")
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Design Themes & Suggestions",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // PDF Export Button
                        IconButton(
                            onClick = { showPdfDialog = true },
                            modifier = Modifier.testTag("export_pdf_button")
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = "Export PDF",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Widget Helper Button
                        IconButton(
                            onClick = { showWidgetHelper = true },
                            modifier = Modifier.testTag("widget_helper_button")
                        ) {
                            Icon(
                                Icons.Default.Widgets,
                                contentDescription = "Add Widget to Phone",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // More Menu
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (state.isArabic) "إضافة مادة يدوياً" else "Add Class Manually") },
                                onClick = {
                                    showMenu = false
                                    itemToEdit = ScheduleItem(
                                        dayOfWeek = state.selectedDay?.name ?: DayOfWeekEnum.SUNDAY.name,
                                        subject = "",
                                        startTime = "09:00",
                                        endTime = "11:00"
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (state.isArabic) "📄 تصدير وطباعة الجدول كـ PDF" else "📄 Export & Print PDF Timetable") },
                                onClick = {
                                    showMenu = false
                                    showPdfDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isArabic) "📱 إعداد ويدجت الشاشة الرئيسية" else "📱 Home Screen Widget Setup") },
                                onClick = {
                                    showMenu = false
                                    showWidgetHelper = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isArabic) "مسح كل المواد" else "Clear All Classes") },
                                onClick = {
                                    showMenu = false
                                    viewModel.clearAll()
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Manual Add FAB
                    FloatingActionButton(
                        onClick = {
                            itemToEdit = ScheduleItem(
                                dayOfWeek = state.selectedDay?.name ?: DayOfWeekEnum.SUNDAY.name,
                                subject = "",
                                startTime = "09:00",
                                endTime = "11:00"
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(48.dp).testTag("manual_add_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Manually")
                    }

                    // Primary Voice Mic FAB
                    ExtendedFloatingActionButton(
                        onClick = { showVoiceDialog = true },
                        icon = {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        text = {
                            Text(
                                text = if (state.isArabic) "تحدث لإضافة مادة" else "Speak to Add",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("voice_fab")
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Next Class Countdown or Today Highlight Card
                val todayItems = state.items.filter { it.dayOfWeek == todayEnum.name }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("today_highlight_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isArabic) "اليوم: ${todayEnum.arabicName}" else "Today: ${todayEnum.englishName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (todayItems.isNotEmpty()) {
                                    val next = todayItems.first()
                                    if (state.isArabic)
                                        "المحاضرة القادمة: ${next.subject} (${next.startTime}) - ${next.place}"
                                    else
                                        "Upcoming: ${next.subject} (${next.startTime}) at ${next.place}"
                                } else {
                                    if (state.isArabic) "لا توجد محاضرات لباقي اليوم" else "No classes scheduled for today"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Quick Widget Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showWidgetHelper = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isArabic) "الويدجت" else "Widget",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice / Text Schedule Assistant Card
                var quickInputText by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_voice_assistant_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (state.isArabic) "إضافة مادة بالصوت أو النص" else "Say or Type Your Schedule",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (state.isArabic) "اليوم + المادة + الوقت + المجموعة + المكان" else "Day + Subject + Time + Group + Place",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = quickInputText,
                            onValueChange = { quickInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_schedule_input_field"),
                            placeholder = {
                                Text(
                                    text = if (state.isArabic)
                                        "الأحد رياضيات 9-11 مجموعة 1 مدرج أ"
                                    else
                                        "Sunday Calculus 10-12 Group A Hall 101",
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(
                                    onClick = { showVoiceDialog = true },
                                    modifier = Modifier.testTag("open_voice_dialog_inline")
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Example Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val samplePhrases = if (state.isArabic) listOf(
                                "الأحد رياضيات من 9 ل 11 مجموعة 1 مدرج أ",
                                "الثلاثاء معمل حاسب من 1 ل 3 سكشن 2 معمل 4",
                                "الأربعاء فيزياء من 8:30 ل 10:30 قاعة 205"
                            ) else listOf(
                                "Sunday Calculus from 10:00 to 12:00 Group A Hall 101",
                                "Monday Physics Lab from 1:00 to 3:00 Sec 2 Lab B",
                                "Wednesday Database from 8:30 to 10:30 Room 302"
                            )

                            samplePhrases.forEach { phrase ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { quickInputText = phrase }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = phrase,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (quickInputText.isNotBlank()) {
                                    viewModel.processNaturalLanguageInput(quickInputText)
                                    quickInputText = ""
                                }
                            },
                            enabled = quickInputText.isNotBlank() && !state.isParsingInput,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analyze_and_suggest_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isParsingInput) {
                                Text(if (state.isArabic) "جاري التحليل واقتراح التصميم..." else "Analyzing & Suggesting Design...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isArabic) "✨ إضافة واقتراح تصميم الجدول" else "✨ Add & Suggest Table Design",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day Selector Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "All Week" Chip
                    val isAllSelected = state.selectedDay == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .clickable { viewModel.selectDay(null) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("day_chip_all")
                    ) {
                        Text(
                            text = if (state.isArabic) "كل الأسبوع" else "All Week",
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Day Chips
                    DayOfWeekEnum.entries.forEach { day ->
                        val isSelected = state.selectedDay == day
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .clickable { viewModel.selectDay(day) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("day_chip_${day.name}")
                        ) {
                            Text(
                                text = day.displayName(state.isArabic),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Timetable Content
                when (state.layoutMode) {
                    LayoutMode.WEEK_MATRIX -> {
                        WeekMatrixTable(
                            items = state.items,
                            theme = state.currentTheme,
                            isArabic = state.isArabic,
                            onItemClick = { itemToEdit = it },
                            onCellAddClick = { day, time ->
                                itemToEdit = ScheduleItem(
                                    dayOfWeek = day.name,
                                    subject = "",
                                    startTime = time,
                                    endTime = "%02d:00".format((time.substringBefore(":").toInt() + 2).coerceAtMost(23))
                                )
                            }
                        )
                    }
                    LayoutMode.DAY_CARDS -> {
                        DailyCardsView(
                            items = state.filteredItems,
                            theme = state.currentTheme,
                            isArabic = state.isArabic,
                            onItemClick = { itemToEdit = it },
                            onDeleteClick = { viewModel.deleteItem(it) }
                        )
                    }
                    LayoutMode.COMPACT_LIST -> {
                        CompactListView(
                            items = state.filteredItems,
                            theme = state.currentTheme,
                            isArabic = state.isArabic,
                            onItemClick = { itemToEdit = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prominent Home Screen Widget Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_screen_widget_promo_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isArabic) "ويدجت الجدول لشاشة هاتفك" else "Phone Home Screen Widget",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (state.isArabic) "عرض المحاضرات والقاعات مباشرة على الشاشة الرئيسية" else "Glance at classes, halls & times on your home screen",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = { showWidgetHelper = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_widget_promo_button")
                        ) {
                            Text(if (state.isArabic) "إعداد الويدجت 📱" else "Add Widget 📱")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // --- Dialogs ---

        // 1. Voice Input Dialog
        if (showVoiceDialog) {
            VoiceInputDialog(
                isArabic = state.isArabic,
                isParsing = state.isParsingInput,
                onDismiss = { showVoiceDialog = false },
                onSubmitInput = { input ->
                    showVoiceDialog = false
                    viewModel.processNaturalLanguageInput(input)
                }
            )
        }

        // 2. Candidate Dialog (Shown after voice/text parsed for confirmation)
        state.parsedCandidate?.let { candidate ->
            EditScheduleItemDialog(
                item = candidate,
                isArabic = state.isArabic,
                onDismiss = { viewModel.dismissCandidate() },
                onSave = { updatedItem ->
                    viewModel.saveItem(updatedItem)
                }
            )
        }

        // 3. Manual Edit Dialog
        itemToEdit?.let { item ->
            EditScheduleItemDialog(
                item = item,
                isArabic = state.isArabic,
                onDismiss = { itemToEdit = null },
                onSave = { updatedItem ->
                    viewModel.saveItem(updatedItem)
                    itemToEdit = null
                },
                onDelete = { itemToDelete ->
                    viewModel.deleteItem(itemToDelete)
                    itemToEdit = null
                }
            )
        }

        // 4. Design Suggestions Sheet
        if (showDesignSheet) {
            DesignSuggestionsSheet(
                currentTheme = state.currentTheme,
                currentLayout = state.layoutMode,
                timetableTitle = state.timetableTitle,
                isArabic = state.isArabic,
                isAiSuggesting = state.isAiSuggesting,
                aiSuggestion = state.aiSuggestion,
                parsedCandidate = state.parsedCandidate,
                onDismiss = {
                    showDesignSheet = false
                    viewModel.dismissAiSuggestion()
                    viewModel.dismissCandidate()
                },
                onSelectTheme = { themeId ->
                    viewModel.setTheme(themeId)
                },
                onSelectLayout = { layout ->
                    viewModel.setLayoutMode(layout)
                },
                onUpdateTitle = { newTitle ->
                    viewModel.setTimetableTitle(newTitle)
                },
                onRequestAiSuggestions = {
                    viewModel.requestAiDesignSuggestions()
                },
                onApplyAiSuggestion = { suggestion ->
                    viewModel.applyAiSuggestion(suggestion)
                },
                onSaveCandidateWithDesign = { themeId, layout ->
                    viewModel.saveCandidateWithDesign(themeId, layout)
                    showDesignSheet = false
                    showWidgetHelper = true
                }
            )
        }

        // 5. Widget Helper Sheet
        if (showWidgetHelper) {
            val todayClasses = state.items.filter { it.dayOfWeek == todayEnum.name }
            WidgetHelperSheet(
                todayItems = todayClasses,
                isArabic = state.isArabic,
                onDismiss = { showWidgetHelper = false }
            )
        }

        // 6. PDF Export Dialog
        if (showPdfDialog) {
            PdfExportDialog(
                items = state.items,
                timetableTitle = state.timetableTitle,
                theme = state.currentTheme,
                isArabic = state.isArabic,
                onDismiss = { showPdfDialog = false }
            )
        }
    }
}
