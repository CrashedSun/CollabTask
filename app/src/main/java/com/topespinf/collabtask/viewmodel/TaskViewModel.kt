package com.topespinf.collabtask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topespinf.collabtask.model.TaskMilestone
import com.topespinf.collabtask.model.Task
import com.topespinf.collabtask.model.TaskParticipant
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.repositories.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            SessionRepository.currentUser.collectLatest { user ->
                if (user == null) {
                    _tasks.value = emptyList()
                    _isLoading.value = false
                    _errorMessage.value = null
                    return@collectLatest
                }

                TaskRepository.observeTasks()
                    .onStart {
                        _isLoading.value = true
                    }
                    .catch { error ->
                        _errorMessage.value = error.message ?: "Não foi possível carregar as tarefas."
                        _isLoading.value = false
                    }
                    .collectLatest { items ->
                        _tasks.value = items
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
            }
        }
    }

    fun getTasks(): List<Task> = _tasks.value

    fun createTask(
        title: String,
        description: String,
        participants: List<TaskParticipant>,
        milestones: List<String>,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.createTask(title, description, participants, milestones)
            }.onSuccess { task ->
                onSuccess(task)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível criar a tarefa.")
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        assignee: String,
        milestones: List<String>,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.createTask(title, description, assignee, milestones)
            }.onSuccess { task ->
                onSuccess(task)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível criar a tarefa.")
            }
        }
    }

    fun createTask(title: String, description: String, participants: List<TaskParticipant>, milestones: List<String>): Boolean {
        return runBlocking(Dispatchers.IO) {
            runCatching { TaskRepository.createTask(title, description, participants, milestones) }.isSuccess
        }
    }

    fun createTask(title: String, description: String, assignee: String, milestones: List<String>): Boolean {
        return runBlocking(Dispatchers.IO) {
            runCatching { TaskRepository.createTask(title, description, assignee, milestones) }.isSuccess
        }
    }

    fun createTask(title: String, description: String, assignee: String): Boolean {
        return createTask(title, description, assignee, listOf(title))
    }

    fun updateFirstTask(
        status: String,
        comment: String,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        val firstTask = _tasks.value.firstOrNull()
        if (firstTask == null) {
            onError("Nenhuma tarefa disponível para edição.")
            return
        }

        viewModelScope.launch {
            runCatching {
                TaskRepository.updateTask(firstTask.id, status, comment)
            }.onSuccess { task ->
                onSuccess(task)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível atualizar a tarefa.")
            }
        }
    }

    fun updateFirstTask(status: String, comment: String): Boolean {
        val firstTask = _tasks.value.firstOrNull() ?: return false
        return runBlocking(Dispatchers.IO) {
            runCatching {
                TaskRepository.updateTask(firstTask.id, status, comment)
            }.isSuccess
        }
    }

    fun updateFirstTask(
        title: String,
        description: String,
        status: String,
        participants: List<TaskParticipant>,
        milestones: List<TaskMilestone>,
        comment: String,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        val firstTask = _tasks.value.firstOrNull()
        if (firstTask == null) {
            onError("Nenhuma tarefa disponível para edição.")
            return
        }
        updateTaskById(
            taskId = firstTask.id,
            title = title,
            description = description,
            status = status,
            participants = participants,
            milestones = milestones,
            comment = comment,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun updateTaskById(
        taskId: String,
        title: String,
        description: String,
        status: String,
        participants: List<TaskParticipant>,
        milestones: List<TaskMilestone>,
        comment: String,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.updateTask(
                    taskId = taskId,
                    title = title,
                    description = description,
                    status = status,
                    participants = participants,
                    milestones = milestones,
                    comment = comment
                )
            }.onSuccess { task ->
                onSuccess(task)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível atualizar a tarefa.")
            }
        }
    }

    fun deleteTaskById(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.deleteTask(taskId)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível excluir a tarefa.")
            }
        }
    }

    fun leaveTaskById(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.leaveTask(taskId)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível sair da tarefa.")
            }
        }
    }

    fun reopenCompletedTaskById(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.reopenCompletedTask(taskId)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível reabrir a tarefa.")
            }
        }
    }

    fun setTaskInProgressById(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.setTaskInProgress(taskId)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível iniciar/continuar a tarefa.")
            }
        }
    }

    fun validateParticipantIdentifier(
        identifier: String,
        onSuccess: (TaskParticipant) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.resolveParticipantIdentifier(identifier)
            }.onSuccess { participant ->
                onSuccess(participant)
            }.onFailure { error ->
                onError(error.message ?: "Integrante inválido.")
            }
        }
    }

    fun completeMilestone(taskId: String, milestoneId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.completeMilestone(taskId, milestoneId)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível concluir a milestone.")
            }
        }
    }

    fun reopenMilestone(taskId: String, milestoneId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                TaskRepository.reopenMilestone(taskId, milestoneId)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível reabrir a milestone.")
            }
        }
    }
}
