package com.topespinf.collabtask.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.TaskViewModel

class EditTaskDialogFragment : BottomSheetDialogFragment() {
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_edit_task, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val taskNameField = view.findViewById<TextInputEditText>(R.id.editTaskName)
        val taskDescriptionField = view.findViewById<TextInputEditText>(R.id.editTaskDescription)
        val statusField = view.findViewById<TextInputEditText>(R.id.editTaskStatus)
        val closeButton = view.findViewById<View>(R.id.closeEditTaskButton)
        val saveButton = view.findViewById<MaterialButton>(R.id.saveTaskChangesButton)

        closeButton.setOnClickListener {
            dismiss()
        }

        // Get first task to display
        val tasks = taskViewModel.getTasks()
        val task = tasks.firstOrNull()

        if (task != null) {
            taskNameField.setText(task.title)
            taskDescriptionField.setText(task.description)
            statusField.setText(task.status)
        }

        saveButton.setOnClickListener {
            val updated = taskViewModel.updateFirstTask(
                status = statusField.text?.toString().orEmpty(),
                comment = ""
            )

            if (updated) {
                Toast.makeText(
                    requireContext(),
                    R.string.xml_task_updated,
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.xml_task_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}







