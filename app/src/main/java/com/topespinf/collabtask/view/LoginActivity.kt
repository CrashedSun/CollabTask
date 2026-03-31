package com.topespinf.collabtask.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_login)
        BottomNavHandler.bind(this)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val emailField = findViewById<TextInputEditText>(R.id.loginEmail)
        val passwordField = findViewById<TextInputEditText>(R.id.loginPassword)

        findViewById<MaterialButton>(R.id.loginButton).setOnClickListener {
            // Verificação de credenciais removida para testes
            // Navega direto para HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.openRegisterButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView>(R.id.forgotPasswordButton).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }
}


