package com.moodspace.app.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import com.moodspace.app.R
import com.moodspace.app.data.MoodEntry
import com.moodspace.app.data.MoodKind
import com.moodspace.app.util.DateFormats
import com.moodspace.app.util.MoodThemeManager
import java.util.Calendar

// HU05 – Consultar historial semanal de estados de ánimo
class HistoryActivity : SessionActivity() {

    private val viewModel: MoodViewModel by viewModels()

    private var currentWeekStart: Long = DateFormats.startOfWeek()
    private var allEntries: List<MoodEntry> = emptyList()

    private lateinit var tvWeekRange: TextView
    private lateinit var btnPrevWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private lateinit var layoutWeekDaysBar: LinearLayout
    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_history)
        title = getString(R.string.title_history)

        tvWeekRange = findViewById(R.id.tvWeekRange)
        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)
        layoutWeekDaysBar = findViewById(R.id.layoutWeekDaysBar)
        listView = findViewById(R.id.lvHistory)
        tvEmpty = findViewById(R.id.tvEmpty)

        btnPrevWeek.setOnClickListener {
            currentWeekStart -= 7 * 86400000L
            updateWeekUI()
        }

        btnNextWeek.setOnClickListener {
            if (currentWeekStart + 7 * 86400000L <= DateFormats.startOfWeek()) {
                currentWeekStart += 7 * 86400000L
                updateWeekUI()
            }
        }

        viewModel.entries.observe(this) { entries ->
            allEntries = entries
            updateWeekUI()
        }

        viewModel.loadAll()
    }

    private fun updateWeekUI() {
        val weekEnd = currentWeekStart + 7 * 86400000L
        tvWeekRange.text = DateFormats.weekRangeText(currentWeekStart)

        val isCurrentWeek = currentWeekStart >= DateFormats.startOfWeek()
        btnNextWeek.alpha = if (isCurrentWeek) 0.3f else 1.0f
        btnNextWeek.isEnabled = !isCurrentWeek

        val filteredEntries = allEntries.filter { it.createdAt in currentWeekStart until weekEnd }

        renderWeekDaysBar(filteredEntries)

        if (filteredEntries.isEmpty()) {
            listView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            listView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            listView.adapter = WeeklyHistoryAdapter(filteredEntries)
            listView.setOnItemClickListener { _, _, position, _ ->
                val intent = Intent(this, MoodDetailActivity::class.java)
                intent.putExtra(MoodDetailActivity.EXTRA_ENTRY_ID, filteredEntries[position].id)
                startActivity(intent)
            }
        }
    }

    private fun renderWeekDaysBar(weekEntries: List<MoodEntry>) {
        layoutWeekDaysBar.removeAllViews()
        val dayNames = arrayOf("L", "M", "M", "J", "V", "S", "D")

        val cal = Calendar.getInstance().apply { timeInMillis = currentWeekStart }

        for (i in 0 until 7) {
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + 86400000L
            val dayNumber = cal.get(Calendar.DAY_OF_MONTH)

            val entryForDay = weekEntries.find { it.createdAt in dayStart until dayEnd }
            val moodKind = entryForDay?.let { MoodKind.fromLabel(it.mood) }
            val palette = MoodThemeManager.getPalette(moodKind)

            val dayView = LayoutInflater.from(this).inflate(R.layout.item_history_day_bar, layoutWeekDaysBar, false)

            val tvDayName = dayView.findViewById<TextView>(R.id.tvDayName)
            val tvDayNum = dayView.findViewById<TextView>(R.id.tvDayNum)
            val vMoodDot = dayView.findViewById<View>(R.id.vMoodDot)

            tvDayName.text = dayNames[i]
            tvDayNum.text = dayNumber.toString()

            if (entryForDay != null) {
                vMoodDot.visibility = View.VISIBLE
                vMoodDot.backgroundTintList = ColorStateList.valueOf(palette.primary)
            } else {
                vMoodDot.visibility = View.INVISIBLE
            }

            layoutWeekDaysBar.addView(dayView)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private inner class WeeklyHistoryAdapter(private val items: List<MoodEntry>) : BaseAdapter() {
        override fun getCount(): Int = items.size
        override fun getItem(position: Int): MoodEntry = items[position]
        override fun getItemId(position: Int): Long = items[position].id

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@HistoryActivity)
                .inflate(R.layout.item_history_entry, parent, false)

            val entry = getItem(position)
            val moodKind = MoodKind.fromLabel(entry.mood)
            val palette = MoodThemeManager.getPalette(moodKind)

            val tvEmoji = view.findViewById<TextView>(R.id.tvHistoryEmoji)
            val tvMood = view.findViewById<TextView>(R.id.tvHistoryMood)
            val tvDate = view.findViewById<TextView>(R.id.tvHistoryDate)
            val tvNote = view.findViewById<TextView>(R.id.tvHistoryNote)

            tvEmoji.text = palette.emoji
            tvEmoji.setBackgroundColor(palette.primaryContainer)
            tvMood.text = entry.mood
            tvDate.text = DateFormats.dayNameAndDate(entry.createdAt) + " · " + DateFormats.timeOnly(entry.createdAt)

            if (entry.note.isNotBlank()) {
                tvNote.text = "“${entry.note}”"
                tvNote.visibility = View.VISIBLE
            } else {
                tvNote.visibility = View.GONE
            }

            return view
        }
    }
}
