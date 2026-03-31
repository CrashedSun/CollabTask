package com.topespinf.collabtask.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.TaskViewModel

class TaskDetailDialogFragment : BottomSheetDialogFragment() {
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_task_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val closeButton = view.findViewById<View>(R.id.closeTaskDetailButton)
        val titleView = view.findViewById<TextView>(R.id.taskDetailTitle)
        val descriptionView = view.findViewById<TextView>(R.id.taskDetailDescription)
        val creatorView = view.findViewById<TextView>(R.id.taskDetailCreator)
        val statusView = view.findViewById<TextView>(R.id.taskDetailStatus)
        val collaboratorsContainer = view.findViewById<LinearLayout>(R.id.taskDetailCollaboratorsContainer)

        closeButton.setOnClickListener {
            dismiss()
        }

        // Get first task to display
        val tasks = taskViewModel.getTasks()
        val task = tasks.firstOrNull()

        if (task != null) {
            titleView.text = task.title
            descriptionView.text = task.description
            creatorView.text = "Criada por: ${task.assignee}"
            statusView.text = task.status

            // Exibir colaboradores
            collaboratorsContainer.removeAllViews()
            if (task.collaborators.isNotEmpty()) {
                task.collaborators.forEach { collaborator ->
                    val collaboratorView = TextView(requireContext()).apply {
                        text = "• $collaborator"
                        textSize = 16f
                        setTextColor(requireContext().getColor(R.color.xml_text_primary))
                    }
                    collaboratorsContainer.addView(collaboratorView)
                }
            } else {
                val emptyView = TextView(requireContext()).apply {
                    text = "Nenhum colaborador atribuído"
                    textSize = 16f
                    setTextColor(requireContext().getColor(R.color.xml_text_secondary))
                }
                collaboratorsContainer.addView(emptyView)
            }
        }
    }
}


