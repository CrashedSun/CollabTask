package com.topespinf.collabtask.view

import android.app.Activity
import android.content.res.ColorStateList
import android.content.Intent
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat
import com.topespinf.collabtask.R

object BottomNavHandler {
    fun bind(activity: Activity) {
        bindButton(activity, R.id.bottomNavHomeButton, HomeActivity::class.java)
        bindButton(activity, R.id.bottomNavTasksButton, TasksActivity::class.java)
        bindButton(activity, R.id.bottomNavProfileButton, ProfileActivity::class.java)
    }

    private fun bindButton(activity: Activity, buttonId: Int, target: Class<out Activity>) {
        val button = activity.findViewById<MaterialButton>(buttonId) ?: return
        if (activity::class.java == target) {
            styleSelected(button)
            button.isEnabled = false
            return
        }

        styleDefault(button)

        button.setOnClickListener {
            val intent = Intent(activity, target).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            activity.startActivity(intent)
        }
    }

    private fun styleSelected(button: MaterialButton) {
        val context = button.context
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.xml_primary))
        button.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    private fun styleDefault(button: MaterialButton) {
        val context = button.context
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
        button.setTextColor(ContextCompat.getColor(context, R.color.xml_text_secondary))
        button.isEnabled = true
    }
}



