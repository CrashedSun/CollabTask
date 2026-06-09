package com.topespinf.collabtask.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.topespinf.collabtask.model.Task
import com.topespinf.collabtask.model.TaskMilestone
import com.topespinf.collabtask.model.TaskParticipant
import com.topespinf.collabtask.model.User

internal data class FirestoreUserRecord(
    val name: String = "",
    val email: String = "",
    val user: String = "",
    val role: String = "Membro",
    val createdAt: Timestamp = Timestamp(0, 0)
)

internal data class FirestoreTaskRecord(
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

internal fun FirebaseUser.toSessionUser(role: String = "Membro"): User {
    return User(
        id = uid,
        name = displayName.orEmpty().ifBlank { email.orEmpty() },
        email = email.orEmpty(),
        user = "",
        role = role
    )
}

internal fun User.toFirestoreRecord(): FirestoreUserRecord {
    return FirestoreUserRecord(
        name = name,
        email = email,
        user = user,
        role = role,
        createdAt = Timestamp.now()
    )
}

internal fun Task.toFirestoreRecord(): FirestoreTaskRecord {
    return FirestoreTaskRecord(
        title = title,
        description = description,
        ownerId = ownerId,
        ownerName = ownerName,
        status = status,
        comments = comments,
        participants = participants,
        milestones = milestones,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

internal fun FirestoreTaskRecord.toTask(id: String): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        ownerId = ownerId,
        ownerName = ownerName,
        status = status,
        comments = comments,
        participants = participants,
        milestones = milestones,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
