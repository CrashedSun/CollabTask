package com.topespinf.collabtask.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.topespinf.collabtask.MainActivity

class LandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

