package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class LayoutMode(val englishName: String, val arabicName: String) {
    WEEK_MATRIX("Week Grid Matrix", "جدول أسبوعي شبكي"),
    DAY_CARDS("Daily Cards", "بطاقات يومية متسلسلة"),
    COMPACT_LIST("Compact List", "قائمة مضغوطة")
}

data class TableDesignTheme(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val primaryHeaderHex: String,
    val secondaryHeaderHex: String,
    val backgroundHex: String,
    val cardBackgroundHex: String,
    val cardBorderHex: String,
    val textPrimaryHex: String,
    val textSecondaryHex: String,
    val accentHex: String,
    val previewPalette: List<String>
) {
    companion object {
        val EMERALD_ACADEMIC = TableDesignTheme(
            id = "EMERALD",
            nameEn = "Emerald Academic",
            nameAr = "أكاديمي زمردي",
            descriptionEn = "Fresh teal & emerald tones with crisp modern borders. Clean and focused for study.",
            descriptionAr = "درجات الزمرد النضرة والتركواز مع حدود عصرية واضحة ومريحة للعين.",
            primaryHeaderHex = "#065F46",
            secondaryHeaderHex = "#0D9488",
            backgroundHex = "#F0FDF4",
            cardBackgroundHex = "#FFFFFF",
            cardBorderHex = "#A7F3D0",
            textPrimaryHex = "#064E3B",
            textSecondaryHex = "#047857",
            accentHex = "#10B981",
            previewPalette = listOf("#065F46", "#10B981", "#34D399", "#A7F3D0")
        )

        val ROYAL_INDIGO = TableDesignTheme(
            id = "ROYAL_INDIGO",
            nameEn = "Royal Indigo",
            nameAr = "ملكي نيلي فاخر",
            descriptionEn = "Deep navy and royal violet collegiate look. Prestigious and elegant.",
            descriptionAr = "أزرق ملكي ونيلي مستوحى من أعرق الجامعات بتصميم فخم ومرتب.",
            primaryHeaderHex = "#1E1B4B",
            secondaryHeaderHex = "#3730A3",
            backgroundHex = "#F5F3FF",
            cardBackgroundHex = "#FFFFFF",
            cardBorderHex = "#DDD6FE",
            textPrimaryHex = "#1E1B4B",
            textSecondaryHex = "#4338CA",
            accentHex = "#6366F1",
            previewPalette = listOf("#1E1B4B", "#4338CA", "#6366F1", "#A5B4FC")
        )

        val SUNSET_WARMTH = TableDesignTheme(
            id = "SUNSET_WARMTH",
            nameEn = "Sunset Coral",
            nameAr = "غروب دافئ",
            descriptionEn = "Vibrant coral, amber, and terracotta. Energetic and easy to scan.",
            descriptionAr = "تدرجات مرجانية وعنبرية مشرقة تمنح النشاط وسهولة تمييز المواد.",
            primaryHeaderHex = "#9A3412",
            secondaryHeaderHex = "#C2410C",
            backgroundHex = "#FFF7ED",
            cardBackgroundHex = "#FFFFFF",
            cardBorderHex = "#FED7AA",
            textPrimaryHex = "#7C2D12",
            textSecondaryHex = "#C2410C",
            accentHex = "#F97316",
            previewPalette = listOf("#9A3412", "#EA580C", "#FB923C", "#FDBA74")
        )

        val CYBER_DARK = TableDesignTheme(
            id = "CYBER_DARK",
            nameEn = "Midnight Cyber",
            nameAr = "سايبر ليلي داكن",
            descriptionEn = "Sleek dark mode with electric cyan, purple, and high-contrast badges.",
            descriptionAr = "وضع داكن فائق الحداثة مع إضاءات نيون تركوازية وبنفسجية واضحة.",
            primaryHeaderHex = "#0F172A",
            secondaryHeaderHex = "#1E293B",
            backgroundHex = "#0B0F19",
            cardBackgroundHex = "#161E2E",
            cardBorderHex = "#334155",
            textPrimaryHex = "#F8FAFC",
            textSecondaryHex = "#94A3B8",
            accentHex = "#38BDF8",
            previewPalette = listOf("#0F172A", "#38BDF8", "#818CF8", "#F472B6")
        )

        val NORDIC_PASTEL = TableDesignTheme(
            id = "NORDIC_PASTEL",
            nameEn = "Nordic Pastel",
            nameAr = "باستيل هادئ",
            descriptionEn = "Soft calming sage, lavender, and butter tones. Minimalist and gentle.",
            descriptionAr = "ألوان هادئة مستوحاة من الطراز الاسكندنافي تناسب القراءة الطويلة.",
            primaryHeaderHex = "#334155",
            secondaryHeaderHex = "#475569",
            backgroundHex = "#F8FAFC",
            cardBackgroundHex = "#FFFFFF",
            cardBorderHex = "#E2E8F0",
            textPrimaryHex = "#1E293B",
            textSecondaryHex = "#64748B",
            accentHex = "#8B5CF6",
            previewPalette = listOf("#475569", "#8B5CF6", "#A78BFA", "#C4B5FD")
        )

        val CLASSIC_CAMPUS = TableDesignTheme(
            id = "CLASSIC_CAMPUS",
            nameEn = "Classic Campus",
            nameAr = "كلاسيكي جامعي",
            descriptionEn = "Oxford navy and warm gold heraldic accents. Traditional Ivy League feel.",
            descriptionAr = "الأزرق الكحلي مع اللمسات الذهبية الملكية للجامعات العريقة.",
            primaryHeaderHex = "#1E293B",
            secondaryHeaderHex = "#334155",
            backgroundHex = "#FEFDF8",
            cardBackgroundHex = "#FFFFFF",
            cardBorderHex = "#E2D9C8",
            textPrimaryHex = "#1E293B",
            textSecondaryHex = "#854D0E",
            accentHex = "#D97706",
            previewPalette = listOf("#1E293B", "#D97706", "#F59E0B", "#FCD34D")
        )

        val allThemes = listOf(
            EMERALD_ACADEMIC,
            ROYAL_INDIGO,
            SUNSET_WARMTH,
            CYBER_DARK,
            NORDIC_PASTEL,
            CLASSIC_CAMPUS
        )

        fun findById(id: String): TableDesignTheme {
            return allThemes.find { it.id == id } ?: EMERALD_ACADEMIC
        }
    }
}
