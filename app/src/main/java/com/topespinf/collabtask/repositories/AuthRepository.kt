package com.topespinf.collabtask.repositories

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.topespinf.collabtask.model.User
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import kotlin.random.Random

object AuthRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    suspend fun login(email: String, password: String): User {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = auth.currentUser ?: error("Usuário autenticado não encontrado.")
        return upsertUserProfile(firebaseUser, fallbackName = firebaseUser.displayName)
    }

    suspend fun register(name: String, email: String, password: String): User {
        val sanitizedName = name.trim()
        val sanitizedEmail = email.trim()
        auth.createUserWithEmailAndPassword(sanitizedEmail, password).await()
        val firebaseUser = auth.currentUser ?: error("Usuário autenticado não encontrado.")
        firebaseUser.updateProfile(userProfileChangeRequest {
            displayName = sanitizedName
        }).await()
        return upsertUserProfile(firebaseUser, fallbackName = sanitizedName)
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): User {
        val idToken = account.idToken ?: error("Conta Google sem token de autenticação.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        val firebaseUser = auth.currentUser ?: error("Usuário autenticado não encontrado.")
        return upsertUserProfile(firebaseUser, fallbackName = firebaseUser.displayName)
    }

    suspend fun sendPasswordReset(email: String) {
        val sanitizedEmail = email.trim()
        require(sanitizedEmail.isNotBlank()) { "Informe um e-mail válido." }
        val directMatch = firestore.collection("users")
            .whereEqualTo("email", sanitizedEmail)
            .limit(1)
            .get()
            .await()
            .isEmpty
            .not()
        val lowercaseEmail = sanitizedEmail.lowercase()
        val lowercaseMatch = if (lowercaseEmail != sanitizedEmail) {
            firestore.collection("users")
                .whereEqualTo("email", lowercaseEmail)
                .limit(1)
                .get()
                .await()
                .isEmpty
                .not()
        } else {
            false
        }
        require(directMatch || lowercaseMatch) { "Nenhuma conta encontrada com este e-mail." }
        auth.sendPasswordResetEmail(sanitizedEmail).await()
    }

    suspend fun updateDisplayName(newName: String): User {
        val sanitizedName = newName.trim()
        require(sanitizedName.isNotBlank()) { "Nome não pode estar vazio." }

        val firebaseUser = auth.currentUser ?: error("Usuário autenticado não encontrado.")
        firebaseUser.updateProfile(userProfileChangeRequest {
            displayName = sanitizedName
        }).await()

        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(mapOf("name" to sanitizedName), SetOptions.merge())
            .await()

        val current = SessionRepository.currentUser.value
        val updatedUser = User(
            id = firebaseUser.uid,
            name = sanitizedName,
            email = current?.email.orEmpty().ifBlank { firebaseUser.email.orEmpty() },
            user = current?.user.orEmpty(),
            role = current?.role.orEmpty().ifBlank { "Membro" }
        )
        SessionRepository.setCurrentUser(updatedUser)
        return updatedUser
    }

    suspend fun deleteCurrentAccount(confirmationName: String) {
        val sessionUser = SessionRepository.currentUser.value ?: error("Faça login para excluir sua conta.")
        val firebaseUser = auth.currentUser ?: error("Usuário autenticado não encontrado.")
        val expectedName = sessionUser.name.trim()
        val typedName = confirmationName.trim()
        require(typedName.isNotBlank()) { "Digite seu nome completo para confirmar a exclusão." }
        require(expectedName.equals(typedName, ignoreCase = true)) {
            "Nome de confirmação inválido. Digite exatamente seu nome completo."
        }

        runCatching { removeUserFromTasks(sessionUser.id) }
        runCatching { deleteUserSubcollection(sessionUser.id, "notifications") }
        runCatching { deleteUserSubcollection(sessionUser.id, "notificationTokens") }
        runCatching { firestore.collection("users").document(sessionUser.id).delete().await() }
        firebaseUser.delete().await()
        SessionRepository.clear()
    }

    fun logout() {
        SessionRepository.clear()
    }

    fun currentUser(): User? {
        return SessionRepository.currentUser.value
    }

    suspend fun refreshCurrentUserProfile() {
        val firebaseUser = auth.currentUser ?: return
        upsertUserProfile(firebaseUser, fallbackName = firebaseUser.displayName)
    }

    private suspend fun upsertUserProfile(
        firebaseUser: FirebaseUser,
        fallbackName: String?
    ): User {
        val docRef = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = docRef.get().await()
        val stored = snapshot.toObject(FirestoreUserRecord::class.java)
        val resolvedName = stored?.name.orEmpty().ifBlank {
            fallbackName.orEmpty().ifBlank { firebaseUser.email.orEmpty() }
        }
        val resolvedUserHandle = stored?.user.orEmpty().ifBlank {
            generateUniqueUserHandle(resolvedName)
        }
        val user = User(
            id = firebaseUser.uid,
            name = resolvedName,
            email = stored?.email.orEmpty().ifBlank { firebaseUser.email.orEmpty() },
            user = resolvedUserHandle,
            role = stored?.role.orEmpty().ifBlank { "Membro" }
        )
        docRef.set(user.toFirestoreRecord(), SetOptions.merge()).await()
        SessionRepository.setCurrentUser(user)
        return user
    }

    private suspend fun generateUniqueUserHandle(displayName: String): String {
        val base = normalizeUserBase(displayName)
        repeat(200) {
            val suffix = Random.nextInt(0, 10_000)
            val candidate = "$base#${suffix.toString().padStart(4, '0')}"
            val existing = firestore.collection("users")
                .whereEqualTo("user", candidate)
                .limit(1)
                .get()
                .await()
            if (existing.isEmpty) {
                return candidate
            }
        }
        error("Não foi possível gerar um identificador de usuário único.")
    }

    private suspend fun removeUserFromTasks(userId: String) {
        val taskSnapshots = firestore.collection("tasks").get().await()
        taskSnapshots.documents.forEach { document ->
            val task = document.toObject(FirestoreTaskRecord::class.java)?.toTask(document.id) ?: return@forEach
            if (task.ownerId == userId) {
                document.reference.delete().await()
                return@forEach
            }

            val updatedParticipants = task.participants.filterNot { participant ->
                participant.userId == userId
            }
            if (updatedParticipants.size == task.participants.size) {
                return@forEach
            }

            val updatedTask = task.copy(
                participants = updatedParticipants,
                updatedAt = Timestamp.now()
            )
            document.reference.set(updatedTask.toFirestoreRecord(), SetOptions.merge()).await()
        }
    }

    private suspend fun deleteUserSubcollection(userId: String, subcollection: String) {
        val snapshots = firestore.collection("users")
            .document(userId)
            .collection(subcollection)
            .get()
            .await()
        snapshots.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }

    private fun normalizeUserBase(value: String): String {
        val trimmed = value.trim().ifBlank { "usuario" }
        val decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
        val withoutAccents = decomposed.replace("\\p{Mn}+".toRegex(), "")
        val collapsed = withoutAccents
            .lowercase()
            .replace("\\s+".toRegex(), "")
            .replace("[^a-z0-9]".toRegex(), "")
        return collapsed.ifBlank { "usuario" }
    }
}
