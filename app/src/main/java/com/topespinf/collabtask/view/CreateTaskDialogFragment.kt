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

class CreateTaskDialogFragment : BottomSheetDialogFragment() {
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_create_task, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val titleField = view.findViewById<TextInputEditText>(R.id.createTaskTitle)
        val descriptionField = view.findViewById<TextInputEditText>(R.id.createTaskDescription)
        val assigneeField = view.findViewById<TextInputEditText>(R.id.createTaskAssignedTo)
        val closeButton = view.findViewById<View>(R.id.closeCreateTaskButton)
        val saveButton = view.findViewById<MaterialButton>(R.id.saveTaskButton)

        closeButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            val created = taskViewModel.createTask(
                title = titleField.text?.toString().orEmpty(),
                description = descriptionField.text?.toString().orEmpty(),
                assignee = assigneeField.text?.toString().orEmpty()
            )

            if (created) {
                Toast.makeText(
                    requireContext(),
                    R.string.xml_task_saved,
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

