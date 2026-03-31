package com.topespinf.collabtask.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.AuthViewModel

class RegisterActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_register)
        BottomNavHandler.bind(this)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val nameField = findViewById<TextInputEditText>(R.id.registerName)
        val emailField = findViewById<TextInputEditText>(R.id.registerEmail)
        val passwordField = findViewById<TextInputEditText>(R.id.registerPassword)

        findViewById<MaterialButton>(R.id.registerButton).setOnClickListener {
            val isRegistered = authViewModel.register(
                nameField.text?.toString().orEmpty(),
                emailField.text?.toString().orEmpty(),
                passwordField.text?.toString().orEmpty()
            )
            if (isRegistered) {
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            } else {
                Toast.makeText(this, R.string.xml_register_error, Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.backToLoginButton).setOnClickListener {
            finish()
        }
    }
}


