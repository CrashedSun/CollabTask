package com.topespinf.collabtask.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.ProfileViewModel

class ProfileActivity : ComponentActivity() {
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_profile)
        BottomNavHandler.bind(this)

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val usernameText = findViewById<TextView>(R.id.profileUsernameText)

        val user = profileViewModel.getCurrentUser()
        if (user != null) {
            usernameText.text = getString(R.string.xml_profile_user_fmt, user.user)
        }

        findViewById<MaterialButton>(R.id.profileChangePasswordButton).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.profileLogoutButton).setOnClickListener {
            profileViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}


