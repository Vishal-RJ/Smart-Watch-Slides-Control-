package com.presentation.wearclicker.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.presentation.wearclicker.AppConfig
import com.presentation.wearclicker.network.ConnectionState
import com.presentation.wearclicker.network.PresentationWebSocketClient
import com.presentation.wearclicker.util.HapticHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PresentationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(AppConfig.PREFS_NAME, Context.MODE_PRIVATE)

    private val _laptopIp = MutableStateFlow(
        prefs.getString(AppConfig.KEY_LAPTOP_IP, null)?.let { saved ->
            if (saved == "127.0.0.1" && AppConfig.DEFAULT_LAPTOP_IP != "127.0.0.1") {
                AppConfig.DEFAULT_LAPTOP_IP
            } else saved
        } ?: AppConfig.DEFAULT_LAPTOP_IP
    )
    val laptopIp: StateFlow<String> = _laptopIp.asStateFlow()

    private val _isConfigDialogOpen = MutableStateFlow(false)
    val isConfigDialogOpen: StateFlow<Boolean> = _isConfigDialogOpen.asStateFlow()

    // Transient action hint ("NEXT", "PREV", or null) for brief HUD feedback
    private val _lastAction = MutableStateFlow<String?>(null)
    val lastAction: StateFlow<String?> = _lastAction.asStateFlow()
    private var clearActionJob: Job? = null

    private val wsClient = PresentationWebSocketClient(
        serverUrl = AppConfig.buildWebSocketUrl(_laptopIp.value)
    )

    val connectionState: StateFlow<ConnectionState> = wsClient.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    init {
        wsClient.connect()
    }

    /**
     * Triggered on verified Single Tap -> Advance to Next Slide.
     */
    fun onSingleTap() {
        HapticHelper.performSingleTapFeedback(getApplication())
        flashAction("NEXT")
        viewModelScope.launch {
            wsClient.sendCommand("NEXT")
        }
    }

    /**
     * Triggered on verified Double Tap -> Return to Previous Slide.
     */
    fun onDoubleTap() {
        HapticHelper.performDoubleTapFeedback(getApplication())
        flashAction("PREV")
        viewModelScope.launch {
            wsClient.sendCommand("PREV")
        }
    }

    /**
     * Reconnect to the server manually.
     */
    fun onReconnect() {
        HapticHelper.performSingleTapFeedback(getApplication())
        wsClient.connect()
    }

    /**
     * Update laptop IP, persist it, and reconnect.
     */
    fun updateLaptopIp(newIp: String) {
        val trimmed = newIp.trim()
        if (trimmed.isNotBlank()) {
            prefs.edit().putString(AppConfig.KEY_LAPTOP_IP, trimmed).apply()
            _laptopIp.value = trimmed
            val newUrl = AppConfig.buildWebSocketUrl(trimmed)
            wsClient.updateServerUrl(newUrl)
        }
        _isConfigDialogOpen.value = false
    }

    fun openConfigDialog() {
        HapticHelper.performLongPressFeedback(getApplication())
        _isConfigDialogOpen.value = true
    }

    fun closeConfigDialog() {
        _isConfigDialogOpen.value = false
    }

    private fun flashAction(action: String) {
        _lastAction.value = action
        clearActionJob?.cancel()
        clearActionJob = viewModelScope.launch {
            delay(500)
            _lastAction.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
    }
}
