package com.topespinf.collabtask.ui.screens.tasks

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("EditTaskDialog", "EditTaskDialog is being rendered")
    
    var status by remember { mutableStateOf("Em Andamento") }
    var comment by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Editar Tarefa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Atualize status, progresso e comentários.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comentário") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Button(
                onClick = {
                    onSave()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Alterações")
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}









