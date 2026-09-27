package com.nighthelper.app.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects a deliberate ~180° flip of the phone (face-up <-> face-down).
 *
 * Rules to avoid accidental triggers:
 * - |z| must clearly cross +-7.5 m/s^2 (real flip, not pocket noise)
 * - total magnitude must stay between 8 and 13 (not being shaken)
 * - new orientation must be stable for STABLE_MS before committing
 * - cooldown between triggers
 */
class FlipGestureDetector(
    private val onFlip: () -> Unit
) : SensorEventListener {

    private enum class Face { UP, DOWN }

    private var currentFace: Face? = null
    private var pendingFace: Face? = null
    private var pendingSince = 0L
    private var downSeenAt = 0L
    private var lastTriggerAt = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        val e = event ?: return
        if (e.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val x = e.values[0]
        val y = e.values[1]
        val z = e.values[2]
        val total = sqrt((x * x + y * y + z * z).toDouble())
        if (total < 8.0 || total > 13.0) return

        val now = System.currentTimeMillis()
        val face = when {
            z > 7.5f -> Face.UP
            z < -7.5f -> Face.DOWN
            else -> null
        } ?: return

        if (face != pendingFace) {
            pendingFace = face
            pendingSince = now
            return
        }
        if (now - pendingSince < STABLE_MS) return
        if (face == currentFace) return

        val previous = currentFace
        currentFace = face
        when (previous) {
            Face.UP -> if (face == Face.DOWN) {
                downSeenAt = now
                tryTrigger(now)
            }
            Face.DOWN -> if (now - downSeenAt in 1..RETURN_WINDOW_MS) {
                tryTrigger(now)
            }
            null -> if (face == Face.DOWN) downSeenAt = now
        }
    }

    private fun tryTrigger(now: Long) {
        if (now - lastTriggerAt < COOLDOWN_MS) return
        lastTriggerAt = now
        onFlip()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val STABLE_MS = 120L
        private const val COOLDOWN_MS = 3000L
        private const val RETURN_WINDOW_MS = 4000L
    }
}
