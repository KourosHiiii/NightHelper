package com.nighthelper.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nighthelper.app.MainViewModel
import com.nighthelper.app.R
import com.nighthelper.app.data.NightTips
import com.nighthelper.app.ui.components.SvgIcon
import com.nighthelper.app.ui.components.rememberHaptics
import com.nighthelper.app.ui.theme.Hairline
import com.nighthelper.app.ui.theme.Lavender
import com.nighthelper.app.ui.theme.MintSoft
import com.nighthelper.app.ui.theme.MoonGold
import com.nighthelper.app.ui.theme.TextDim
import com.nighthelper.app.util.toFa
import java.time.LocalDate

@Composable
fun GoodNightScreen(
    vm: MainViewModel,
    onOpenHistory: () -> Unit,
    onBack: () -> Unit
) {
    val items by vm.items.collectAsState()
    val states by vm.states.collectAsState()
    val settings by vm.settings.collectAsState()
    val haptic = rememberHaptics(settings.hapticsEnabled)
    var closed by remember { mutableStateOf(false) }

    val doneCount = items.count { states[it.id]?.done == true }
    val missed = items.filter { states[it.id]?.done != true }
    val gratitude = items
        .firstOrNull { it.type == com.nighthelper.app.data.ItemType.TEXT }
        ?.let { states[it.id]?.textValue?.takeIf { text -> text.isNotBlank() } }
    val tip = NightTips.all[LocalDate.now().dayOfWeek.value % NightTips.all.size]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlowingMoon()
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.goodnight_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.goodnight_sub_fmt,
                doneCount.toFa(),
                items.size.toFa()
            ),
            fontSize = 15.sp,
            color = TextDim,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        TipCard(
            iconKey = "sparkle",
            label = stringResource(R.string.tip_label),
            text = tip
        )

        gratitude?.let { note ->
            Spacer(Modifier.height(14.dp))
            TipCard(
                iconKey = "heart",
                label = stringResource(R.string.gratitude_label),
                text = note,
                accent = MintSoft
            )
        }

        if (missed.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SvgIcon(
                            key = "zzz",
                            modifier = Modifier.size(18.dp),
                            tint = TextDim
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.missed_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDim
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    missed.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Hairline.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                SvgIcon(
                                    key = item.iconKey,
                                    modifier = Modifier.size(17.dp),
                                    tint = TextDim
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                color = TextDim
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.missed_sub),
                        fontSize = 12.sp,
                        color = TextDim.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(26.dp))

        if (!closed) {
            Button(
                onClick = {
                    haptic()
                    vm.closeNight()
                    closed = true
                },
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
                    text = stringResource(R.string.btn_close_night),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = MintSoft,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.saved_chip),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintSoft
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = null,
                    tint = Lavender,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.btn_history),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Lavender
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        androidx.compose.material3.TextButton(onClick = onBack) {
            Text(
                text = stringResource(R.string.btn_back),
                color = TextDim,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun GlowingMoon() {
    val transition = rememberInfiniteTransition(label = "moon")
    val floaty by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(3800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "moonFloat"
    )
    val glowPulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    Box(
        modifier = Modifier
            .offset(y = (floaty * 7).dp)
            .size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((200 * glowPulse).dp)
                .background(
                    Brush.radialGradient(
                        listOf(MoonGold.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Image(
            painter = painterResource(R.drawable.ic_moon),
            contentDescription = null,
            modifier = Modifier.size(110.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MoonGold)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp)
                .size(52.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            SvgIcon(
                key = "zzz",
                modifier = Modifier.size(26.dp),
                tint = Lavender
            )
        }
    }
}

@Composable
private fun TipCard(
    iconKey: String,
    label: String,
    text: String,
    accent: Color = MoonGold
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accent.copy(alpha = 0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SvgIcon(key = iconKey, modifier = Modifier.size(23.dp), tint = accent)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextDim
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
