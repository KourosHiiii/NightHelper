package com.nighthelper.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nighthelper.app.MainActivity
import com.nighthelper.app.R
import com.nighthelper.app.data.ChecklistItem
import com.nighthelper.app.data.ItemState
import com.nighthelper.app.data.NightRepository
import com.nighthelper.app.util.toFa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NightWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                NightRepository(context).checkDailyReset()
                pushAll(context, manager)
            }
            result.finish()
        }
    }

    companion object {
        suspend fun pushAll(context: Context, manager: AppWidgetManager) {
            val repo = NightRepository(context)
            val items: List<ChecklistItem> = repo.items.first()
            val states: Map<String, ItemState> = repo.states.first()
            val done = items.count { states[it.id]?.done == true }
            val total = items.size

            val progressText = when {
                total == 0 -> ""
                done == total -> context.getString(R.string.widget_all_done)
                else -> context.getString(
                    R.string.widget_progress_fmt,
                    done.toFa(),
                    total.toFa()
                )
            }

            val launch = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_night).apply {
                setOnClickPendingIntent(R.id.widget_root, launch)
                setOnClickPendingIntent(R.id.widget_action, launch)
                setTextViewText(R.id.widget_progress, progressText)
            }

            val ids = manager.getAppWidgetIds(
                ComponentName(context, NightWidgetReceiver::class.java)
            )
            if (ids.isNotEmpty()) {
                manager.updateAll(ids, views)
            }
        }

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            runBlocking {
                runCatching {
                    NightRepository(context).checkDailyReset()
                    pushAll(context, manager)
                }
            }
        }
    }
}
