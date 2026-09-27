package com.nighthelper.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nighthelper.app.R
import com.nighthelper.app.ui.theme.MoonGold
import com.nighthelper.app.ui.theme.NightPurple
import com.nighthelper.app.ui.theme.NightPurpleDeep
import com.nighthelper.app.ui.theme.NightPurpleHigh
import kotlin.math.sin

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float
)

/**
 * Full-screen night sky: vertical gradient, twinkling SVG-less stars (canvas),
 * and a soft glowing moon painted from the vector drawable.
 */
@Composable
fun MoonSky(
    modifier: Modifier = Modifier,
    showCornerMoon: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "sky")
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "twinkle"
    )
    val floaty by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floaty"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NightPurple, NightPurpleDeep, NightPurpleHigh)
                )
            )
    ) {
        val stars = remember { generateStars(52) }
        Canvas(modifier = Modifier.matchParentSize()) {
            stars.forEach { star ->
                val wave = (sin(star.phase + twinkle * 2f * Math.PI.toFloat()) + 1f) / 2f
                val alpha = 0.15f + 0.6f * wave
                drawCircle(
                    color = Color(0xFFCDBFFF).copy(alpha = alpha),
                    radius = star.radius * size.minDimension / 900f,
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }
        if (showCornerMoon) {
            Image(
                painter = painterResource(R.drawable.ic_moon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 18.dp)
                    .size(52.dp)
                    .offset(y = (floaty * 4).dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MoonGold)
            )
        }
        content()
    }
}

private fun generateStars(count: Int): List<Star> = List(count) { index ->
    Star(
        x = ((index * 37) % 100) / 100f + 0.005f,
        y = ((index * 61) % 90) / 100f + 0.02f,
        radius = 1.6f + ((index * 13) % 10) / 10f * 1.8f,
        phase = ((index * 29) % 100) / 100f * 2f * Math.PI.toFloat()
    )
}
