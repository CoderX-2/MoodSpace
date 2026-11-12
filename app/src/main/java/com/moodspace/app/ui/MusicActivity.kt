package com.moodspace.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.moodspace.app.R
import com.moodspace.app.data.MoodKind
import com.moodspace.app.data.MusicCatalog
import com.moodspace.app.data.MusicPick

// HU13 – Consultar música según el estado de ánimo
class MusicActivity : SessionActivity() {

    private val moodViewModel: MoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_music)
        title = getString(R.string.title_music)

        val spMood = findViewById<Spinner>(R.id.spMusicMood)
        val list = findViewById<ListView>(R.id.lvMusic)
        val adapter = MusicAdapter(this)
        list.adapter = adapter

        // El orden del arreglo `moods` coincide con el de MoodKind
        spMood.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.moods)
        )
        spMood.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapter.clear()
                adapter.addAll(MusicCatalog.forMood(MoodKind.values()[position]))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // Por defecto se muestra la música del estado de hoy; al girar la pantalla el spinner se restaura solo
        val restoring = savedInstanceState != null
        if (!restoring) {
            val requested = intent.getIntExtra(EXTRA_MOOD, -1)
            if (requested in MoodKind.values().indices) {
                spMood.setSelection(requested)
            } else {
                spMood.setSelection(MoodKind.NEUTRAL.ordinal) // si hoy no ha registrado estado
                moodViewModel.todayMood.observe(this) { mood ->
                    if (mood != null) spMood.setSelection(mood.ordinal)
                }
                moodViewModel.loadTodayMood()
            }
        }

        list.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position)?.let { open(it) }
        }
    }

    /** Abre la búsqueda en el navegador o en la app de música instalada. */
    private fun open(pick: MusicPick) {
        val uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(pick.query))
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_music_open, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /** Posición (ordinal) de [MoodKind] que se quiere mostrar; opcional. */
        const val EXTRA_MOOD = "mood_kind"
    }
}

private class MusicAdapter(context: Context) : ArrayAdapter<MusicPick>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        val pick = getItem(position) ?: return view
        view.findViewById<TextView>(android.R.id.text1).text = "🎵 " + pick.title
        view.findViewById<TextView>(android.R.id.text2).text = pick.description
        return view
    }
}
