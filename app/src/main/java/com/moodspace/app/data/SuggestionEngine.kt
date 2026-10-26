package com.moodspace.app.data

import com.moodspace.app.R

/**
 * HU12 – Reglas para organizar el día según el estado de ánimo, las tareas y los hábitos.
 * Es una función pura (sin Android ni base de datos) para poder probarla fácilmente.
 */
object SuggestionEngine {

    /**
     * @param mood estado de hoy, o null si aún no lo registra.
     * @param tasks tareas del usuario (se ignoran las completadas).
     * @param today inicio del día de hoy, para detectar tareas vencidas.
     */
    fun build(mood: MoodKind?, tasks: List<Task>, habits: List<Habit>, today: Long): List<Suggestion> {
        val result = mutableListOf<Suggestion>()
        val pending = tasks.filter { !it.done }
        val byUrgency = compareBy<Task>({ it.priority }, { it.dueDate ?: Long.MAX_VALUE })

        // 1) Consejo general según el estado
        result.add(moodAdvice(mood))

        // 2) Tareas: con ánimo alto se empieza por lo más importante; con ánimo bajo, por lo más ligero
        val overdue = pending.count { val due = it.dueDate; due != null && due < today }
        if (overdue > 0) {
            result.add(Suggestion(SuggestionKind.TASK, R.string.sug_task_overdue_title, R.string.sug_task_overdue_detail, overdue.toString()))
        }
        if (pending.isEmpty()) {
            result.add(Suggestion(SuggestionKind.TASK, R.string.sug_task_none_title, R.string.sug_task_none_detail))
        } else {
            val lowEnergy = mood == MoodKind.SAD || mood == MoodKind.ANGRY
            if (lowEnergy) {
                val light = pending.sortedWith(byUrgency).maxByOrNull { it.priority }!!
                result.add(Suggestion(SuggestionKind.TASK, R.string.sug_task_light_title, R.string.sug_task_light_detail, light.title))
            } else {
                val top = pending.minWithOrNull(byUrgency)!!
                result.add(Suggestion(SuggestionKind.TASK, R.string.sug_task_top_title, R.string.sug_task_top_detail, top.title))
            }
        }

        // 3) Hábitos
        val pendingHabits = habits.filter { !it.doneToday }
        when {
            habits.isEmpty() ->
                result.add(Suggestion(SuggestionKind.HABIT, R.string.sug_habit_create_title, R.string.sug_habit_create_detail))
            pendingHabits.isEmpty() ->
                result.add(Suggestion(SuggestionKind.HABIT, R.string.sug_habit_done_title, R.string.sug_habit_done_detail))
            else -> {
                // Prefiere el que tenga racha para no perderla
                val next = pendingHabits.maxByOrNull { it.streak }!!
                result.add(Suggestion(SuggestionKind.HABIT, R.string.sug_habit_pending_title, R.string.sug_habit_pending_detail, next.name))
            }
        }

        // 4) Música para acompañar el día
        result.add(Suggestion(SuggestionKind.MUSIC, R.string.sug_music_title, R.string.sug_music_detail))
        return result
    }

    private fun moodAdvice(mood: MoodKind?): Suggestion {
        val (title, detail) = when (mood) {
            MoodKind.HAPPY -> R.string.sug_mood_happy_title to R.string.sug_mood_happy_detail
            MoodKind.GOOD -> R.string.sug_mood_good_title to R.string.sug_mood_good_detail
            MoodKind.NEUTRAL -> R.string.sug_mood_neutral_title to R.string.sug_mood_neutral_detail
            MoodKind.SAD -> R.string.sug_mood_sad_title to R.string.sug_mood_sad_detail
            MoodKind.ANGRY -> R.string.sug_mood_angry_title to R.string.sug_mood_angry_detail
            null -> R.string.sug_mood_none_title to R.string.sug_mood_none_detail
        }
        return Suggestion(SuggestionKind.MOOD, title, detail)
    }
}
