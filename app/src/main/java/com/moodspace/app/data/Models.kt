package com.moodspace.app.data

import androidx.annotation.StringRes

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val favoriteActivities: String,
    val remindersEnabled: Boolean
)

data class MoodEntry(val id: Long, val mood: String, val note: String, val createdAt: Long)

data class Credentials(val userId: Long, val hash: String, val salt: String)

/** Prioridades de una tarea. Un número menor significa más importante. */
object TaskPriority {
    const val HIGH = 1
    const val MEDIUM = 2
    const val LOW = 3
}

/** Forma de ordenar la agenda (HU09). Las tareas completadas siempre van al final. */
enum class TaskOrder { PRIORITY, DATE }

/**
 * @param dueDate inicio del día límite (hora local) en milisegundos, o null si no tiene fecha.
 * @param priority uno de [TaskPriority].
 */
data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val dueDate: Long?,
    val priority: Int,
    val done: Boolean,
    val createdAt: Long,
    val completedAt: Long?
)

/** Estados de ánimo conocidos. El orden coincide con el arreglo `moods` de strings.xml. */
enum class MoodKind {
    HAPPY, GOOD, NEUTRAL, SAD, ANGRY;

    companion object {
        /** Convierte la etiqueta guardada (ej. "😄 Feliz") en su [MoodKind], o null si no se reconoce. */
        fun fromLabel(label: String): MoodKind? = when {
            label.contains("Feliz", ignoreCase = true) -> HAPPY
            label.contains("Bien", ignoreCase = true) -> GOOD
            label.contains("Neutral", ignoreCase = true) -> NEUTRAL
            label.contains("Triste", ignoreCase = true) -> SAD
            label.contains("Molesto", ignoreCase = true) -> ANGRY
            else -> null
        }
    }
}

/**
 * Hábito del usuario (HU10).
 * @param doneToday si ya se registró su cumplimiento hoy (HU11).
 * @param streak días consecutivos cumplidos; si hoy aún no se cumple, cuenta hasta ayer.
 */
data class Habit(
    val id: Long,
    val name: String,
    val description: String,
    val createdAt: Long,
    val doneToday: Boolean,
    val streak: Int
)

enum class SuggestionKind { MOOD, TASK, HABIT, MUSIC }

/**
 * Sugerencia para organizar el día (HU12).
 * @param arg valor opcional que se inserta en [title] (ej. el nombre de la tarea).
 */
data class Suggestion(
    val kind: SuggestionKind,
    @StringRes val title: Int,
    @StringRes val detail: Int,
    val arg: String? = null
)

/** Recomendación musical (HU13). [query] es lo que se busca al abrirla. */
data class MusicPick(val title: String, val description: String, val query: String)
