package com.topespinf.collabtask.repositories

import com.google.firebase.auth.FirebaseAuth
import com.topespinf.collabtask.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionRepository {
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow(auth.currentUser?.toSessionUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun bootstrapFromFirebase() {
        _currentUser.value = auth.currentUser?.toSessionUser()
    }

    fun clear() {
        auth.signOut()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

