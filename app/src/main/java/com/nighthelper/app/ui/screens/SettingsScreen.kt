package com.nighthelper.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nighthelper.app.MainViewModel
import com.nighthelper.app.R
import com.nighthelper.app.data.ChecklistItem
import com.nighthelper.app.data.ItemType
import com.nighthelper.app.ui.components.SvgIcon
import com.nighthelper.app.ui.components.availableIconKeys
import com.nighthelper.app.ui.components.iconRes
import com.nighthelper.app.ui.components.rememberHaptics
import com.nighthelper.app.ui.theme.Hairline
import com.nighthelper.app.ui.theme.Lavender
import com.nighthelper.app.ui.theme.MoonGold
import com.nighthelper.app.ui.theme.TextDim
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val context = LocalContext.current
    val items by vm.items.collectAsState()
    val settings by vm.settings.collectAsState()
    val haptic = rememberHaptics(settings.hapticsEnabled)

    var editTarget by remember { mutableStateOf<ChecklistItem?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ChecklistItem?>(null) }
    var showRestore by remember { mutableStateOf(false) }

    val notifPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.updateSettings { it.copy(persistentNotificationEnabled = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = null,
                            tint = Lavender
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                IconButton(onClick = onOpenHistory) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = null,
                        tint = TextDim
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(6.dp))

        TriggersSection(
            vm = vm,
            settings = settings,
            onEnableNotifications = {
                if (Build.VERSION.SDK_INT >= 33) {
                    notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    vm.updateSettings { it.copy(persistentNotificationEnabled = true) }
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        ItemsSection(
            items = items,
            onAdd = { showAdd = true },
            onEdit = { editTarget = it },
            onDelete = { deleteTarget = it },
            onRestore = { showRestore = true }
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.sec_about),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SvgIcon(
                        key = "moon",
                        modifier = Modifier.size(20.dp),
                        tint = MoonGold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.about_text),
                        fontSize = 14.sp,
                        color = TextDim
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }

    if (showAdd || editTarget != null) {
        ItemEditDialog(
            initial = editTarget,
            nextOrder = items.size,
            onDismiss = {
                showAdd = false
                editTarget = null
            },
            onSave = { item ->
                haptic()
                vm.upsertItem(item)
                showAdd = false
                editTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.confirm_delete)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteItem(target.id)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showRestore) {
        AlertDialog(
            onDismissRequest = { showRestore = false },
            title = { Text(stringResource(R.string.btn_restore)) },
            text = { Text(stringResource(R.string.confirm_restore)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.restoreDefaults()
                    showRestore = false
                }) {
                    Text(stringResource(R.string.btn_restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestore = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun TriggersSection(
    vm: MainViewModel,
    settings: com.nighthelper.app.data.SettingsState,
    onEnableNotifications: () -> Unit
) {
    val context = LocalContext.current
    val overlayGranted = Settings.canDrawOverlays(context)

    com.nighthelper.app.ui.components.SectionCard(
        title = stringResource(R.string.sec_triggers),
        iconKey = "flip"
    ) {
        ToggleRow(
            iconKey = "flip",
            title = stringResource(R.string.trigger_flip),
            subtitle = stringResource(R.string.trigger_flip_desc),
            checked = settings.flipGestureEnabled,
            onChecked = { enabled ->
                vm.updateSettings { it.copy(flipGestureEnabled = enabled) }
            }
        )
        ToggleRow(
            iconKey = "bell",
            title = stringResource(R.string.trigger_notif),
            subtitle = stringResource(R.string.trigger_notif_desc),
            checked = settings.persistentNotificationEnabled,
            onChecked = { enabled ->
                if (enabled) {
                    onEnableNotifications()
                } else {
                    vm.updateSettings { it.copy(persistentNotificationEnabled = false) }
                }
            }
        )
        ToggleRow(
            iconKey = "sparkle",
            title = stringResource(R.string.trigger_haptic),
            subtitle = stringResource(R.string.trigger_haptic_desc),
            checked = settings.hapticsEnabled,
            onChecked = { enabled ->
                vm.updateSettings { it.copy(hapticsEnabled = enabled) }
            }
        )

        if (settings.flipGestureEnabled && !overlayGranted) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MoonGold.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, MoonGold.copy(alpha = 0.25f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.overlay_needed),
                        fontSize = 13.sp,
                        color = TextDim
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            runCatching { context.startActivity(intent) }
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.btn_overlay),
                            color = MoonGold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    iconKey: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Lavender.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            SvgIcon(key = iconKey, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextDim
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Lavender,
                checkedThumbColor = com.nighthelper.app.ui.theme.LavenderDeep
            )
        )
    }
}

@Composable
private fun ItemsSection(
    items: List<ChecklistItem>,
    onAdd: () -> Unit,
    onEdit: (ChecklistItem) -> Unit,
    onDelete: (ChecklistItem) -> Unit,
    onRestore: () -> Unit
) {
    com.nighthelper.app.ui.components.SectionCard(
        title = stringResource(R.string.sec_items),
        iconKey = "widget"
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Lavender.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SvgIcon(key = item.iconKey, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (item.type == ItemType.TEXT) {
                            stringResource(R.string.item_type_text)
                        } else {
                            stringResource(R.string.item_type_yesno)
                        },
                        fontSize = 12.sp,
                        color = TextDim
                    )
                }
                IconButton(onClick = { onEdit(item) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = null,
                        tint = TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = { onDelete(item) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_add_item),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRestore) {
            Text(
                text = stringResource(R.string.btn_restore),
                color = TextDim,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ItemEditDialog(
    initial: ChecklistItem?,
    nextOrder: Int,
    onDismiss: () -> Unit,
    onSave: (ChecklistItem) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var isTextType by remember {
        mutableStateOf(initial?.type == ItemType.TEXT)
    }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: "star") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) {
                    stringResource(R.string.dialog_add_title)
                } else {
                    stringResource(R.string.dialog_edit_title)
                }
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_label)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isTextType,
                        onClick = { isTextType = false },
                        label = { Text(stringResource(R.string.item_type_yesno), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = isTextType,
                        onClick = { isTextType = true },
                        label = { Text(stringResource(R.string.item_type_text), fontSize = 12.sp) }
                    )
                }
                Spacer(Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(availableIconKeys) { key ->
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (key == iconKey) Lavender.copy(alpha = 0.2f)
                                    else Lavender.copy(alpha = 0.06f),
                                    CircleShape
                                )
                                .border(
                                    width = if (key == iconKey) 2.dp else 1.dp,
                                    color = if (key == iconKey) Lavender else Hairline,
                                    shape = CircleShape
                                )
                                .clickable { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            SvgIcon(key = key, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ChecklistItem(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            subtitle = initial?.subtitle ?: "",
                            iconKey = iconKey,
                            type = if (isTextType) ItemType.TEXT else ItemType.YES_NO,
                            isBuiltIn = initial?.isBuiltIn ?: false,
                            order = initial?.order ?: nextOrder
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_save_text), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
