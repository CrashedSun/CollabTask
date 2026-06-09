package com.topespinf.collabtask.model

import com.google.firebase.Timestamp

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val status: String = "Nao iniciada",
    val comments: List<String> = emptyList(),
    val participants: List<TaskParticipant> = emptyList(),
    val milestones: List<TaskMilestone> = emptyList(),
    val createdAt: Timestamp = Timestamp(0, 0),
    val updatedAt: Timestamp = Timestamp(0, 0)
)

