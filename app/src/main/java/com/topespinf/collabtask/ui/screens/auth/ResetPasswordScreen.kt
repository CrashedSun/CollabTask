package com.topespinf.collabtask.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun ResetPasswordScreen(
    onConfirm: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by SessionRepository.currentUser.collectAsState()
    var email by remember { mutableStateOf(currentUser?.email.orEmpty()) }
    var isLoading by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        if (email.isBlank()) {
            email = currentUser?.email.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.reset_password),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                if (!feedback.isNullOrBlank()) {
                    Text(
                        text = feedback.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        authViewModel.sendPasswordReset(
                            email = email,
                            onSuccess = {
                                isLoading = false
                                feedback = "E-mail de redefinição enviado."
                                onConfirm()
                            },
                            onError = {
                                isLoading = false
                                feedback = it
                            }
                        )
                    },
                    enabled = !isLoading && email.isNotBlank()
                ) {
                    Text(stringResource(R.string.send_recovery_email))
                }
            }
        }
    }
}
