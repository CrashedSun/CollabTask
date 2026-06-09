package com.topespinf.collabtask.repositories

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.topespinf.collabtask.model.AppNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal data class FirestoreNotificationRecord(
    val type: String = "",
    val message: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val createdAt: Timestamp = Timestamp(0, 0),
    val read: Boolean = false
)

object NotificationRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val registration = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val record = document.toObject(FirestoreNotificationRecord::class.java) ?: return@mapNotNull null
                    AppNotification(
                        id = document.id,
                        type = record.type,
                        message = record.message,
                        taskId = record.taskId,
                        taskTitle = record.taskTitle,
                        createdAt = record.createdAt,
                        read = record.read
                    )
                }
                trySend(notifications)
            }

        awaitClose { registration.remove() }
    }

    suspend fun createNotification(
        userId: String,
        type: String,
        message: String,
        taskId: String,
        taskTitle: String
    ) {
        if (userId.isBlank()) {
            return
        }
        val document = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .document()
        document.set(
            FirestoreNotificationRecord(
                type = type,
                message = message,
                taskId = taskId,
                taskTitle = taskTitle,
                createdAt = Timestamp.now(),
                read = false
            )
        ).await()
    }

    suspend fun markNotificationsAsRead(userId: String, notificationIds: List<String>) {
        if (userId.isBlank() || notificationIds.isEmpty()) {
            return
        }
        val batch = firestore.batch()
        notificationIds.forEach { notificationId ->
            val document = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
            batch.update(document, "read", true)
        }
        batch.commit().await()
    }
}
