package com.topespinf.collabtask.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton
import com.topespinf.collabtask.R

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_home)
        BottomNavHandler.bind(this)

        findViewById<MaterialButton>(R.id.homeGetStartedButton).setOnClickListener {
            startActivity(Intent(this, UsageGuideActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.homeOpenTasksButton).setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.homeOpenProfileButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}


