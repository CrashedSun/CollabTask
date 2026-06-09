package com.topespinf.collabtask.model

import com.google.firebase.Timestamp

data class TaskMilestone(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val completedById: String? = null,
    val completedByName: String? = null,
    val completedAt: Timestamp? = null
)
