package com.topespinf.collabtask.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.TaskViewModel

class CreateTaskActivity : ComponentActivity() {
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_create_task)
        BottomNavHandler.bind(this)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val titleField = findViewById<TextInputEditText>(R.id.createTaskTitle)
        val descriptionField = findViewById<TextInputEditText>(R.id.createTaskDescription)
        val assigneeField = findViewById<TextInputEditText>(R.id.createTaskAssignedTo)

        findViewById<MaterialButton>(R.id.saveTaskButton).setOnClickListener {
            val created = taskViewModel.createTask(
                title = titleField.text?.toString().orEmpty(),
                description = descriptionField.text?.toString().orEmpty(),
                assignee = assigneeField.text?.toString().orEmpty()
            )

            if (created) {
                Toast.makeText(this, R.string.xml_task_saved, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.xml_task_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}


