package com.presentation.wearclicker.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.presentation.wearclicker.AppConfig
import com.presentation.wearclicker.gesture.WristGestureDetector
import com.presentation.wearclicker.network.ConnectionState
import com.presentation.wearclicker.network.PresentationWebSocketClient
import com.presentation.wearclicker.util.HapticHelper
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

    private val _gesturesEnabled = MutableStateFlow(
        prefs.getBoolean(AppConfig.KEY_GESTURES_ENABLED, AppConfig.DEFAULT_GESTURES_ENABLED)
    )
    val gesturesEnabled: StateFlow<Boolean> = _gesturesEnabled.asStateFlow()

    private val _isConfigDialogOpen = MutableStateFlow(false)
    val isConfigDialogOpen: StateFlow<Boolean> = _isConfigDialogOpen.asStateFlow()

    private val wsClient = PresentationWebSocketClient(
        serverUrl = AppConfig.buildWebSocketUrl(_laptopIp.value)
    )

    val connectionState: StateFlow<ConnectionState> = wsClient.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    // Wrist gesture recognizer for hardware IMU on Samsung Galaxy Watch 4
    private val gestureDetector = WristGestureDetector(
        context = application,
        onGestureNext = {
            viewModelScope.launch {
                handleGestureNext()
            }
        },
        onGesturePrev = {
            viewModelScope.launch {
                handleGesturePrev()
            }
        }
    )

    init {
        wsClient.connect()
        if (_gesturesEnabled.value) {
            gestureDetector.start()
        }
    }

    /**
     * Triggered on verified Single Tap -> Advance to Next Slide silently without on-screen text.
     */
    fun onSingleTap() {
        HapticHelper.performSingleTapFeedback(getApplication())
        viewModelScope.launch {
            wsClient.sendCommand("NEXT")
        }
    }

    /**
     * Triggered on verified Double Tap -> Return to Previous Slide silently without on-screen text.
     */
    fun onDoubleTap() {
        HapticHelper.performDoubleTapFeedback(getApplication())
        viewModelScope.launch {
            wsClient.sendCommand("PREV")
        }
    }

    /**
     * Triggered via Outward Wrist Flick (Gesture Next).
     */
    private fun handleGestureNext() {
        HapticHelper.performSingleTapFeedback(getApplication())
        viewModelScope.launch {
            wsClient.sendCommand("NEXT")
        }
    }

    /**
     * Triggered via Inward Wrist Flick (Gesture Prev).
     */
    private fun handleGesturePrev() {
        HapticHelper.performDoubleTapFeedback(getApplication())
        viewModelScope.launch {
            wsClient.sendCommand("PREV")
        }
    }

    /**
     * Update laptop IP and gesture toggle preference, persist them, and apply changes.
     */
    fun updateSettings(newIp: String, enableGestures: Boolean) {
        val trimmed = newIp.trim()
        val editor = prefs.edit()

        if (trimmed.isNotBlank()) {
            editor.putString(AppConfig.KEY_LAPTOP_IP, trimmed)
            _laptopIp.value = trimmed
            val newUrl = AppConfig.buildWebSocketUrl(trimmed)
            wsClient.updateServerUrl(newUrl)
        }

        editor.putBoolean(AppConfig.KEY_GESTURES_ENABLED, enableGestures)
        editor.apply()
        _gesturesEnabled.value = enableGestures

        if (enableGestures) {
            gestureDetector.start()
        } else {
            gestureDetector.stop()
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

    override fun onCleared() {
        super.onCleared()
        gestureDetector.stop()
        wsClient.disconnect()
    }
}
