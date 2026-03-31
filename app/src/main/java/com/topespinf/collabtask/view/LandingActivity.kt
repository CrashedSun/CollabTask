package com.topespinf.collabtask.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton
import com.topespinf.collabtask.R

class LandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_landing)

        findViewById<MaterialButton>(R.id.landingLoginButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.landingRegisterButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

