package com.spell.master.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.WrongRed

/** Animated 30s countdown -- grows and turns red once [secondsLeft] drops to 10 or below,
 * and gives the whole clock a little punch each second in sync with the tick sound. */
@Composable
fun CountdownTimer(secondsLeft: Int, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 56.dp) {
    val urgent = secondsLeft in 0..10
    val scale by animateFloatAsState(
        targetValue = if (urgent) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "timerScale"
    )
    val color by animateColorAsState(targetValue = if (urgent) WrongRed else InkBrown, label = "timerColor")

    val tickScale = remember { Animatable(1f) }
    LaunchedEffect(secondsLeft) {
        tickScale.snapTo(if (urgent) 1.18f else 1.12f)
        tickScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(modifier = modifier.size(size).scale(tickScale.value), contentAlignment = Alignment.Center) {
        LottieAnim(
            asset = "clock_pulse.json",
            modifier = Modifier.size(size),
            loop = true,
            speed = if (urgent) 1.8f else 1f
        )
        Text(
            text = secondsLeft.coerceAtLeast(0).toString(),
            color = color,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
            modifier = Modifier.scale(scale)
        )
    }
}
