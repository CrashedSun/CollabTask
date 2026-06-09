package com.topespinf.collabtask.model

data class TaskParticipant(
    val userId: String = "",
    val name: String = "",
    val role: String = "MEMBER"
)
