package com.topespinf.collabtask.viewmodel

import androidx.lifecycle.ViewModel
import com.topespinf.collabtask.model.Task
import com.topespinf.collabtask.repositories.TaskRepository

class TaskViewModel : ViewModel() {
    fun getTasks(): List<Task> = TaskRepository.getTasks()

    fun createTask(title: String, description: String, assignee: String): Boolean {
        return TaskRepository.createTask(title, description, assignee) != null
    }

    fun updateFirstTask(status: String, comment: String, collaborators: List<String> = emptyList()): Boolean {
        return TaskRepository.updateFirstTask(status, comment, collaborators)
    }
}

