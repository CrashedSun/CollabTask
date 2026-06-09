package com.topespinf.collabtask.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.topespinf.collabtask.model.User
import com.topespinf.collabtask.repositories.AuthRepository
import com.topespinf.collabtask.repositories.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AuthViewModel : ViewModel() {
    fun login(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                AuthRepository.login(email, password)
            }.onSuccess { user ->
                SessionRepository.setCurrentUser(user)
                onSuccess(user)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível fazer login.")
            }
        }
    }

    fun login(email: String, password: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            runCatching { AuthRepository.login(email, password) }.onSuccess { user ->
                SessionRepository.setCurrentUser(user)
            }.isSuccess
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                AuthRepository.register(name, email, password)
            }.onSuccess { user ->
                SessionRepository.setCurrentUser(user)
                onSuccess(user)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível criar a conta.")
            }
        }
    }

    fun register(name: String, email: String, password: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            runCatching { AuthRepository.register(name, email, password) }.onSuccess { user ->
                SessionRepository.setCurrentUser(user)
            }.isSuccess
        }
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                AuthRepository.sendPasswordReset(email)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível enviar o e-mail de redefinição.")
            }
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String): Boolean {
        val email = SessionRepository.currentUser.value?.email ?: return false
        if (newPassword.length < 4 || newPassword != confirmPassword) {
            return false
        }
        return runBlocking(Dispatchers.IO) {
            runCatching { AuthRepository.sendPasswordReset(email) }.isSuccess
        }
    }

    fun signInWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                AuthRepository.signInWithGoogle(account)
            }.onSuccess { user ->
                SessionRepository.setCurrentUser(user)
                onSuccess(user)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível entrar com Google.")
            }
        }
    }

    fun buildGoogleSignInClient(context: Context): GoogleSignInClient? {
        val clientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (clientIdRes == 0) return null
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(clientIdRes))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun logout() {
        AuthRepository.logout()
    }

    fun deleteCurrentAccount(
        confirmationName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                AuthRepository.deleteCurrentAccount(confirmationName)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível excluir sua conta.")
            }
        }
    }

    fun updateDisplayName(
        newName: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                AuthRepository.updateDisplayName(newName)
            }.onSuccess { user ->
                SessionRepository.setCurrentUser(user)
                onSuccess(user)
            }.onFailure { error ->
                onError(error.message ?: "Não foi possível atualizar o perfil.")
            }
        }
    }
}
