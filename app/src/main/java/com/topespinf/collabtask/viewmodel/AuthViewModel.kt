package com.topespinf.collabtask.viewmodel

import androidx.lifecycle.ViewModel
import com.topespinf.collabtask.repositories.AuthRepository
import com.topespinf.collabtask.repositories.SessionRepository

class AuthViewModel : ViewModel() {
    fun login(email: String, password: String): Boolean {
        val user = AuthRepository.login(email, password) ?: return false
        SessionRepository.currentUser = user
        return true
    }

    fun register(name: String, email: String, password: String): Boolean {
        val user = AuthRepository.register(name, email, password) ?: return false
        SessionRepository.currentUser = user
        return true
    }

    fun resetPassword(newPassword: String, confirmPassword: String): Boolean {
        return AuthRepository.resetPassword(newPassword, confirmPassword)
    }

    fun logout() {
        SessionRepository.currentUser = null
    }
}

