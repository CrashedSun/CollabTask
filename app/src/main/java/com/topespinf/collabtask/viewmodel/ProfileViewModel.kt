package com.topespinf.collabtask.viewmodel

import androidx.lifecycle.ViewModel
import com.topespinf.collabtask.model.User
import com.topespinf.collabtask.repositories.SessionRepository

class ProfileViewModel : ViewModel() {
    fun getCurrentUser(): User? = SessionRepository.currentUser

    fun logout() {
        SessionRepository.currentUser = null
    }
}

