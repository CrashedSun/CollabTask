package com.topespinf.collabtask.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topespinf.collabtask.R
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.ui.theme.ScreenBackground
import com.topespinf.collabtask.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by SessionRepository.currentUser.collectAsState()
    var feedback by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(currentUser?.name.orEmpty()) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var deleteConfirmationName by remember { mutableStateOf("") }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.profile_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        item {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Seu Perfil", fontWeight = FontWeight.SemiBold)
                        OutlinedButton(
                            onClick = {
                                if (isEditingProfile) {
                                    isEditingProfile = false
                                } else {
                                    editedName = currentUser?.name.orEmpty()
                                    isEditingProfile = true
                                }
                            },
                            enabled = !isSavingProfile
                        ) { Text(if (isEditingProfile) "Cancelar" else "Editar Perfil") }
                    }
                    if (isEditingProfile) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Nome completo") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSavingProfile
                        )
                        Button(
                            onClick = {
                                isSavingProfile = true
                                authViewModel.updateDisplayName(
                                    newName = editedName,
                                    onSuccess = {
                                        isSavingProfile = false
                                        isEditingProfile = false
                                        feedback = "Perfil atualizado com sucesso."
                                    },
                                    onError = {
                                        isSavingProfile = false
                                        feedback = it
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSavingProfile
                        ) {
                            Text("Salvar Perfil")
                        }
                    }
                    Text("Usuario: ${currentUser?.user.orEmpty().ifBlank { "Sem acesso" }}")
                    Text("Nome Completo: ${currentUser?.name.orEmpty().ifBlank { "Sem acesso" }}")
                    Text("Endereco de E-mail: ${currentUser?.email.orEmpty().ifBlank { "Sem acesso" }}")
                    Text("Permissoes: criar, atribuir e gerenciar tarefas")
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Seguranca da Conta", fontWeight = FontWeight.SemiBold)
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val email = currentUser?.email.orEmpty()
                            if (email.isBlank()) {
                                feedback = "Faça login para enviar o e-mail de redefinição."
                            } else {
                                isSending = true
                                authViewModel.sendPasswordReset(
                                    email = email,
                                    onSuccess = {
                                        isSending = false
                                        feedback = "E-mail de redefinição enviado."
                                    },
                                    onError = {
                                        isSending = false
                                        feedback = it
                                    }
                                )
                            }
                        },
                        enabled = !isSending
                    ) {
                        Text("Enviar E-mail de Recuperação")
                    }
                    if (!feedback.isNullOrBlank()) {
                        Text(
                            text = feedback.orEmpty(),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gerenciamento de Sessao", fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            authViewModel.logout()
                            onLogoutClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Excluir Conta", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    Text(
                        "Para confirmar, digite seu nome completo exatamente como está no perfil.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = deleteConfirmationName,
                        onValueChange = { deleteConfirmationName = it },
                        label = { Text("Nome completo de confirmação") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDeletingAccount
                    )
                    Button(
                        onClick = { showDeleteAccountConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDeletingAccount &&
                            deleteConfirmationName.trim().equals(currentUser?.name.orEmpty().trim(), ignoreCase = true),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Excluir minha conta")
                    }
                    if (!feedback.isNullOrBlank()) {
                        Text(
                            text = feedback.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showDeleteAccountConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmation = false },
            title = { Text("Confirmar exclusão de conta") },
            text = { Text("Essa ação é permanente e não poderá ser desfeita. Deseja continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountConfirmation = false
                        isDeletingAccount = true
                        authViewModel.deleteCurrentAccount(
                            confirmationName = deleteConfirmationName,
                            onSuccess = {
                                isDeletingAccount = false
                                onLogoutClick()
                            },
                            onError = { error ->
                                isDeletingAccount = false
                                feedback = error
                            }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
