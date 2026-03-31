package com.topespinf.collabtask.model

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val assignee: String,
    val status: String,
    val comments: MutableList<String> = mutableListOf(),
    val collaborators: MutableList<String> = mutableListOf()
)

