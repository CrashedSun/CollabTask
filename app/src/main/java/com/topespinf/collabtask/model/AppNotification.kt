package com.topespinf.collabtask.model

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val createdAt: Timestamp = Timestamp(0, 0),
    val read: Boolean = false
)
