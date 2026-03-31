package com.topespinf.collabtask.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.TaskViewModel

class EditTaskActivity : ComponentActivity() {
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_edit_task)
        BottomNavHandler.bind(this)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val statusField = findViewById<TextInputEditText>(R.id.editTaskStatus)
        val commentField = findViewById<TextInputEditText>(R.id.editTaskComment)

        findViewById<MaterialButton>(R.id.saveTaskChangesButton).setOnClickListener {
            val updated = taskViewModel.updateFirstTask(
                status = statusField.text?.toString().orEmpty(),
                comment = commentField.text?.toString().orEmpty()
            )

            if (updated) {
                Toast.makeText(this, R.string.xml_task_updated, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.xml_task_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}


