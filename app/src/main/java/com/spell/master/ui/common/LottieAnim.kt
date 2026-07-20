package com.spell.master.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Thin wrapper around lottie-compose that loads a bodymovin JSON from
 * assets/lottie/<name>, plays it, and optionally reports completion once
 * (for one-shot animations like the correct/wrong bursts).
 */
@Composable
fun LottieAnim(
    asset: String,
    modifier: Modifier = Modifier.size(96.dp),
    loop: Boolean = true,
    speed: Float = 1f,
    onFinished: (() -> Unit)? = null
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/$asset"))
    val iterations = if (loop) LottieConstants.IterateForever else 1
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
        isPlaying = true
    )
    LottieAnimation(composition = composition, progress = { progress }, modifier = modifier)

    if (!loop && onFinished != null) {
        var fired by remember(asset) { mutableStateOf(false) }
        LaunchedEffect(asset, progress) {
            if (progress >= 1f && !fired) {
                fired = true
                onFinished()
            }
        }
    }
}

/** A tiny local fade/scale helper used by a couple of screens for pop-in effects. */
@Composable
fun rememberPopInScale(delayMillis: Int = 0): Float {
    var target by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) kotlinx.coroutines.delay(delayMillis.toLong())
        target = 1f
    }
    val scale by animateFloatAsState(targetValue = target, animationSpec = tween(350, easing = LinearEasing), label = "popIn")
    return scale
}
