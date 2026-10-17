package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClassType
import com.example.data.model.DayOfWeekEnum
import com.example.data.model.LayoutMode
import com.example.data.model.ScheduleItem
import com.example.data.model.TableDesignTheme
import com.example.data.repository.ScheduleRepository
import com.example.network.AiDesignSuggestion
import com.example.network.GeminiApiClient
import com.example.util.NaturalLanguageScheduleParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val items: List<ScheduleItem> = emptyList(),
    val filteredItems: List<ScheduleItem> = emptyList(),
    val selectedDay: DayOfWeekEnum? = null, // null means whole week
    val currentTheme: TableDesignTheme = TableDesignTheme.EMERALD_ACADEMIC,
    val layoutMode: LayoutMode = LayoutMode.WEEK_MATRIX,
    val isArabic: Boolean = false,
    val timetableTitle: String = "University Timetable",
    val isParsingInput: Boolean = false,
    val parsedCandidate: ScheduleItem? = null,
    val isAiSuggesting: Boolean = false,
    val aiSuggestion: AiDesignSuggestion? = null,
    val infoMessage: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)

    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            currentTheme = TableDesignTheme.findById(repository.getSelectedThemeId()),
            layoutMode = repository.getLayoutMode(),
            isArabic = repository.isArabic(),
            timetableTitle = repository.getTimetableTitle()
        )
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.removeSampleItems()
        }

        viewModelScope.launch {
            repository.getAllItems().collect { items ->
                _uiState.update { current ->
                    val filtered = filterItems(items, current.selectedDay)
                    current.copy(
                        items = items,
                        filteredItems = filtered
                    )
                }
            }
        }
    }

    private fun filterItems(items: List<ScheduleItem>, day: DayOfWeekEnum?): List<ScheduleItem> {
        return if (day == null) {
            items.sortedWith(compareBy({ DayOfWeekEnum.fromString(it.dayOfWeek).order }, { it.startTime }))
        } else {
            items.filter { it.dayOfWeek == day.name }.sortedBy { it.startTime }
        }
    }

    fun selectDay(day: DayOfWeekEnum?) {
        _uiState.update { current ->
            current.copy(
                selectedDay = day,
                filteredItems = filterItems(current.items, day)
            )
        }
    }

    fun setTheme(themeId: String) {
        val theme = TableDesignTheme.findById(themeId)
        _uiState.update { it.copy(currentTheme = theme) }
        repository.setSelectedThemeId(themeId)
    }

    fun setLayoutMode(mode: LayoutMode) {
        _uiState.update { it.copy(layoutMode = mode) }
        repository.setLayoutMode(mode)
    }

    fun toggleLanguage() {
        val next = !_uiState.value.isArabic
        _uiState.update { it.copy(isArabic = next) }
        repository.setArabic(next)
    }

    fun setTimetableTitle(title: String) {
        _uiState.update { it.copy(timetableTitle = title) }
        repository.setTimetableTitle(title)
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun dismissCandidate() {
        _uiState.update { it.copy(parsedCandidate = null) }
    }

    /**
     * Process voice or typed natural language sentence:
     * e.g., "Sunday Calculus 10:00 to 12:00 Group A Hall B"
     * or "الأحد رياضيات من 10 ل 12 مجموعة أ مدرج 1"
     */
    fun processNaturalLanguageInput(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isParsingInput = true) }
            try {
                val parsed = NaturalLanguageScheduleParser.parse(input)
                _uiState.update {
                    it.copy(
                        isParsingInput = false,
                        parsedCandidate = ScheduleItem(
                            dayOfWeek = parsed.dayOfWeek,
                            subject = parsed.subject,
                            startTime = parsed.startTime,
                            endTime = parsed.endTime,
                            groupName = parsed.groupName,
                            place = parsed.place,
                            classType = parsed.classType,
                            colorHex = parsed.colorHex
                        )
                    )
                }
            } catch (e: Exception) {
                val parsed = NaturalLanguageScheduleParser.parseOffline(input)
                _uiState.update {
                    it.copy(
                        isParsingInput = false,
                        parsedCandidate = ScheduleItem(
                            dayOfWeek = parsed.dayOfWeek,
                            subject = parsed.subject,
                            startTime = parsed.startTime,
                            endTime = parsed.endTime,
                            groupName = parsed.groupName,
                            place = parsed.place,
                            classType = parsed.classType,
                            colorHex = parsed.colorHex
                        )
                    )
                }
            }
        }
    }

    fun saveItem(item: ScheduleItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertItem(item)
            } else {
                repository.updateItem(item)
            }
            _uiState.update {
                it.copy(
                    parsedCandidate = null,
                    infoMessage = if (it.isArabic) "تم حفظ المحاضرة وتحديث الويدجت بنجاح" else "Class saved & widget updated!"
                )
            }
        }
    }

    fun deleteItem(item: ScheduleItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _uiState.update {
                it.copy(infoMessage = if (it.isArabic) "تم حذف المحاضرة" else "Class deleted.")
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _uiState.update {
                it.copy(infoMessage = if (it.isArabic) "تم مسح جميع المحاضرات" else "Schedule cleared.")
            }
        }
    }

    /**
     * Request AI suggestions for table design & color coordination
     */
    fun requestAiDesignSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiSuggesting = true) }
            try {
                val items = _uiState.value.items
                val summary = if (items.isEmpty()) {
                    "General University Computer Science Schedule with Lectures and Labs"
                } else {
                    items.joinToString("\n") {
                        "${it.dayOfWeek}: ${it.subject} (${it.classType}) at ${it.startTime}-${it.endTime}, ${it.groupName}, ${it.place}"
                    }
                }

                val suggestion = GeminiApiClient.suggestTableDesign(summary, _uiState.value.isArabic)
                if (suggestion != null) {
                    _uiState.update { it.copy(isAiSuggesting = false, aiSuggestion = suggestion) }
                } else {
                    _uiState.update {
                        it.copy(
                            isAiSuggesting = false,
                            aiSuggestion = AiDesignSuggestion(
                                title = if (it.isArabic) "تصميم مقترح: الطراز الزمردي الأكاديمي" else "Suggested: Modern Emerald Academic",
                                description = if (it.isArabic)
                                    "بناءً على توزيع موادك، نوصي بتصميم الجدول الأسبوعي الشبكي (Week Grid) مع تلوين المعامل بالأخضر والمحاضرات بالأزرق لسهولة الرؤية وتنسيق الويدجت."
                                else
                                    "Based on your course distribution, a Week Matrix layout with emerald accents gives ideal clarity for distinguishing lectures from lab sessions.",
                                recommendedThemeId = TableDesignTheme.EMERALD_ACADEMIC.id,
                                recommendedLayout = LayoutMode.WEEK_MATRIX.name
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiSuggesting = false,
                        aiSuggestion = AiDesignSuggestion(
                            title = if (it.isArabic) "تصميم مقترح: كلاسيكي جامعي" else "Suggested: Classic Campus",
                            description = if (it.isArabic)
                                "نمط أنيق يعتمد على تباين عالي لسرعة قراءة القاعات والمجموعات على الشاشة والويدجت."
                            else
                                "A refined high-contrast palette optimized for quick room and group scanning on your device and widget.",
                            recommendedThemeId = TableDesignTheme.CLASSIC_CAMPUS.id,
                            recommendedLayout = LayoutMode.WEEK_MATRIX.name
                        )
                    )
                }
            }
        }
    }

    fun applyAiSuggestion(suggestion: AiDesignSuggestion) {
        setTheme(suggestion.recommendedThemeId)
        try {
            setLayoutMode(LayoutMode.valueOf(suggestion.recommendedLayout))
        } catch (_: Exception) {}
        _uiState.update {
            it.copy(
                aiSuggestion = null,
                infoMessage = if (it.isArabic) "تم تطبيق التصميم المقترح!" else "AI Design applied successfully!"
            )
        }
    }

    fun dismissAiSuggestion() {
        _uiState.update { it.copy(aiSuggestion = null) }
    }

    fun saveCandidateWithDesign(themeId: String, layout: LayoutMode) {
        val candidate = _uiState.value.parsedCandidate
        if (candidate != null) {
            saveItem(candidate)
        }
        setTheme(themeId)
        setLayoutMode(layout)
        _uiState.update {
            it.copy(
                parsedCandidate = null,
                infoMessage = if (it.isArabic) "تم إنشاء وتنسيق الجدول بنجاح وتحديث الويدجت!" else "Table created, styled & widget updated!"
            )
        }
    }
}
