package com.topespinf.collabtask.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topespinf.collabtask.model.Task
import com.topespinf.collabtask.model.TaskMilestone
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.ui.theme.ScreenBackground
import com.topespinf.collabtask.viewmodel.TaskViewModel

@Composable
fun TasksScreen(
    onCreateTaskClick: () -> Unit,
    onEditTaskClick: (taskId: String, fromAdminView: Boolean) -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    val currentUser by SessionRepository.currentUser.collectAsState()
    var showCreatedAndAdminTasks by rememberSaveable { mutableStateOf(false) }
    var showCompletedTasksForAdmin by rememberSaveable { mutableStateOf(false) }
    var milestoneErrorMessage by remember { mutableStateOf<String?>(null) }

    val userIdentifier = currentUser?.id.orEmpty()
    val userName = currentUser?.name.orEmpty()
    val isTaskAdmin: (Task) -> Boolean = { task ->
        task.ownerId == userIdentifier || task.participants.any { participant ->
            participant.role.equals("ADMIN", ignoreCase = true) &&
                (participant.userId == userIdentifier || participant.name.equals(userName, ignoreCase = true))
        }
    }
    val subscribedTasks = tasks.filter { task ->
        val isAdminInTask = isTaskAdmin(task)
        val isMemberInTask = task.participants.any { participant ->
            participant.role.equals("MEMBER", ignoreCase = true) &&
                (participant.userId == userIdentifier || participant.name.equals(userName, ignoreCase = true))
        }
        isMemberInTask && !isAdminInTask && !isTaskCompleted(task)
    }
    val createdOrAdminTasks = tasks.filter { task ->
        val isAdminInTask = isTaskAdmin(task)
        val isCompleted = isTaskCompleted(task)
        isAdminInTask && (!isCompleted || showCompletedTasksForAdmin)
    }
    val visibleTasks = if (showCreatedAndAdminTasks) createdOrAdminTasks else subscribedTasks

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Painel de Tarefas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Crie, edite e acompanhe tarefas da equipe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ações Rápidas", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Fluxo de criação e atribuição de tarefas para a equipe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(onClick = onCreateTaskClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Criar Tarefa")
                    }
                    TaskModeToggle(
                        showCreatedAndAdminTasks = showCreatedAndAdminTasks,
                        onModeChange = { showCreatedAndAdminTasks = it }
                    )
                    if (showCreatedAndAdminTasks) {
                        OutlinedButton(
                            onClick = { showCompletedTasksForAdmin = !showCompletedTasksForAdmin },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (showCompletedTasksForAdmin) {
                                    "Ocultar Concluídas"
                                } else {
                                    "Ver Concluídas"
                                }
                            )
                        }
                    }
                }
            }
        }
        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
        }
        if (!errorMessage.isNullOrBlank()) {
            item {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (!milestoneErrorMessage.isNullOrBlank()) {
            item {
                Text(
                    text = milestoneErrorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        items(visibleTasks) { task ->
            TaskCard(
                task = task,
                currentUserId = userIdentifier,
                currentUserName = userName,
                onEditClick = { taskId ->
                    onEditTaskClick(taskId, showCreatedAndAdminTasks)
                },
                onMilestoneComplete = { taskId, milestoneId, onError ->
                    taskViewModel.completeMilestone(taskId, milestoneId) { error ->
                        milestoneErrorMessage = error
                        onError(error)
                    }
                },
                onMilestoneReopen = { taskId, milestoneId, onError ->
                    taskViewModel.reopenMilestone(taskId, milestoneId) { error ->
                        milestoneErrorMessage = error
                        onError(error)
                    }
                },
                onLeaveTask = { taskId ->
                    taskViewModel.leaveTaskById(
                        taskId = taskId,
                        onSuccess = {
                            milestoneErrorMessage = null
                        },
                        onError = { error ->
                            milestoneErrorMessage = error
                        }
                    )
                },
                onReopenCompletedTask = { taskId ->
                    taskViewModel.reopenCompletedTaskById(
                        taskId = taskId,
                        onSuccess = {
                            milestoneErrorMessage = null
                        },
                        onError = { error ->
                            milestoneErrorMessage = error
                        }
                    )
                },
                onSetTaskInProgress = { taskId ->
                    taskViewModel.setTaskInProgressById(
                        taskId = taskId,
                        onSuccess = {
                            milestoneErrorMessage = null
                        },
                        onError = { error ->
                            milestoneErrorMessage = error
                        }
                    )
                }
            )
        }
        if (visibleTasks.isEmpty() && !isLoading) {
            item {
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            if (showCreatedAndAdminTasks && showCompletedTasksForAdmin) {
                                "Nenhuma tarefa concluída encontrada."
                            } else if (showCreatedAndAdminTasks) {
                                "Nenhuma tarefa criada ou administrada ativa encontrada."
                            } else {
                                "Nenhuma tarefa inscrita encontrada."
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    currentUserId: String,
    currentUserName: String,
    onEditClick: (String) -> Unit,
    onMilestoneComplete: (String, String, (String) -> Unit) -> Unit,
    onMilestoneReopen: (String, String, (String) -> Unit) -> Unit,
    onLeaveTask: (String) -> Unit,
    onReopenCompletedTask: (String) -> Unit,
    onSetTaskInProgress: (String) -> Unit
) {
    val isCompletedTask = isTaskCompleted(task)
    val isNotStartedTask = isTaskNotStarted(task)
    val isPausedTask = isTaskPaused(task)
    val isInProgressTask = isTaskInProgress(task)
    val canEditTask = canUserEditTask(task, currentUserId, currentUserName)
    val canCompleteMilestone = task.participants.any {
        it.userId == currentUserId || it.name.equals(currentUserName, ignoreCase = true)
    } || task.ownerId == currentUserId
    val canStartOrContinueTask = canCompleteMilestone && (isNotStartedTask || isPausedTask)
    val canLeaveTask = task.ownerId != currentUserId && task.participants.any {
        it.userId == currentUserId || it.name.equals(currentUserName, ignoreCase = true)
    }
    var showLeaveConfirmation by rememberSaveable(task.id) { mutableStateOf(false) }
    val completedCount = task.milestones.count { it.completed }
    val totalCount = task.milestones.size
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(task.title, fontWeight = FontWeight.SemiBold)
            Text("Criada por: ${task.ownerName.ifBlank { "Sem autor" }}", style = MaterialTheme.typography.bodySmall)
            Text("Status: ${task.status}", style = MaterialTheme.typography.bodySmall)
            if (totalCount > 0) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("$completedCount/$totalCount milestones concluídas", style = MaterialTheme.typography.bodySmall)
            }
            val admins = task.participants.filter { it.role.equals("ADMIN", ignoreCase = true) }
            if (admins.isNotEmpty()) {
                Text(
                    "Admin: ${admins.joinToString { it.name.ifBlank { it.userId } }}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (task.milestones.isNotEmpty() && (isCompletedTask || isInProgressTask)) {
                Spacer(modifier = Modifier.height(4.dp))
                task.milestones.forEach { milestone ->
                    MilestoneRow(
                        milestone = milestone,
                        canComplete = canCompleteMilestone,
                        canReopen = canEditTask,
                        showActionButtons = isInProgressTask,
                        onComplete = {
                            onMilestoneComplete(task.id, milestone.id) { }
                        },
                        onReopen = {
                            onMilestoneReopen(task.id, milestone.id) { }
                        }
                    )
                }
            }
            if (task.comments.isNotEmpty()) {
                Text("Último comentário: ${task.comments.last()}", style = MaterialTheme.typography.bodySmall)
            }
            if (canStartOrContinueTask) {
                OutlinedButton(onClick = { onSetTaskInProgress(task.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isPausedTask) "Continuar Tarefa" else "Iniciar Tarefa")
                }
            }
            if (isCompletedTask && canEditTask) {
                OutlinedButton(onClick = { onReopenCompletedTask(task.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reabrir")
                }
            } else if (canEditTask) {
                OutlinedButton(onClick = { onEditClick(task.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar Tarefa")
                }
            }
            if (!isCompletedTask && canLeaveTask) {
                OutlinedButton(
                    onClick = { showLeaveConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sair da Tarefa")
                }
            }
        }
    }
    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text("Sair da tarefa") },
            text = { Text("Tem certeza que deseja sair desta tarefa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmation = false
                        onLeaveTask(task.id)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun canUserEditTask(task: Task, currentUserId: String, currentUserName: String): Boolean {
    return task.ownerId == currentUserId || task.participants.any { participant ->
        participant.role.equals("ADMIN", ignoreCase = true) &&
            (participant.userId == currentUserId || participant.name.equals(currentUserName, ignoreCase = true))
    }
}

private fun isTaskCompleted(task: Task): Boolean {
    return task.status.equals("Concluida", ignoreCase = true)
}

private fun isTaskInProgress(task: Task): Boolean {
    return task.status.equals("Em progresso", ignoreCase = true)
}

private fun isTaskNotStarted(task: Task): Boolean {
    return task.status.equals("Nao iniciada", ignoreCase = true)
}

private fun isTaskPaused(task: Task): Boolean {
    return task.status.equals("Pausada", ignoreCase = true)
}

@Composable
private fun TaskModeToggle(
    showCreatedAndAdminTasks: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TextButton(
            onClick = { onModeChange(false) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (!showCreatedAndAdminTasks) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (!showCreatedAndAdminTasks) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            Text("Inscritas")
        }
        TextButton(
            onClick = { onModeChange(true) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (showCreatedAndAdminTasks) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (showCreatedAndAdminTasks) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            Text("Criadas/Admin")
        }
    }
}

@Composable
private fun MilestoneRow(
    milestone: TaskMilestone,
    canComplete: Boolean,
    canReopen: Boolean,
    showActionButtons: Boolean,
    onComplete: () -> Unit,
    onReopen: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (milestone.completed) "✓ ${milestone.title}" else milestone.title,
            style = MaterialTheme.typography.bodySmall
        )
        if (!showActionButtons) {
            if (milestone.completed) {
                Text(
                    text = milestone.completedByName?.let { "por $it" } ?: "concluída",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else if (milestone.completed) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = milestone.completedByName?.let { "por $it" } ?: "concluída",
                    style = MaterialTheme.typography.bodySmall
                )
                if (canReopen) {
                    OutlinedButton(onClick = onReopen) {
                        Text("Reabrir")
                    }
                }
            }
        } else {
            OutlinedButton(onClick = onComplete, enabled = canComplete) {
                Text("Concluir")
            }
        }
    }
}
