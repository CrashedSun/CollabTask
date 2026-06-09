package com.topespinf.collabtask.viewmodel

import androidx.lifecycle.ViewModel
import com.topespinf.collabtask.model.User
import com.topespinf.collabtask.repositories.AuthRepository
import com.topespinf.collabtask.repositories.SessionRepository

class ProfileViewModel : ViewModel() {
    fun getCurrentUser(): User? = SessionRepository.currentUser.value

    fun logout() {
        AuthRepository.logout()
    }
}
