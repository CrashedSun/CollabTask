package com.topespinf.collabtask.ui.screens.auth

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.topespinf.collabtask.model.User
import com.topespinf.collabtask.ui.theme.ScreenBackground
import com.topespinf.collabtask.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleClient = remember(context) { authViewModel.buildGoogleSignInClient(context) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: run {
            errorMessage = "Não foi possível iniciar o login com Google."
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account == null) {
                errorMessage = "Conta Google inválida."
                return@rememberLauncherForActivityResult
            }
            isLoading = true
            authViewModel.signInWithGoogle(
                account = account,
                onSuccess = {
                    isLoading = false
                    onLoginSuccess()
                },
                onError = {
                    isLoading = false
                    errorMessage = it
                }
            )
        } catch (exception: ApiException) {
            isLoading = false
            errorMessage = exception.message ?: "Falha ao autenticar com Google."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("CollabTask", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Entre para colaborar com sua equipe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
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
                        isLoading = true
                        authViewModel.login(
                            email = email,
                            password = password,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
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
                    Text("Entrar")
                }

                OutlinedButton(
                    onClick = {
                        if (googleClient == null) {
                            errorMessage = "Rebaixe o google-services.json após ativar Google Sign-In e cadastrar o SHA-1."
                        } else {
                            googleLauncher.launch(googleClient.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Entrar com Google")
                }

                OutlinedButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                    Text("Criar Conta")
                }
                OutlinedButton(onClick = onForgotPasswordClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                    Text("Esqueci minha senha")
                }
            }
        }
    }
}
