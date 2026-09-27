package com.nighthelper.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nighthelper.app.MainActivity
import com.nighthelper.app.R
import com.nighthelper.app.data.NightRepository
import com.nighthelper.app.data.SettingsState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class FlipService : Service() {

    private lateinit var sensorManager: SensorManager
    private var detector: FlipGestureDetector? = null
    private var sensorRegistered = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val settings = runBlocking { NightRepository(this@FlipService).settings.first() }
        if (!settings.flipGestureEnabled && !settings.persistentNotificationEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification(settings)
        if (Build.VERSION.SDK_INT >= 30) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        updateSensorListener(settings)
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterSensor()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateSensorListener(settings: SettingsState) {
        if (settings.flipGestureEnabled) {
            registerSensor()
        } else {
            unregisterSensor()
        }
    }

    private fun registerSensor() {
        if (sensorRegistered) return
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        if (detector == null) {
            detector = FlipGestureDetector { onFlipDetected() }
        }
        detector?.let {
            sensorManager.registerListener(it, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorRegistered = true
        }
    }

    private fun unregisterSensor() {
        detector?.let { sensorManager.unregisterListener(it) }
        sensorRegistered = false
    }

    private fun onFlipDetected() {
        val launch = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        try {
            startActivity(launch)
        } catch (e: Exception) {
            showFlipFallbackNotification()
        }
    }

    private fun createChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            getString(R.string.notif_channel_service),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = getString(R.string.notif_channel_service_desc)
            setShowBadge(false)
        }
        val nightChannel = NotificationChannel(
            CHANNEL_NIGHT,
            getString(R.string.notif_channel_night),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notif_channel_night_desc)
        }
        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(nightChannel)
    }

    private fun openAppPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun openSettingsPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_SETTINGS,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(MainActivity.EXTRA_ROUTE, "settings")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun buildNotification(settings: SettingsState): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle(getString(R.string.notif_service_title))
            .setContentText(getString(R.string.notif_service_text))
            .setContentIntent(openAppPendingIntent())
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
        if (settings.persistentNotificationEnabled) {
            builder.addAction(0, getString(R.string.notif_action_start), openAppPendingIntent())
            builder.addAction(0, getString(R.string.notif_action_settings), openSettingsPendingIntent())
        }
        return builder.build()
    }

    /**
     * When background activity launch is blocked (Android 10+ without overlay
     * permission), fall back to a heads-up notification the user can tap.
     */
    private fun showFlipFallbackNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_NIGHT)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle(getString(R.string.notif_flip_title))
            .setContentText(getString(R.string.notif_flip_text))
            .setContentIntent(openAppPendingIntent())
            .setFullScreenIntent(openAppPendingIntent(), true)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        runCatching { manager.notify(NOTIFICATION_FLIP_ID, notification) }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_FLIP_ID = 1002
        private const val REQUEST_OPEN = 10
        private const val REQUEST_SETTINGS = 11
        private const val CHANNEL_SERVICE = "night_service"
        private const val CHANNEL_NIGHT = "night_alerts"

        fun start(context: Context) {
            val intent = Intent(context, FlipService::class.java)
            runCatching {
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FlipService::class.java))
        }
    }
}
