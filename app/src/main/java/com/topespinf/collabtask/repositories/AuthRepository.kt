package com.topespinf.collabtask.repositories

import com.topespinf.collabtask.model.User

object AuthRepository {
    private val users = mutableListOf(
        User(id = 1, name = "Administrador CollabTask", email = "admin@collabtask.com", role = "Administrador")
    )

    fun login(email: String, password: String): User? {
        if (password.isBlank()) return null
        return users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
    }

    fun register(name: String, email: String, password: String): User? {
        if (name.isBlank() || email.isBlank() || password.length < 4) return null
        if (users.any { it.email.equals(email.trim(), ignoreCase = true) }) return null

        val user = User(
            id = users.size + 1,
            name = name.trim(),
            email = email.trim(),
            role = "Membro"
        )
        users.add(user)
        return user
    }

    fun resetPassword(newPassword: String, confirmPassword: String): Boolean {
        return newPassword.length >= 4 && newPassword == confirmPassword
    }
}

