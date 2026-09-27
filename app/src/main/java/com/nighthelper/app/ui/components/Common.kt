package com.nighthelper.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nighthelper.app.R
import com.nighthelper.app.ui.theme.Hairline
import com.nighthelper.app.ui.theme.Lavender

/** Maps an item's iconKey to an SVG vector drawable resource. */
fun iconRes(key: String): Int = when (key) {
    "pill" -> R.drawable.ic_pill
    "tooth" -> R.drawable.ic_tooth
    "book" -> R.drawable.ic_book
    "heart" -> R.drawable.ic_heart
    "drop" -> R.drawable.ic_drop
    "zzz" -> R.drawable.ic_zzz
    "bell" -> R.drawable.ic_bell
    "star" -> R.drawable.ic_star
    "sparkle" -> R.drawable.ic_sparkle
    "settings" -> R.drawable.ic_settings
    "history" -> R.drawable.ic_history
    "flip" -> R.drawable.ic_flip
    "widget" -> R.drawable.ic_widget
    else -> R.drawable.ic_moon
}

val availableIconKeys = listOf("pill", "tooth", "book", "heart", "star", "sparkle", "drop", "zzz", "bell", "moon")

@Composable
fun SvgIcon(
    key: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Icon(
        painter = painterResource(iconRes(key)),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun rememberHaptics(enabled: Boolean): () -> Unit {
    val view = LocalView.current
    return remember(enabled, view) {
        {
            if (enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    iconKey: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Lavender.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SvgIcon(key = iconKey, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.size(10.dp))
            content()
        }
    }
}
