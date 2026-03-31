package com.topespinf.collabtask.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.AuthViewModel

class ResetPasswordActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_reset_password)
        BottomNavHandler.bind(this)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val newPasswordField = findViewById<TextInputEditText>(R.id.resetPasswordNew)
        val confirmPasswordField = findViewById<TextInputEditText>(R.id.resetPasswordConfirm)

        findViewById<MaterialButton>(R.id.resetPasswordConfirmButton).setOnClickListener {
            val success = authViewModel.resetPassword(
                newPasswordField.text?.toString().orEmpty(),
                confirmPasswordField.text?.toString().orEmpty()
            )
            if (success) {
                Toast.makeText(this, R.string.xml_password_updated, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.xml_password_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}


