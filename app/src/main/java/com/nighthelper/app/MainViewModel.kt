package com.nighthelper.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nighthelper.app.data.ChecklistItem
import com.nighthelper.app.data.DayRecord
import com.nighthelper.app.data.DefaultItems
import com.nighthelper.app.data.ItemState
import com.nighthelper.app.data.NightRepository
import com.nighthelper.app.data.SettingsState
import com.nighthelper.app.service.FlipService
import com.nighthelper.app.widget.NightWidgetReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = NightRepository(application)

    val items: StateFlow<List<ChecklistItem>> = repo.items
        .stateIn(viewModelScope, SharingStarted.Eagerly, DefaultItems.create())

    val states: StateFlow<Map<String, ItemState>> = repo.states
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val history: StateFlow<List<DayRecord>> = repo.history
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val settings: StateFlow<SettingsState> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())

    init {
        viewModelScope.launch {
            repo.checkDailyReset()
            syncService(repo.settings.first())
        }
    }

    fun onAppOpen() {
        viewModelScope.launch {
            repo.checkDailyReset()
            syncService(repo.settings.first())
        }
    }

    fun answer(item: ChecklistItem, done: Boolean) {
        viewModelScope.launch {
            val text = states.value[item.id]?.textValue ?: ""
            repo.setItemState(item.id, ItemState(done = done, textValue = text))
            refreshWidget()
        }
    }

    fun submitText(item: ChecklistItem, text: String) {
        viewModelScope.launch {
            repo.setItemState(item.id, ItemState(done = text.isNotBlank(), textValue = text))
            refreshWidget()
        }
    }

    fun restartTonight() {
        viewModelScope.launch {
            repo.resetStates()
            refreshWidget()
        }
    }

    fun closeNight() {
        viewModelScope.launch {
            val its = items.value
            val st = states.value
            val completed = its.filter { st[it.id]?.done == true }
            val missed = its.filter { st[it.id]?.done != true }
            val gratitude = its
                .firstOrNull { it.type == com.nighthelper.app.data.ItemType.TEXT }
                ?.let { st[it.id]?.textValue?.takeIf { text -> text.isNotBlank() } }
            repo.addHistory(
                DayRecord(
                    date = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                    totalItems = its.size,
                    completedItems = completed.size,
                    completedTitles = completed.map { it.title },
                    missedTitles = missed.map { it.title },
                    gratitude = gratitude,
                    closedAt = System.currentTimeMillis()
                )
            )
            refreshWidget()
        }
    }

    fun upsertItem(item: ChecklistItem) {
        viewModelScope.launch {
            val current = items.value.filter { it.id != item.id }
            val merged = (current + item).sortedBy { it.order }
            repo.saveItems(merged)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repo.saveItems(items.value.filter { it.id != id })
        }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            repo.saveItems(DefaultItems.create())
            repo.resetStates()
            refreshWidget()
        }
    }

    fun updateSettings(transform: (SettingsState) -> SettingsState) {
        viewModelScope.launch {
            val updated = transform(repo.settings.first())
            repo.saveSettings(updated)
            syncService(updated)
        }
    }

    private fun syncService(s: SettingsState) {
        val context = getApplication<Application>()
        if (s.flipGestureEnabled || s.persistentNotificationEnabled) {
            FlipService.start(context)
        } else {
            FlipService.stop(context)
        }
    }

    private fun refreshWidget() {
        val context = getApplication<Application>()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val manager = android.appwidget.AppWidgetManager.getInstance(context)
                NightWidgetReceiver.pushAll(context, manager)
            }
        }
    }
}
