package com.topespinf.collabtask.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton
import com.topespinf.collabtask.R

class UsageGuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_usage_guide)
        BottomNavHandler.bind(this)

        findViewById<MaterialButton>(R.id.usageContinueButton).setOnClickListener {
            finish()
        }
    }
}


