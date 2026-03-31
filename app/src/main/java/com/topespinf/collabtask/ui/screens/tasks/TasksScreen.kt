package com.topespinf.collabtask.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topespinf.collabtask.ui.theme.ScreenBackground

data class TaskCard(
    val title: String,
    val collaborator: String,
    val progress: String
)

data class ActivityEvent(
    val actor: String,
    val action: String,
    val whenText: String
)

@Composable
fun TasksScreen() {
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showEditTaskDialog by remember { mutableStateOf(false) }
    
    val tasks = listOf(
        TaskCard("Criar wireframes do dashboard", "Ana", "Em progresso"),
        TaskCard("Revisar backlog da sprint", "Carlos", "Aguardando"),
        TaskCard("Configurar notificacoes", "Maria", "Concluida")
    )
    val history = listOf(
        ActivityEvent("Ana", "comentou na tarefa 'Wireframes'", "ha 10 min"),
        ActivityEvent("Carlos", "alterou status para Em progresso", "ha 32 min"),
        ActivityEvent("Maria", "foi atribuida na tarefa 'Notificacoes'", "ha 1 h")
    )

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
                    Text("Acoes Rapidas", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Fluxo de criacao e atribuicao de tarefas para a equipe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(onClick = { showCreateTaskDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Criar Tarefa")
                    }
                    OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                        Text("Atribuir Colaborador")
                    }
                }
            }
        }
        items(tasks) { task ->
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(task.title, fontWeight = FontWeight.SemiBold)
                    Text("Colaborador: ${task.collaborator}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${task.progress}", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { showEditTaskDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar Tarefa")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Notificacoes", fontWeight = FontWeight.SemiBold)
                    Text("3 atualizacoes pendentes de leitura", style = MaterialTheme.typography.bodySmall)
                    Text("- Ana comentou em 'Wireframes'", style = MaterialTheme.typography.bodySmall)
                    Text("- Sprint atualizada por Carlos", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Historico de Alteracoes e Comentarios", fontWeight = FontWeight.SemiBold)
                    history.forEach { event ->
                        Text("${event.actor} ${event.action} (${event.whenText})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    // Diálogos FORA do LazyColumn
    if (showCreateTaskDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateTaskDialog = false },
            onSave = { 
                showCreateTaskDialog = false
            }
        )
    }

    if (showEditTaskDialog) {
        EditTaskDialog(
            onDismiss = { showEditTaskDialog = false },
            onSave = { 
                showEditTaskDialog = false
            }
        )
    }
}

