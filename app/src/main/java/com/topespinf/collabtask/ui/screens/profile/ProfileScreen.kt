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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topespinf.collabtask.R
import com.topespinf.collabtask.ui.theme.ScreenBackground

@Composable
fun ProfileScreen(
    onChangePasswordClick: () -> Unit
) {
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
                        OutlinedButton(onClick = { }) {
                            Text("Editar Perfil")
                        }
                    }
                    Text("Usuario: exemplo123")
                    Text("Nome Completo: Exemplo Exemplo de Exemplo Neto")
                    Text("Endereco de E-mail: exemplo@email.com")
                    Text("Perfil: Administrador")
                    Text("Permissoes: criar, atribuir e gerenciar tarefas")
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Seguranca da Conta", fontWeight = FontWeight.SemiBold)
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onChangePasswordClick) {
                        Text(stringResource(R.string.change_password))
                    }
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { }) {
                        Text(stringResource(R.string.send_recovery_email))
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gerenciamento de Sessao", fontWeight = FontWeight.SemiBold)
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }
        }
    }
}

