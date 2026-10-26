# MoodSpace

App Android (Kotlin) para registrar y consultar tu estado de ánimo. Proyecto de Jira: **Proyecto MoodSpace** (PROYEC).

## Sprint 1 – Base (completado)

| Historia | Jira | Puntos | Pantalla / archivo |
|---|---|---|---|
| HU01 – Alta de cuenta | PROYEC-7 | 5 | `RegisterActivity` |
| HU02 – Inicio de sesión | PROYEC-8 | 3 | `LoginActivity`, `SessionManager` |
| HU03 – Editar perfil | PROYEC-9 | 3 | `ProfileActivity` |
| HU04 – Registrar estado | PROYEC-10 | 5 | `MainActivity` |
| HU05 – Consultar historial | PROYEC-11 | 3 | `HistoryActivity` |

Total: 19 puntos.

## Sprint 2 – Agenda y tareas

| Historia | Jira | Puntos* | Pantalla / archivo |
|---|---|---|---|
| HU06 – Crear tarea | PROYEC-12 | 5 | `TaskFormActivity`, `TaskRepository` |
| HU07 – Editar tarea | PROYEC-13 | 3 | `TaskFormActivity` (con `EXTRA_TASK_ID`) |
| HU08 – Completar tarea | PROYEC-14 | 3 | `AgendaActivity`, `TaskAdapter` (casilla) |
| HU09 – Priorizar agenda | PROYEC-15 | 5 | `AgendaActivity` (orden), prioridad Alta/Media/Baja |

Total: 16 puntos. *Puntos propuestos; ajústalos a lo que haya estimado el equipo.

Notas técnicas:
- La base de datos pasó a la **versión 2** y agrega la tabla `tasks`. Quien ya tenga la app instalada conserva sus datos (migración en `MoodDbHelper.onUpgrade`).
- La agenda siempre muestra las pendientes primero y las completadas al final. Se puede ordenar por prioridad o por fecha límite.
- Una tarea sin fecha límite va después de las que sí tienen fecha. Las pendientes con fecha pasada se marcan como vencidas.

## Tecnologías
Kotlin · Android (minSdk 24) · SQLite (`SQLiteOpenHelper`) · Material 3 · Gradle Kotlin DSL

## Cómo correrlo
1. Abre la carpeta en IntelliJ IDEA (con el plugin de Android) y espera el Gradle sync.
2. Elige un emulador o dispositivo y presiona Run.
