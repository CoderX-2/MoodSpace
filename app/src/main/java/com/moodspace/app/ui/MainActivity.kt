package com.moodspace.app.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.moodspace.app.R
import com.moodspace.app.data.MoodKind
import com.moodspace.app.util.DateFormats
import com.moodspace.app.util.MoodThemeManager

// HU04 – Registrar estado (pantalla principal); desde aquí se accede a agenda, hábitos, sugerencias y música
class MainActivity : SessionActivity() {

    private val moodViewModel: MoodViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var rootLayout: View
    private lateinit var cardHero: MaterialCardView
    private lateinit var cardWeeklyDominant: MaterialCardView
    private lateinit var tvWeeklyDominantEmoji: TextView
    private lateinit var tvWeeklyDominantText: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvMoodEmojiBadge: TextView
    private lateinit var tvActiveMoodStatus: TextView
    private lateinit var btnSaveMood: Button
    private lateinit var chipMoods: ChipGroup

    private lateinit var iconViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        cardHero = findViewById(R.id.cardHero)
        cardWeeklyDominant = findViewById(R.id.cardWeeklyDominant)
        tvWeeklyDominantEmoji = findViewById(R.id.tvWeeklyDominantEmoji)
        tvWeeklyDominantText = findViewById(R.id.tvWeeklyDominantText)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvMoodEmojiBadge = findViewById(R.id.tvMoodEmojiBadge)
        tvActiveMoodStatus = findViewById(R.id.tvActiveMoodStatus)
        btnSaveMood = findViewById(R.id.btnSaveMood)
        chipMoods = findViewById(R.id.chipMoods)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)

        iconViews = listOf(
            findViewById(R.id.ivIconAgenda),
            findViewById(R.id.ivIconHabits),
            findViewById(R.id.ivIconSuggestions),
            findViewById(R.id.ivIconMusic),
            findViewById(R.id.ivIconHistory),
            findViewById(R.id.ivIconProfile)
        )

        // Un chip por cada estado del arreglo `moods`
        val moods = resources.getStringArray(R.array.moods)
        moods.forEach { label ->
            val chip = layoutInflater.inflate(R.layout.item_mood_chip, chipMoods, false) as Chip
            chip.id = View.generateViewId()
            chip.text = label
            chipMoods.addView(chip)
        }

        // Al cambiar el chip seleccionado, actualiza dinámicamente el color del tema en tiempo real
        chipMoods.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            if (checkedId != null) {
                val selectedChip = group.findViewById<Chip>(checkedId)
                val moodKind = MoodKind.fromLabel(selectedChip.text.toString())
                applyMoodThemeDynamic(moodKind)
            }
        }

        profileViewModel.user.observe(this) { user ->
            tvGreeting.text = getString(R.string.greeting, user?.name ?: "")
        }

        moodViewModel.entries.observe(this) { entries ->
            val weekStart = DateFormats.startOfWeek()
            val weekEnd = weekStart + 7 * 86400000L
            val weekEntries = entries.filter { it.createdAt in weekStart until weekEnd }

            if (weekEntries.isNotEmpty()) {
                val counts = mutableMapOf<MoodKind, Int>()
                weekEntries.forEach { entry ->
                    val kind = MoodKind.fromLabel(entry.mood)
                    if (kind != null) {
                        counts[kind] = (counts[kind] ?: 0) + 1
                    }
                }
                val dominantPair = counts.maxByOrNull { it.value }
                if (dominantPair != null) {
                    val palette = MoodThemeManager.getPalette(dominantPair.key)
                    tvWeeklyDominantEmoji.text = palette.emoji
                    tvWeeklyDominantText.text = "${palette.emoji} ${palette.label} (${dominantPair.value} ${if (dominantPair.value == 1) "registro" else "registros"})"
                    cardWeeklyDominant.strokeColor = palette.primary
                    cardWeeklyDominant.visibility = View.VISIBLE
                } else {
                    cardWeeklyDominant.visibility = View.GONE
                }
            } else {
                cardWeeklyDominant.visibility = View.GONE
            }
        }

        moodViewModel.saved.observe(this) { mood ->
            MoodThemeManager.saveActiveMood(this, mood)
            applyMoodThemeDynamic(MoodKind.fromLabel(mood))
            moodViewModel.loadAll()
            Toast.makeText(this, getString(R.string.mood_saved, mood), Toast.LENGTH_SHORT).show()
        }

        btnSaveMood.setOnClickListener {
            val selected = chipMoods.findViewById<Chip>(chipMoods.checkedChipId)
            if (selected == null) {
                Toast.makeText(this, R.string.error_select_mood, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            moodViewModel.saveMood(selected.text.toString(), etNote.text.toString())
            etNote.text?.clear()
        }

        findViewById<View>(R.id.btnAgenda).setOnClickListener {
            startActivity(Intent(this, AgendaActivity::class.java))
        }
        findViewById<View>(R.id.btnHabits).setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
        findViewById<View>(R.id.btnSuggestions).setOnClickListener {
            startActivity(Intent(this, SuggestionsActivity::class.java))
        }
        findViewById<View>(R.id.btnMusic).setOnClickListener {
            startActivity(Intent(this, MusicActivity::class.java))
        }
        findViewById<View>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<View>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Carga el estado activo guardado e historial
        val currentMood = MoodThemeManager.getActiveMood(this)
        applyMoodThemeDynamic(currentMood)
        moodViewModel.loadAll()
    }

    override fun onResume() {
        super.onResume()
        if (sessionOk) {
            profileViewModel.loadUser()
            val currentMood = MoodThemeManager.getActiveMood(this)
            applyMoodThemeDynamic(currentMood)
            moodViewModel.loadAll()
        }
    }

    private fun applyMoodThemeDynamic(moodKind: MoodKind?) {
        val palette = MoodThemeManager.getPalette(moodKind)
        MoodThemeManager.applyThemeToWindow(window, moodKind)

        rootLayout.setBackgroundColor(palette.primaryContainer)
        cardHero.strokeColor = palette.primary

        if (moodKind != null) {
            tvMoodEmojiBadge.text = palette.emoji
            tvActiveMoodStatus.text = "Estado seleccionado: ${palette.emoji} ${palette.label}"
            tvActiveMoodStatus.visibility = View.VISIBLE
        } else {
            tvMoodEmojiBadge.text = "✨"
            tvActiveMoodStatus.text = "Elige cómo te sientes hoy para adaptar tu espacio"
            tvActiveMoodStatus.visibility = View.VISIBLE
        }

        btnSaveMood.backgroundTintList = ColorStateList.valueOf(palette.primary)

        iconViews.forEach { icon ->
            icon.imageTintList = ColorStateList.valueOf(palette.primary)
        }
    }
}
