package com.topespinf.collabtask.repositories

import com.topespinf.collabtask.model.Task

object TaskRepository {
    private val tasks = mutableListOf(
        Task(1, "Criar wireframes do dashboard", "Criar telas principais no Figma", "Ana", "Em progresso", collaborators = mutableListOf("Bruno", "Carlos", "Diana")),
        Task(2, "Revisar backlog da sprint", "Priorizar itens da sprint atual", "Carlos", "Aguardando", collaborators = mutableListOf("Ana", "Eduardo"))
    )

    fun getTasks(): List<Task> = tasks.toList()

    fun createTask(title: String, description: String, assignee: String): Task? {
        if (title.isBlank() || assignee.isBlank()) return null
        val task = Task(
            id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
            title = title.trim(),
            description = description.trim(),
            assignee = assignee.trim(),
            status = "Aguardando"
        )
        tasks.add(task)
        return task
    }

    fun updateFirstTask(status: String, comment: String, collaborators: List<String> = emptyList()): Boolean {
        val firstTask = tasks.firstOrNull() ?: return false
        val updated = firstTask.copy(
            status = status.ifBlank { firstTask.status },
            comments = firstTask.comments.toMutableList().apply {
                if (comment.isNotBlank()) add(comment.trim())
            },
            collaborators = collaborators.toMutableList()
        )
        val index = tasks.indexOfFirst { it.id == firstTask.id }
        tasks[index] = updated
        return true
    }
}

