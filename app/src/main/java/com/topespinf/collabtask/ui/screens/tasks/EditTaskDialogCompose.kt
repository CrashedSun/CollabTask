package com.topespinf.collabtask.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topespinf.collabtask.model.TaskMilestone
import com.topespinf.collabtask.model.TaskParticipant
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.viewmodel.TaskViewModel

@Composable
fun EditTaskDialogCompose(
    taskId: String,
    canManageAdmins: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val currentUser by SessionRepository.currentUser.collectAsState()
    val task = tasks.firstOrNull { it.id == taskId }
    val canEditTask = task?.let { currentTask ->
        val currentUserId = currentUser?.id.orEmpty()
        val currentUserName = currentUser?.name.orEmpty()
        currentTask.ownerId == currentUserId || currentTask.participants.any { participant ->
            participant.role.equals("ADMIN", ignoreCase = true) &&
                (participant.userId == currentUserId || participant.name.equals(currentUserName, ignoreCase = true))
        }
    } ?: false

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Nao iniciada") }
    var comment by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf<List<TaskParticipant>>(emptyList()) }
    var milestones by remember { mutableStateOf<List<TaskMilestone>>(emptyList()) }
    var newParticipantName by remember { mutableStateOf("") }
    var newMilestoneTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var participantPendingRemovalIndex by remember { mutableStateOf<Int?>(null) }
    var milestonePendingRemovalIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteTaskConfirmation by remember { mutableStateOf(false) }
    var isAddingParticipant by remember { mutableStateOf(false) }

    val statusOptions = listOf("Nao iniciada", "Em progresso", "Concluida", "Pausada")

    LaunchedEffect(task?.id) {
        if (task != null) {
            title = task.title
            description = task.description
            status = task.status
            participants = task.participants
            milestones = task.milestones
            comment = ""
            newParticipantName = ""
            newMilestoneTitle = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarefa") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!canEditTask) {
                    Text("Somente admins da tarefa podem acessar a edição.")
                    return@Column
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Box {
                    OutlinedButton(
                        onClick = { statusExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Status: $status")
                    }
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    if (canEditTask) {
                        "Integrantes (marque para ADMIN):"
                    } else {
                        "Integrantes:"
                    }
                )
                if (participants.isEmpty()) {
                    Text("Nenhum integrante disponível.")
                } else {
                    participants.forEachIndexed { index, participant ->
                        val isAdmin = participant.role.equals("ADMIN", ignoreCase = true)
                        val isOwnerParticipant = task?.ownerId?.isNotBlank() == true &&
                            participant.userId == task.ownerId
                        val canToggleAdmin = canEditTask && !isOwnerParticipant
                        val canRemoveParticipant = canEditTask && !isOwnerParticipant
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canToggleAdmin) {
                                    if (!canToggleAdmin) return@clickable
                                    participants = participants.toMutableList().also { list ->
                                        list[index] = participant.copy(role = if (isAdmin) "MEMBER" else "ADMIN")
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAdmin,
                                enabled = canToggleAdmin,
                                onCheckedChange = {
                                    if (canToggleAdmin) {
                                        participants = participants.toMutableList().also { list ->
                                            list[index] = participant.copy(role = if (isAdmin) "MEMBER" else "ADMIN")
                                        }
                                    }
                                }
                            )
                            Text(
                                buildString {
                                    append(participant.name.ifBlank { participant.userId })
                                    append(" (")
                                    append(participant.role)
                                    append(")")
                                    if (isOwnerParticipant) {
                                        append(" - criador")
                                    }
                                }
                            )
                            if (canRemoveParticipant) {
                                TextButton(
                                    onClick = {
                                        participantPendingRemovalIndex = index
                                    }
                                ) {
                                    Text("Remover")
                                }
                            }
                        }
                    }
                }
                if (canEditTask) {
                    OutlinedTextField(
                        value = newParticipantName,
                        onValueChange = { newParticipantName = it },
                        label = { Text("Novo integrante (user ou email)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = {
                            val cleanedName = newParticipantName.trim()
                            if (cleanedName.isBlank()) {
                                return@TextButton
                            }
                            if (isAddingParticipant) {
                                return@TextButton
                            }
                            isAddingParticipant = true
                            taskViewModel.validateParticipantIdentifier(
                                identifier = cleanedName,
                                onSuccess = { validatedParticipant ->
                                    isAddingParticipant = false
                                    val alreadyExists = participants.any {
                                        it.userId.isNotBlank() && it.userId == validatedParticipant.userId
                                    }
                                    if (alreadyExists) {
                                        errorMessage = "Integrante já adicionado."
                                        return@validateParticipantIdentifier
                                    }
                                    participants = participants + validatedParticipant
                                    newParticipantName = ""
                                    errorMessage = null
                                },
                                onError = {
                                    isAddingParticipant = false
                                    errorMessage = it
                                }
                            )
                        }
                    ) {
                        Text(if (isAddingParticipant) "Validando..." else "Adicionar integrante")
                    }
                }

                Text("Milestones:")
                if (milestones.isEmpty()) {
                    Text("Nenhuma milestone disponível.")
                } else {
                    milestones.forEachIndexed { index, milestone ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = milestone.title,
                                modifier = Modifier.weight(1f)
                            )
                            if (canEditTask) {
                                TextButton(
                                    onClick = {
                                        milestonePendingRemovalIndex = index
                                    }
                                ) {
                                    Text("Remover")
                                }
                            }
                        }
                    }
                }
                if (canEditTask) {
                    OutlinedTextField(
                        value = newMilestoneTitle,
                        onValueChange = { newMilestoneTitle = it },
                        label = { Text("Nova milestone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = {
                            val cleanedTitle = newMilestoneTitle.trim()
                            if (cleanedTitle.isBlank()) {
                                return@TextButton
                            }
                            milestones = milestones + TaskMilestone(
                                id = "",
                                title = cleanedTitle
                            )
                            newMilestoneTitle = ""
                        }
                    ) {
                        Text("Adicionar milestone")
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentário") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
                if (canEditTask) {
                    TextButton(
                        onClick = {
                            showDeleteTaskConfirmation = true
                        }
                    ) {
                        Text("Apagar tarefa")
                    }
                }
            }
        },
        confirmButton = {
            if (canEditTask) {
                TextButton(
                    onClick = {
                        val currentTask = task ?: return@TextButton
                        if (title.isBlank() || description.isBlank()) {
                            errorMessage = "Título e descrição são obrigatórios."
                            return@TextButton
                        }
                        if (participants.isEmpty()) {
                            errorMessage = "A tarefa precisa de ao menos um integrante."
                            return@TextButton
                        }
                        if (milestones.isEmpty()) {
                            errorMessage = "A tarefa precisa de ao menos uma milestone."
                            return@TextButton
                        }
                        taskViewModel.updateTaskById(
                            taskId = currentTask.id,
                            title = title,
                            description = description,
                            status = status,
                            participants = participants,
                            milestones = milestones,
                            comment = comment,
                            onSuccess = {
                                onSave()
                            },
                            onError = { errorMessage = it }
                        )
                    }
                ) {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    val participantToRemove = participantPendingRemovalIndex?.let { index ->
        participants.getOrNull(index)
    }
    if (participantPendingRemovalIndex != null && participantToRemove != null) {
        AlertDialog(
            onDismissRequest = { participantPendingRemovalIndex = null },
            title = { Text("Remover integrante") },
            text = {
                Text("Deseja remover ${participantToRemove.name.ifBlank { "este integrante" }} da tarefa?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = participantPendingRemovalIndex ?: return@TextButton
                        participants = participants.filterIndexed { itemIndex, _ -> itemIndex != index }
                        participantPendingRemovalIndex = null
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { participantPendingRemovalIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val milestoneToRemove = milestonePendingRemovalIndex?.let { index ->
        milestones.getOrNull(index)
    }
    if (milestonePendingRemovalIndex != null && milestoneToRemove != null) {
        AlertDialog(
            onDismissRequest = { milestonePendingRemovalIndex = null },
            title = { Text("Remover milestone") },
            text = {
                Text("Deseja remover a milestone \"${milestoneToRemove.title}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = milestonePendingRemovalIndex ?: return@TextButton
                        milestones = milestones.filterIndexed { itemIndex, _ -> itemIndex != index }
                        milestonePendingRemovalIndex = null
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { milestonePendingRemovalIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteTaskConfirmation && canEditTask) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteTaskConfirmation = false },
            title = { Text("Apagar tarefa") },
            text = { Text("Tem certeza que deseja apagar esta tarefa? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        val currentTask = task ?: return@TextButton
                        isDeleting = true
                        taskViewModel.deleteTaskById(
                            taskId = currentTask.id,
                            onSuccess = {
                                isDeleting = false
                                showDeleteTaskConfirmation = false
                                onSave()
                                onDismiss()
                            },
                            onError = {
                                isDeleting = false
                                showDeleteTaskConfirmation = false
                                errorMessage = it
                            }
                        )
                    }
                ) {
                    Text("Apagar")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = { showDeleteTaskConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
