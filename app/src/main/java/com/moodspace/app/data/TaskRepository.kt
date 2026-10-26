package com.moodspace.app.data

class TaskRepository(private val db: MoodDbHelper) {

    /** Devuelve el id de la nueva tarea, o -1 si no se pudo guardar. */
    fun add(userId: Long, title: String, description: String, dueDate: Long?, priority: Int): Long =
        db.insertTask(userId, title.trim(), description.trim(), dueDate, priority)

    fun update(
        id: Long, userId: Long, title: String, description: String, dueDate: Long?, priority: Int
    ): Boolean = db.updateTask(id, userId, title.trim(), description.trim(), dueDate, priority)

    fun setDone(id: Long, userId: Long, done: Boolean): Boolean = db.setTaskDone(id, userId, done)

    fun get(id: Long, userId: Long): Task? = db.getTask(id, userId)

    fun agenda(userId: Long, order: TaskOrder): List<Task> = db.getTasks(userId, order)
}
