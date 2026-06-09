package com.topespinf.collabtask.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topespinf.collabtask.model.TaskParticipant
import com.topespinf.collabtask.viewmodel.TaskViewModel

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var newParticipantIdentifier by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf<List<TaskParticipant>>(emptyList()) }
    var newMilestoneTitle by remember { mutableStateOf("") }
    var milestones by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAddingParticipant by remember { mutableStateOf(false) }
    var participantPendingRemovalIndex by remember { mutableStateOf<Int?>(null) }
    var milestonePendingRemovalIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Nova Tarefa") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Crie e atribua tarefas para sua equipe.",
                    modifier = Modifier.fillMaxWidth()
                )

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

                OutlinedTextField(
                    value = newParticipantIdentifier,
                    onValueChange = { newParticipantIdentifier = it },
                    label = { Text("Novo integrante (user ou email)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = {
                        val cleanedIdentifier = newParticipantIdentifier.trim()
                        if (cleanedIdentifier.isBlank() || isAddingParticipant) {
                            return@TextButton
                        }
                        isAddingParticipant = true
                        taskViewModel.validateParticipantIdentifier(
                            identifier = cleanedIdentifier,
                            onSuccess = { validatedParticipant ->
                                isAddingParticipant = false
                                val alreadyExists = participants.any { it.userId == validatedParticipant.userId }
                                if (alreadyExists) {
                                    errorMessage = "Integrante já adicionado."
                                    return@validateParticipantIdentifier
                                }
                                participants = participants + validatedParticipant
                                newParticipantIdentifier = ""
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

                Text("Integrantes adicionados:")
                if (participants.isEmpty()) {
                    Text("Nenhum integrante adicionado.")
                } else {
                    participants.forEachIndexed { index, participant ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(participant.name)
                            TextButton(onClick = { participantPendingRemovalIndex = index }) {
                                Text("Remover")
                            }
                        }
                    }
                }

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
                        val alreadyExists = milestones.any { it.equals(cleanedTitle, ignoreCase = true) }
                        if (alreadyExists) {
                            errorMessage = "Milestone já adicionada."
                            return@TextButton
                        }
                        milestones = milestones + cleanedTitle
                        newMilestoneTitle = ""
                        errorMessage = null
                    }
                ) {
                    Text("Adicionar milestone")
                }
                Text("Milestones adicionadas:")
                if (milestones.isEmpty()) {
                    Text("Nenhuma milestone adicionada.")
                } else {
                    milestones.forEachIndexed { index, milestone ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(milestone)
                            TextButton(onClick = { milestonePendingRemovalIndex = index }) {
                                Text("Remover")
                            }
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        errorMessage = "Título e descrição são obrigatórios."
                        return@TextButton
                    }
                    if (participants.isEmpty()) {
                        errorMessage = "Adicione ao menos um integrante válido."
                        return@TextButton
                    }
                    if (milestones.isEmpty()) {
                        errorMessage = "Adicione ao menos uma milestone."
                        return@TextButton
                    }

                    taskViewModel.createTask(
                        title = title,
                        description = description,
                        participants = participants,
                        milestones = milestones,
                        onSuccess = {
                            onSave()
                        },
                        onError = { errorMessage = it }
                    )
                }
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    val participantToRemove = participantPendingRemovalIndex?.let { index -> participants.getOrNull(index) }
    if (participantPendingRemovalIndex != null && participantToRemove != null) {
        AlertDialog(
            onDismissRequest = { participantPendingRemovalIndex = null },
            title = { Text("Remover integrante") },
            text = { Text("Deseja remover ${participantToRemove.name} da tarefa?") },
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

    val milestoneToRemove = milestonePendingRemovalIndex?.let { index -> milestones.getOrNull(index) }
    if (milestonePendingRemovalIndex != null && milestoneToRemove != null) {
        AlertDialog(
            onDismissRequest = { milestonePendingRemovalIndex = null },
            title = { Text("Remover milestone") },
            text = { Text("Deseja remover a milestone \"$milestoneToRemove\" da tarefa?") },
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
}
