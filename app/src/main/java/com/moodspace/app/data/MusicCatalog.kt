package com.moodspace.app.data

/** HU13 – Catálogo de música sugerida para cada estado de ánimo. */
object MusicCatalog {

    fun forMood(mood: MoodKind): List<MusicPick> = when (mood) {
        MoodKind.HAPPY -> listOf(
            MusicPick("Pop para celebrar", "Ritmos alegres para mantener la energía", "pop alegre playlist para bailar"),
            MusicPick("Éxitos para cantar", "Canciones que se corean fácil", "éxitos en español para cantar feliz"),
            MusicPick("Verano y buen humor", "Sonidos soleados para el día", "summer vibes playlist feel good")
        )
        MoodKind.GOOD -> listOf(
            MusicPick("Indie luminoso", "Suave y positivo para acompañar el día", "indie pop positivo playlist"),
            MusicPick("Música para concentrarte", "Ritmo medio para avanzar con tus tareas", "música para concentrarse y trabajar"),
            MusicPick("Acústico relajado", "Guitarras y voces cálidas", "acoustic chill playlist")
        )
        MoodKind.NEUTRAL -> listOf(
            MusicPick("Lo-fi para el día", "Fondo tranquilo, ideal para estudiar o trabajar", "lofi hip hop playlist"),
            MusicPick("Jazz suave", "Ambiente cálido sin distraerte", "jazz suave relajante playlist"),
            MusicPick("Descubre algo nuevo", "Mezcla variada para cambiar el ánimo", "mix musical variado descubrimientos")
        )
        MoodKind.SAD -> listOf(
            MusicPick("Reconfortante", "Canciones suaves que acompañan", "música reconfortante para días tristes"),
            MusicPick("Piano tranquilo", "Instrumental para respirar y descansar", "piano relajante instrumental"),
            MusicPick("Para animarte poco a poco", "Subiendo el ánimo de a poco", "canciones para levantar el ánimo")
        )
        MoodKind.ANGRY -> listOf(
            MusicPick("Respira y baja el ritmo", "Sonidos calmados para soltar tensión", "música relajante para calmar el enojo"),
            MusicPick("Sonidos de naturaleza", "Lluvia, bosque y agua", "sonidos de naturaleza relajantes"),
            MusicPick("Desahógate con rock", "Si necesitas sacarlo, con energía", "rock energético para desahogarse")
        )
    }
}
