package com.topespinf.collabtask.repositories

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.topespinf.collabtask.model.Task
import com.topespinf.collabtask.model.TaskMilestone
import com.topespinf.collabtask.model.TaskParticipant
import com.topespinf.collabtask.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object TaskRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun observeTasks(): Flow<List<Task>> = callbackFlow {
        val registration = firestore.collection("tasks")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(FirestoreTaskRecord::class.java)?.toTask(document.id)
                }
                trySend(tasks)
            }

        awaitClose { registration.remove() }
    }

    suspend fun createTask(title: String, description: String, assigneeName: String, milestoneTitles: List<String>): Task {
        val assignee = resolveUserByIdentifier(assigneeName.trim())
            ?: error("Integrante inválido: informe um user ou email cadastrado.")
        return createTask(
            title = title,
            description = description,
            participants = listOf(
                TaskParticipant(userId = assignee.id, name = assignee.name, role = "MEMBER")
            ),
            milestoneTitles = milestoneTitles
        )
    }

    suspend fun createTask(
        title: String,
        description: String,
        participants: List<TaskParticipant>,
        milestoneTitles: List<String>
    ): Task {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para criar tarefas.")
        val milestones = milestoneTitles.mapIndexedNotNull { index, item ->
            val cleaned = item.trim()
            if (cleaned.isBlank()) {
                null
            } else {
                TaskMilestone(
                    id = "m-${index + 1}-${cleaned.hashCode()}",
                    title = cleaned
                )
            }
        }
        val normalizedMilestones = normalizeMilestones(milestones)
        require(normalizedMilestones.isNotEmpty()) { "Adicione ao menos uma milestone." }
        val incomingParticipants = participants
            .map {
                it.copy(
                    role = if (it.role.equals("ADMIN", ignoreCase = true)) "ADMIN" else "MEMBER"
                )
            }
            .filter { it.name.isNotBlank() || it.userId.isNotBlank() }
        val validatedParticipants = validateAndNormalizeParticipants(
            participants = listOf(
                TaskParticipant(userId = currentUser.id, name = currentUser.name, role = "ADMIN")
            ) + incomingParticipants,
            ownerId = currentUser.id,
            ownerName = currentUser.name
        )
        require(validatedParticipants.any { it.userId != currentUser.id }) {
            "Adicione ao menos um integrante válido."
        }
        val now = Timestamp.now()
        val document = firestore.collection("tasks").document()
        val task = Task(
            id = document.id,
            title = title.trim(),
            description = description.trim(),
            ownerId = currentUser.id,
            ownerName = currentUser.name,
            status = "Nao iniciada",
            comments = emptyList(),
            participants = validatedParticipants,
            milestones = normalizedMilestones,
            createdAt = now,
            updatedAt = now
        )
        document.set(task.toFirestoreRecord(), SetOptions.merge()).await()
        notifyAddedParticipants(
            taskId = task.id,
            taskTitle = task.title,
            ownerId = task.ownerId,
            previousParticipants = emptyList(),
            currentParticipants = task.participants
        )
        return task
    }

    suspend fun updateTask(taskId: String, status: String, comment: String): Task {
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")
        return updateTask(
            taskId = taskId,
            title = current.title,
            description = current.description,
            status = status,
            participants = current.participants,
            comment = comment
        )
    }

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        status: String,
        participants: List<TaskParticipant>,
        milestones: List<TaskMilestone>? = null,
        comment: String
    ): Task {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para editar tarefas.")
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")
        requireTaskAdmin(current, currentUser.id, currentUser.name, "editar a tarefa")
        val normalizedParticipants = participants
            .map {
                it.copy(
                    role = if (it.role.equals("ADMIN", ignoreCase = true)) "ADMIN" else "MEMBER"
                )
            }
            .filter { it.name.isNotBlank() || it.userId.isNotBlank() }
        val validatedParticipants = validateAndNormalizeParticipants(
            participants = normalizedParticipants,
            ownerId = current.ownerId,
            ownerName = current.ownerName
        )
        require(validatedParticipants.isNotEmpty()) { "A tarefa precisa de ao menos um participante." }
        require(validatedParticipants.any { it.role == "ADMIN" }) { "A tarefa precisa de ao menos um ADMIN." }
        val ownerParticipant = validatedParticipants.firstOrNull { it.userId == current.ownerId }
            ?: error("O criador da tarefa deve permanecer como participante ADMIN.")
        require(ownerParticipant.role == "ADMIN") {
            "Não é permitido remover status ADMIN do criador da tarefa."
        }
        val normalizedMilestones = normalizeMilestones(milestones ?: current.milestones)
        require(normalizedMilestones.isNotEmpty()) { "A tarefa precisa de ao menos uma milestone." }

        val updated = current.copy(
            title = title.trim().ifBlank { current.title },
            description = description.trim().ifBlank { current.description },
            status = status.ifBlank { current.status },
            participants = validatedParticipants,
            milestones = normalizedMilestones,
            comments = current.comments + comment.trim().takeIf { it.isNotBlank() }.orEmpty().let { newComment ->
                if (newComment.isBlank()) emptyList() else listOf(newComment)
            },
            updatedAt = Timestamp.now()
        )
        document.set(updated.toFirestoreRecord(), SetOptions.merge()).await()
        notifyParticipantChanges(
            taskId = updated.id,
            taskTitle = updated.title,
            ownerId = updated.ownerId,
            previousParticipants = current.participants,
            currentParticipants = updated.participants
        )
        return updated
    }

    suspend fun deleteTask(taskId: String) {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para excluir tarefas.")
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")
        requireTaskAdmin(current, currentUser.id, currentUser.name, "excluir a tarefa")
        document.delete().await()
    }

    suspend fun leaveTask(taskId: String): Task {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para sair da tarefa.")
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")

        require(current.ownerId != currentUser.id) { "O criador da tarefa não pode sair dela." }
        val isParticipant = current.participants.any {
            it.userId == currentUser.id || it.name.equals(currentUser.name, ignoreCase = true)
        }
        require(isParticipant) { "Você não participa desta tarefa." }

        val updatedParticipants = current.participants.filterNot {
            it.userId == currentUser.id || it.name.equals(currentUser.name, ignoreCase = true)
        }
        val updated = current.copy(
            participants = updatedParticipants,
            updatedAt = Timestamp.now()
        )
        document.set(updated.toFirestoreRecord(), SetOptions.merge()).await()
        notifyTaskLeaveToAdmins(
            taskId = updated.id,
            taskTitle = updated.title,
            ownerId = updated.ownerId,
            participants = updated.participants,
            leavingUserId = currentUser.id,
            leavingUserName = currentUser.name
        )
        return updated
    }

    suspend fun reopenCompletedTask(taskId: String): Task {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para reabrir tarefas.")
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")
        requireTaskAdmin(current, currentUser.id, currentUser.name, "reabrir tarefas concluídas")

        val updated = current.copy(
            status = "Em progresso",
            updatedAt = Timestamp.now()
        )
        document.set(updated.toFirestoreRecord(), SetOptions.merge()).await()
        return updated
    }

    suspend fun setTaskInProgress(taskId: String): Task {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para iniciar/continuar tarefas.")
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")
        val isParticipant = current.ownerId == currentUser.id || current.participants.any {
            it.userId == currentUser.id || it.name.equals(currentUser.name, ignoreCase = true)
        }
        require(isParticipant) { "Somente participantes e admins podem iniciar/continuar tarefas." }
        require(!current.status.equals("Concluida", ignoreCase = true)) {
            "Não é possível iniciar/continuar uma tarefa concluída."
        }

        val updated = current.copy(
            status = "Em progresso",
            updatedAt = Timestamp.now()
        )
        document.set(updated.toFirestoreRecord(), SetOptions.merge()).await()
        return updated
    }

    suspend fun resolveParticipantIdentifier(identifier: String): TaskParticipant {
        val resolvedUser = resolveUserByIdentifier(identifier.trim())
            ?: error("Integrante inválido: informe um user ou email cadastrado.")
        return TaskParticipant(
            userId = resolvedUser.id,
            name = resolvedUser.name,
            role = "MEMBER"
        )
    }

    suspend fun completeMilestone(taskId: String, milestoneId: String): Task {
        return setMilestoneCompletion(taskId = taskId, milestoneId = milestoneId, completed = true)
    }

    suspend fun reopenMilestone(taskId: String, milestoneId: String): Task {
        return setMilestoneCompletion(taskId = taskId, milestoneId = milestoneId, completed = false)
    }

    private suspend fun setMilestoneCompletion(taskId: String, milestoneId: String, completed: Boolean): Task {
        val currentUser = SessionRepository.currentUser.value ?: error("Faça login para concluir milestones.")
        val document = firestore.collection("tasks").document(taskId)
        val snapshot = document.get().await()
        val current = snapshot.toObject(FirestoreTaskRecord::class.java)?.toTask(taskId)
            ?: error("Tarefa não encontrada.")

        val isParticipant = current.ownerId == currentUser.id || current.participants.any {
            it.userId == currentUser.id || it.name.equals(currentUser.name, ignoreCase = true)
        }
        val isAdmin = current.ownerId == currentUser.id || current.participants.any {
            it.role.equals("ADMIN", ignoreCase = true) &&
                (it.userId == currentUser.id || it.name.equals(currentUser.name, ignoreCase = true))
        }
        if (completed) {
            require(isParticipant) { "Somente participantes podem concluir milestones." }
        } else {
            require(isAdmin) { "Somente admins podem reabrir milestones." }
        }
        require(current.milestones.any { it.id == milestoneId }) { "Milestone não encontrada." }

        val updatedMilestones = current.milestones.map { milestone ->
            if (milestone.id != milestoneId) {
                milestone
            } else if (completed && !milestone.completed) {
                milestone.copy(
                    completed = true,
                    completedById = currentUser.id,
                    completedByName = currentUser.name,
                    completedAt = Timestamp.now()
                )
            } else if (!completed && milestone.completed) {
                milestone.copy(
                    completed = false,
                    completedById = null,
                    completedByName = null,
                    completedAt = null
                )
            } else {
                milestone
            }
        }
        val updated = current.copy(
            milestones = updatedMilestones,
            updatedAt = Timestamp.now()
        )
        document.set(updated.toFirestoreRecord(), SetOptions.merge()).await()
        val wasAllCompleted = current.milestones.isNotEmpty() && current.milestones.all { it.completed }
        val isAllCompleted = updatedMilestones.isNotEmpty() && updatedMilestones.all { it.completed }
        if (!wasAllCompleted && isAllCompleted) {
            NotificationRepository.createNotification(
                userId = updated.ownerId,
                type = "all_milestones_completed",
                message = "Todas as milestones da tarefa \"${updated.title}\" foram concluídas.",
                taskId = updated.id,
                taskTitle = updated.title
            )
        }
        return updated
    }

    private fun requireTaskAdmin(task: Task, userId: String, userName: String, action: String) {
        val isAdmin = task.ownerId == userId || task.participants.any {
            it.role.equals("ADMIN", ignoreCase = true) &&
                (it.userId == userId || it.name.equals(userName, ignoreCase = true))
        }
        require(isAdmin) { "Somente admins podem $action." }
    }

    private suspend fun validateAndNormalizeParticipants(
        participants: List<TaskParticipant>,
        ownerId: String,
        ownerName: String
    ): List<TaskParticipant> {
        val validated = participants.map { participant ->
            val normalizedRole = if (participant.role.equals("ADMIN", ignoreCase = true)) "ADMIN" else "MEMBER"
            if (participant.userId == ownerId) {
                TaskParticipant(userId = ownerId, name = ownerName, role = "ADMIN")
            } else {
                val resolvedUser = when {
                    participant.userId.isNotBlank() -> resolveUserById(participant.userId)
                    participant.name.isNotBlank() -> resolveUserByIdentifier(participant.name)
                    else -> null
                } ?: error(
                    "Integrante inválido: ${participant.name.ifBlank { participant.userId }} não corresponde a um usuário cadastrado."
                )
                TaskParticipant(
                    userId = resolvedUser.id,
                    name = resolvedUser.name,
                    role = normalizedRole
                )
            }
        }

        return validated
            .groupBy { it.userId }
            .map { (_, sameUserParticipants) ->
                val canonical = sameUserParticipants.first()
                val hasAdmin = sameUserParticipants.any { it.role == "ADMIN" }
                canonical.copy(role = if (hasAdmin) "ADMIN" else "MEMBER")
            }
    }

    private suspend fun resolveUserByIdentifier(identifier: String): User? {
        if (identifier.isBlank()) {
            return null
        }
        val normalizedIdentifier = identifier.trim()
        val byEmail = firestore.collection("users")
            .whereEqualTo("email", normalizedIdentifier)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toUser()
        if (byEmail != null) {
            return byEmail
        }

        return firestore.collection("users")
            .whereEqualTo("user", normalizedIdentifier)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toUser()
    }

    private suspend fun resolveUserById(userId: String): User? {
        if (userId.isBlank()) {
            return null
        }
        val snapshot = firestore.collection("users").document(userId).get().await()
        return snapshot.toUser()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        val record = toObject(FirestoreUserRecord::class.java) ?: return null
        return User(
            id = id,
            name = record.name,
            email = record.email,
            user = record.user,
            role = record.role
        )
    }

    private fun normalizeMilestones(milestones: List<TaskMilestone>): List<TaskMilestone> {
        val usedIds = mutableSetOf<String>()
        return milestones.mapIndexedNotNull { index, milestone ->
            val cleanedTitle = milestone.title.trim()
            if (cleanedTitle.isBlank()) {
                null
            } else {
                val baseId = milestone.id.ifBlank { "m-${index + 1}-${cleanedTitle.hashCode()}" }
                var candidateId = baseId
                var suffix = 1
                while (candidateId in usedIds) {
                    candidateId = "$baseId-$suffix"
                    suffix += 1
                }
                usedIds += candidateId
                milestone.copy(
                    id = candidateId,
                    title = cleanedTitle
                )
            }
        }
    }

    private suspend fun notifyParticipantChanges(
        taskId: String,
        taskTitle: String,
        ownerId: String,
        previousParticipants: List<TaskParticipant>,
        currentParticipants: List<TaskParticipant>
    ) {
        val previousIds = previousParticipants.mapNotNull { it.userId.takeIf { id -> id.isNotBlank() } }.toSet()
        val currentIds = currentParticipants.mapNotNull { it.userId.takeIf { id -> id.isNotBlank() } }.toSet()

        val addedUserIds = currentIds.minus(previousIds).minus(ownerId)
        val removedUserIds = previousIds.minus(currentIds).minus(ownerId)

        addedUserIds.forEach { userId ->
            NotificationRepository.createNotification(
                userId = userId,
                type = "task_member_added",
                message = "Você foi adicionado à tarefa \"$taskTitle\".",
                taskId = taskId,
                taskTitle = taskTitle
            )
        }
        removedUserIds.forEach { userId ->
            NotificationRepository.createNotification(
                userId = userId,
                type = "task_member_removed",
                message = "Você foi removido da tarefa \"$taskTitle\".",
                taskId = taskId,
                taskTitle = taskTitle
            )
        }
    }

    private suspend fun notifyAddedParticipants(
        taskId: String,
        taskTitle: String,
        ownerId: String,
        previousParticipants: List<TaskParticipant>,
        currentParticipants: List<TaskParticipant>
    ) {
        notifyParticipantChanges(
            taskId = taskId,
            taskTitle = taskTitle,
            ownerId = ownerId,
            previousParticipants = previousParticipants,
            currentParticipants = currentParticipants
        )
    }

    private suspend fun notifyTaskLeaveToAdmins(
        taskId: String,
        taskTitle: String,
        ownerId: String,
        participants: List<TaskParticipant>,
        leavingUserId: String,
        leavingUserName: String
    ) {
        val adminIds = buildSet {
            if (ownerId.isNotBlank()) add(ownerId)
            participants
                .filter { it.role.equals("ADMIN", ignoreCase = true) && it.userId.isNotBlank() }
                .forEach { add(it.userId) }
        }.minus(leavingUserId)

        adminIds.forEach { adminId ->
            NotificationRepository.createNotification(
                userId = adminId,
                type = "task_member_left",
                message = "O integrante \"$leavingUserName\" saiu da tarefa \"$taskTitle\".",
                taskId = taskId,
                taskTitle = taskTitle
            )
        }
    }
}
