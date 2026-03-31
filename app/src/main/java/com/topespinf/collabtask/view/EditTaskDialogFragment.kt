package com.topespinf.collabtask.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.topespinf.collabtask.R
import com.topespinf.collabtask.viewmodel.TaskViewModel

class EditTaskDialogFragment : BottomSheetDialogFragment() {
    private lateinit var taskViewModel: TaskViewModel
    private val editableCollaborators = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_edit_task, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val statusField = view.findViewById<TextInputEditText>(R.id.editTaskStatus)
        val commentField = view.findViewById<TextInputEditText>(R.id.editTaskComment)
        val closeButton = view.findViewById<View>(R.id.closeEditTaskButton)
        val saveButton = view.findViewById<MaterialButton>(R.id.saveTaskChangesButton)
        val collaboratorsContainer = view.findViewById<LinearLayout>(R.id.editTaskCollaboratorsContainer)
        val addCollaboratorInput = view.findViewById<TextInputEditText>(R.id.addCollaboratorInput)
        val addCollaboratorLayout = addCollaboratorInput.parent as TextInputLayout

        closeButton.setOnClickListener {
            dismiss()
        }

        // Get first task to display
        val tasks = taskViewModel.getTasks()
        val task = tasks.firstOrNull()

        if (task != null) {
            statusField.setText(task.status)
            editableCollaborators.addAll(task.collaborators)
            
            // Exibir colaboradores
            updateCollaboratorsList(collaboratorsContainer)

            // Listener para adicionar colaborador
            addCollaboratorLayout.setEndIconOnClickListener {
                val collaborator = addCollaboratorInput.text?.toString()?.trim()
                if (!collaborator.isNullOrBlank()) {
                    if (!editableCollaborators.contains(collaborator)) {
                        editableCollaborators.add(collaborator)
                        updateCollaboratorsList(collaboratorsContainer)
                        addCollaboratorInput.text?.clear()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Este colaborador já foi adicionado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Digite o nome do colaborador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        saveButton.setOnClickListener {
            val updated = taskViewModel.updateFirstTask(
                status = statusField.text?.toString().orEmpty(),
                comment = commentField.text?.toString().orEmpty(),
                collaborators = editableCollaborators
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

    private fun updateCollaboratorsList(container: LinearLayout) {
        container.removeAllViews()
        
        if (editableCollaborators.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "Nenhum colaborador adicionado"
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.xml_text_secondary))
            }
            container.addView(emptyView)
        } else {
            editableCollaborators.forEach { collaborator ->
                val itemLayout = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                }

                val collaboratorView = TextView(requireContext()).apply {
                    text = "• $collaborator"
                    textSize = 16f
                    setTextColor(requireContext().getColor(R.color.xml_text_primary))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val removeButton = MaterialButton(requireContext()).apply {
                    text = "Remover"
                    textSize = 12f
                    setTextColor(requireContext().getColor(R.color.xml_danger))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        editableCollaborators.remove(collaborator)
                        updateCollaboratorsList(container)
                    }
                }

                itemLayout.addView(collaboratorView)
                itemLayout.addView(removeButton)
                container.addView(itemLayout)
            }
        }
    }
}





