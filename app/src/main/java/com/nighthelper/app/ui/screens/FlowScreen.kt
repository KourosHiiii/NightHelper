package com.nighthelper.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nighthelper.app.MainViewModel
import com.nighthelper.app.R
import com.nighthelper.app.data.ChecklistItem
import com.nighthelper.app.data.ItemState
import com.nighthelper.app.data.ItemType
import com.nighthelper.app.ui.components.SvgIcon
import com.nighthelper.app.ui.components.iconRes
import com.nighthelper.app.ui.components.rememberHaptics
import com.nighthelper.app.ui.theme.Hairline
import com.nighthelper.app.ui.theme.Lavender
import com.nighthelper.app.ui.theme.MoonGold
import com.nighthelper.app.ui.theme.TextDim
import com.nighthelper.app.util.toFa
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FlowScreen(
    vm: MainViewModel,
    onOpenSettings: () -> Unit,
    onGoToGoodNight: () -> Unit
) {
    val items by vm.items.collectAsState()
    val states by vm.states.collectAsState()
    val settings by vm.settings.collectAsState()
    val haptic = rememberHaptics(settings.hapticsEnabled)
    var skipped by remember { mutableStateOf(setOf<String>()) }

    val current = items.firstOrNull {
        states[it.id]?.done != true && it.id !in skipped
    }
    val doneCount = items.count { states[it.id]?.done == true }

    LaunchedEffect(current) {
        if (items.isNotEmpty() && current == null) {
            delay(1400)
            onGoToGoodNight()
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SvgIcon(
                        key = "moon",
                        modifier = Modifier.size(26.dp),
                        tint = MoonGold
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.flow_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(
                                R.string.flow_progress_fmt,
                                doneCount.toFa(),
                                items.size.toFa()
                            ),
                            fontSize = 12.sp,
                            color = TextDim
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    SvgIcon(
                        key = "settings",
                        modifier = Modifier.size(22.dp),
                        tint = TextDim
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        ProgressDots(
            items = items,
            states = states,
            currentId = current?.id,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        AnimatedContent(
            targetState = current,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(spring(dampingRatio = 0.75f, stiffness = 380f)) { it / 2 } +
                        fadeIn(spring(stiffness = 380f))) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut(spring(stiffness = 420f)))
                } else {
                    fadeIn(spring(stiffness = 380f)) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut(spring(stiffness = 420f)))
                }
            },
            label = "questionCard",
            modifier = Modifier.weight(1f)
        ) { item ->
            when {
                item != null -> QuestionCard(
                    item = item,
                    state = states[item.id] ?: ItemState(),
                    onDone = {
                        haptic()
                        vm.answer(item, true)
                    },
                    onSkip = { skipped = skipped + item.id },
                    onText = { text ->
                        haptic()
                        vm.submitText(item, text)
                    }
                )
                else -> AllDoneCard(onGoodNight = onGoToGoodNight)
            }
        }
    }
}

@Composable
private fun ProgressDots(
    items: List<ChecklistItem>,
    states: Map<String, ItemState>,
    currentId: String?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        items.forEach { item ->
            val state = states[item.id]
            val isDone = state?.done == true
            val isCurrent = item.id == currentId
            val color = when {
                isDone -> MoonGold
                isCurrent -> Lavender
                else -> Hairline
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(if (isCurrent) 12.dp else 9.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun QuestionCard(
    item: ChecklistItem,
    state: ItemState,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onText: (String) -> Unit
) {
    var textValue by remember(item.id) { mutableStateOf(state.textValue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Hairline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Lavender.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes(item.iconKey)),
                        contentDescription = null,
                        tint = Lavender,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = item.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (item.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.subtitle,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = TextDim
                    )
                }
                Spacer(Modifier.height(28.dp))
                when (item.type) {
                    ItemType.YES_NO -> {
                        Button(
                            onClick = onDone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_done),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null,
                                tint = TextDim,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_later),
                                fontSize = 16.sp,
                                color = TextDim
                            )
                        }
                    }
                    ItemType.TEXT -> {
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    stringResource(R.string.text_hint),
                                    color = TextDim,
                                    fontSize = 15.sp
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (textValue.isNotBlank()) onText(textValue.trim())
                            })
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onText(textValue.trim()) },
                            enabled = textValue.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_save_text),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        androidx.compose.material3.TextButton(onClick = onSkip) {
                            Text(
                                text = stringResource(R.string.btn_skip),
                                color = TextDim,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllDoneCard(onGoodNight: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(MoonGold.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sparkle),
                contentDescription = null,
                tint = MoonGold,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.all_done_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.all_done_sub),
            fontSize = 15.sp,
            color = TextDim
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onGoodNight,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_zzz),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.btn_goodnight),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
