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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topespinf.collabtask.ui.theme.ScreenBackground
import com.topespinf.collabtask.viewmodel.TaskViewModel

@Composable
fun EditTaskScreen(
    onSave: () -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks by taskViewModel.tasks.collectAsState()
    var status by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tasks) {
        tasks.firstOrNull()?.let { firstTask ->
            if (status.isBlank()) {
                status = firstTask.status
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Editar Tarefa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Atualize status, progresso e comentários.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth(),
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
                        if (status.isBlank()) {
                            errorMessage = "Status não pode estar vazio."
                            return@Button
                        }
                        isLoading = true
                        taskViewModel.updateFirstTask(
                            status = status,
                            comment = comment,
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
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
                    } else {
                        Text("Salvar Alterações")
                    }
                }

                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
