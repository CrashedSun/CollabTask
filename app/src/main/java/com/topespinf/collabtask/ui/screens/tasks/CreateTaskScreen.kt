package com.topespinf.collabtask.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topespinf.collabtask.ui.theme.ScreenBackground
import com.topespinf.collabtask.viewmodel.TaskViewModel

@Composable
fun CreateTaskScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("") }
    var milestones by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Criação de Tarefa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Crie e atribua tarefas para sua equipe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titulo") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descricao") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Atribuir para") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = milestones,
                    onValueChange = { milestones = it },
                    label = { Text("Milestones (separadas por vírgula)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    enabled = !isLoading
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank() || assignedTo.isBlank()) {
                            errorMessage = "Por favor, preencha todos os campos."
                            return@Button
                        }
                        val milestoneTitles = milestones.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (milestoneTitles.isEmpty()) {
                            errorMessage = "Adicione ao menos uma milestone."
                            return@Button
                        }
                        isLoading = true
                        taskViewModel.createTask(
                            title = title,
                            description = description,
                            assignee = assignedTo,
                            milestones = milestoneTitles,
                            onSuccess = {
                                isLoading = false
                                onSave()
                            },
                            onError = {
                                isLoading = false
                                errorMessage = it
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Salvar Tarefa")
                    }
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
