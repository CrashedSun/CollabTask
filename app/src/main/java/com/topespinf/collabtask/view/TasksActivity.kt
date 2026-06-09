package com.topespinf.collabtask.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.TaskViewModel

class TasksActivity : AppCompatActivity() {
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_tasks)
        BottomNavHandler.bind(this)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        findViewById<MaterialButton>(R.id.openCreateTaskButton).setOnClickListener {
            CreateTaskDialogFragment().show(supportFragmentManager, "CreateTaskDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        val tasks = taskViewModel.getTasks()
        bindTaskCard(findViewById(R.id.taskItemOne), tasks.getOrNull(0))
        bindTaskCard(findViewById(R.id.taskItemTwo), tasks.getOrNull(1))
    }

    private fun bindTaskCard(cardRoot: View, task: com.topespinf.collabtask.model.Task?) {
        if (task == null) {
            cardRoot.visibility = View.GONE
            return
        }

        cardRoot.visibility = View.VISIBLE
        cardRoot.findViewById<TextView>(R.id.taskTitle).text = task.title
        val assigneeName = task.participants.firstOrNull { it.role.equals("MEMBER", ignoreCase = true) }?.name
            ?: task.participants.firstOrNull { it.role.equals("ADMIN", ignoreCase = true) }?.name
            ?: task.ownerName
        cardRoot.findViewById<TextView>(R.id.taskOwner).text = getString(R.string.xml_task_owner_fmt, assigneeName)
        cardRoot.findViewById<TextView>(R.id.taskStatus).text = getString(R.string.xml_task_status_fmt, task.status)

        // Listeners para botões do card
        cardRoot.findViewById<MaterialButton>(R.id.viewTaskDetailsButton).setOnClickListener {
            TaskDetailDialogFragment().show(supportFragmentManager, "TaskDetailDialog")
        }

        cardRoot.findViewById<MaterialButton>(R.id.editTaskCardButton).setOnClickListener {
            EditTaskDialogFragment().show(supportFragmentManager, "EditTaskDialog")
        }
    }
}


