package com.moodspace.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Window
import androidx.annotation.ColorInt
import androidx.core.view.WindowInsetsControllerCompat
import com.moodspace.app.data.MoodKind

data class MoodColorPalette(
    @ColorInt val primary: Int,
    @ColorInt val primaryContainer: Int,
    @ColorInt val onPrimary: Int,
    @ColorInt val onContainerText: Int,
    @ColorInt val statusBarColor: Int,
    @ColorInt val gradientStart: Int,
    @ColorInt val gradientEnd: Int,
    val emoji: String,
    val label: String
)

object MoodThemeManager {
    private const val PREFS_NAME = "moodspace_theme"
    private const val KEY_ACTIVE_MOOD = "active_mood_label"

    fun saveActiveMood(context: Context, moodLabel: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_MOOD, moodLabel).apply()
    }

    fun getActiveMood(context: Context): MoodKind? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val label = prefs.getString(KEY_ACTIVE_MOOD, null) ?: return null
        return MoodKind.fromLabel(label)
    }

    fun getPalette(mood: MoodKind?): MoodColorPalette = when (mood) {
        MoodKind.HAPPY -> MoodColorPalette(
            primary = Color.parseColor("#FF8F00"),
            primaryContainer = Color.parseColor("#FFF8E1"),
            onPrimary = Color.WHITE,
            onContainerText = Color.parseColor("#5D4037"),
            statusBarColor = Color.parseColor("#FF6F00"),
            gradientStart = Color.parseColor("#FFC107"),
            gradientEnd = Color.parseColor("#FF8F00"),
            emoji = "😄",
            label = "Feliz"
        )
        MoodKind.GOOD -> MoodColorPalette(
            primary = Color.parseColor("#2E7D32"),
            primaryContainer = Color.parseColor("#E8F5E9"),
            onPrimary = Color.WHITE,
            onContainerText = Color.parseColor("#1B5E20"),
            statusBarColor = Color.parseColor("#1B5E20"),
            gradientStart = Color.parseColor("#66BB6A"),
            gradientEnd = Color.parseColor("#2E7D32"),
            emoji = "🙂",
            label = "Bien"
        )
        MoodKind.NEUTRAL -> MoodColorPalette(
            primary = Color.parseColor("#3F51B5"),
            primaryContainer = Color.parseColor("#E8EAF6"),
            onPrimary = Color.WHITE,
            onContainerText = Color.parseColor("#1A237E"),
            statusBarColor = Color.parseColor("#303F9F"),
            gradientStart = Color.parseColor("#7986CB"),
            gradientEnd = Color.parseColor("#3F51B5"),
            emoji = "😐",
            label = "Neutral"
        )
        MoodKind.SAD -> MoodColorPalette(
            primary = Color.parseColor("#0288D1"),
            primaryContainer = Color.parseColor("#E1F5FE"),
            onPrimary = Color.WHITE,
            onContainerText = Color.parseColor("#01579B"),
            statusBarColor = Color.parseColor("#0277BD"),
            gradientStart = Color.parseColor("#29B6F6"),
            gradientEnd = Color.parseColor("#0288D1"),
            emoji = "😔",
            label = "Triste"
        )
        MoodKind.ANGRY -> MoodColorPalette(
            primary = Color.parseColor("#D32F2F"),
            primaryContainer = Color.parseColor("#FFEBEE"),
            onPrimary = Color.WHITE,
            onContainerText = Color.parseColor("#B71C1C"),
            statusBarColor = Color.parseColor("#C62828"),
            gradientStart = Color.parseColor("#EF5350"),
            gradientEnd = Color.parseColor("#D32F2F"),
            emoji = "😡",
            label = "Molesto"
        )
        null -> MoodColorPalette(
            primary = Color.parseColor("#6750A4"),
            primaryContainer = Color.parseColor("#F8F5FF"),
            onPrimary = Color.WHITE,
            onContainerText = Color.parseColor("#1D1B20"),
            statusBarColor = Color.parseColor("#4F378B"),
            gradientStart = Color.parseColor("#7F67BE"),
            gradientEnd = Color.parseColor("#6750A4"),
            emoji = "✨",
            label = "MoodSpace"
        )
    }

    fun applyThemeToWindow(window: Window, mood: MoodKind?) {
        val palette = getPalette(mood)
        window.statusBarColor = palette.statusBarColor
        window.navigationBarColor = palette.primaryContainer
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = true
    }

    fun createGradientDrawable(startColor: Int, endColor: Int, radiusPx: Float = 0f): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = radiusPx
        }
    }
}
