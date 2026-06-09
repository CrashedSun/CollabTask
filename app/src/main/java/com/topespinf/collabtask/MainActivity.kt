package com.topespinf.collabtask

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.topespinf.collabtask.repositories.NotificationRepository
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.ui.CollabTaskApp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermissionIfNeeded()
        observeIncomingNotifications()
        setContent {
            CollabTaskApp()
        }
    }

    private fun observeIncomingNotifications() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                SessionRepository.currentUser.collectLatest { user ->
                    if (user == null) {
                        return@collectLatest
                    }
                    val prefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
                    val key = "last_dispatched_ms_${user.id}"
                    if (!prefs.contains(key)) {
                        prefs.edit().putLong(key, System.currentTimeMillis()).apply()
                    }
                    NotificationRepository.observeNotifications(user.id).collectLatest { notifications ->
                        dispatchSystemNotifications(
                            userId = user.id,
                            notifications = notifications
                        )
                    }
                }
            }
        }
    }

    private fun dispatchSystemNotifications(
        userId: String,
        notifications: List<com.topespinf.collabtask.model.AppNotification>
    ) {
        if (notifications.isEmpty()) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return
            }
        }

        val prefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
        val key = "last_dispatched_ms_$userId"
        val latestMillis = notifications.maxOfOrNull { it.createdAt.toDate().time } ?: return
        val lastDispatchedMillis = prefs.getLong(key, 0L)
        val newNotifications = notifications
            .filter { it.createdAt.toDate().time > lastDispatchedMillis }
            .sortedBy { it.createdAt.toDate().time }
        if (newNotifications.isEmpty()) {
            return
        }

        newNotifications.forEach { notification ->
            val systemNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("CollabTask")
                .setContentText(notification.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this).notify(notification.id.hashCode(), systemNotification)
        }
        prefs.edit().putLong(key, latestMillis).apply()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Notificações do CollabTask",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alertas sobre tarefas e milestones."
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "collabtask_events"
    }
}

