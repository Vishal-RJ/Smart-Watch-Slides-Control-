package com.presentation.wearclicker.gesture

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log

/**
 * High-accuracy, low-latency wrist gesture detector for Wear OS (Samsung Galaxy Watch 4).
 *
 * Utilizes the onboard Gyroscope and Accelerometer on a dedicated background HandlerThread
 * to recognize rapid wrist flicks / forearm twists without blocking the main UI thread.
 *
 * - Flick / Twist Outward (Roll/Yaw > Threshold) -> Next Slide
 * - Flick / Twist Inward  (Roll/Yaw < -Threshold) -> Previous Slide
 * - Features an 850ms cooldown window to prevent return-swing false triggers.
 */
class WristGestureDetector(
    private val context: Context,
    private val onGestureNext: () -> Unit,
    private val onGesturePrev: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null

    @Volatile
    private var isListening = false

    // Timestamp of the last triggered gesture to enforce a cooldown
    private var lastTriggerTimeMs: Long = 0

    companion object {
        private const val TAG = "WristGestureDetector"

        // Angular velocity threshold in rad/s (~260 degrees/second)
        // Calibrated for deliberate wrist flick/twist gestures while ignoring casual arm movements
        private const val GYRO_FLICK_THRESHOLD = 4.2f

        // Cooldown period in milliseconds after a gesture to prevent recoil/return swing triggers
        private const val GESTURE_COOLDOWN_MS = 850L
    }

    /**
     * Checks if the device has the necessary sensors for gesture recognition.
     */
    fun isAvailable(): Boolean {
        return gyroscope != null || accelerometer != null
    }

    /**
     * Starts listening for wrist motion gestures on a dedicated background thread.
     */
    @Synchronized
    fun start() {
        if (isListening || sensorManager == null) return

        try {
            // Spin up a dedicated background thread for high-frequency sensor events
            val thread = HandlerThread("WristGestureSensorThread").apply { start() }
            val handler = Handler(thread.looper)

            sensorThread = thread
            sensorHandler = handler

            // Register gyroscope for angular rotation speed
            gyroscope?.let { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME,
                    handler
                )
            }

            // Register accelerometer for lateral motion validation
            accelerometer?.let { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME,
                    handler
                )
            }

            isListening = true
            Log.i(TAG, "Wrist gesture detector started successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start wrist gesture detector", e)
        }
    }

    /**
     * Stops sensor listening and terminates background threads to preserve battery.
     */
    @Synchronized
    fun stop() {
        if (!isListening) return

        try {
            sensorManager?.unregisterListener(this)
            sensorThread?.quitSafely()
            sensorThread = null
            sensorHandler = null
            isListening = false
            Log.i(TAG, "Wrist gesture detector stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping wrist gesture detector", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isListening) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastTriggerTimeMs < GESTURE_COOLDOWN_MS) {
            // Still in cooldown window, ignore sensor recoil
            return
        }

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                // Gyroscope values: [0] = rad/s around X, [1] = rad/s around Y (forearm axis), [2] = rad/s around Z (screen normal)
                val gyroX = event.values[0]
                val gyroY = event.values[1]
                val gyroZ = event.values[2]

                // Forearm roll / wrist flick motion is captured predominantly in Y and Z axes
                // Outward twist (clockwise for left wrist) produces positive Y/Z angular velocity
                // Inward twist (counter-clockwise for left wrist) produces negative Y/Z angular velocity
                val primaryRoll = gyroY
                val primaryYaw = gyroZ

                // Check for Outward Flick (Next Slide)
                if (primaryRoll > GYRO_FLICK_THRESHOLD || primaryYaw > GYRO_FLICK_THRESHOLD) {
                    lastTriggerTimeMs = now
                    Log.d(TAG, "Gesture Detected: OUTWARD FLICK -> NEXT (gyroY=$gyroY, gyroZ=$gyroZ)")
                    onGestureNext()
                }
                // Check for Inward Flick (Previous Slide)
                else if (primaryRoll < -GYRO_FLICK_THRESHOLD || primaryYaw < -GYRO_FLICK_THRESHOLD) {
                    lastTriggerTimeMs = now
                    Log.d(TAG, "Gesture Detected: INWARD FLICK -> PREV (gyroY=$gyroY, gyroZ=$gyroZ)")
                    onGesturePrev()
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Accelerometer data available for auxiliary motion filtering if needed
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
