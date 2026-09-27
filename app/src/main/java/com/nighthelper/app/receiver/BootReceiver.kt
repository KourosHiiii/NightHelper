package com.nighthelper.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nighthelper.app.data.NightRepository
import com.nighthelper.app.service.FlipService
import com.nighthelper.app.widget.NightWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val settings = runBlocking { NightRepository(context).settings.first() }
        if (settings.flipGestureEnabled || settings.persistentNotificationEnabled) {
            FlipService.start(context)
        }
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                NightRepository(context).checkDailyReset()
                NightWidgetReceiver.refresh(context)
            }
            result.finish()
        }
    }
}
