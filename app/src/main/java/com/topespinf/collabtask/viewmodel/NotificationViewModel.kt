package com.topespinf.collabtask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topespinf.collabtask.model.AppNotification
import com.topespinf.collabtask.repositories.NotificationRepository
import com.topespinf.collabtask.repositories.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _markingRead = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            SessionRepository.currentUser.collectLatest { user ->
                if (user == null) {
                    _notifications.value = emptyList()
                    _isLoading.value = false
                    _errorMessage.value = null
                    return@collectLatest
                }
                NotificationRepository.observeNotifications(user.id)
                    .onStart {
                        _isLoading.value = true
                    }
                    .catch { error ->
                        _errorMessage.value = error.message ?: "Não foi possível carregar as notificações."
                        _isLoading.value = false
                    }
                    .collectLatest { items ->
                        _notifications.value = items
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
            }
        }
    }

    fun markAllAsRead() {
        if (_markingRead.value) {
            return
        }
        val currentUser = SessionRepository.currentUser.value ?: return
        val unreadIds = _notifications.value
            .filter { !it.read }
            .map { it.id }
            .filter { it.isNotBlank() }
        if (unreadIds.isEmpty()) {
            return
        }
        viewModelScope.launch {
            _markingRead.value = true
            runCatching {
                NotificationRepository.markNotificationsAsRead(currentUser.id, unreadIds)
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Não foi possível atualizar notificações."
            }
            _markingRead.value = false
        }
    }
}
