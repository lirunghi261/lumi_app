package com.example.personaldashboard.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.personaldashboard.R
import kotlinx.coroutines.delay

private enum class LumiPose(@param:DrawableRes val drawable: Int) {
    IDLE(R.drawable.mascot_lumi),
    BLINK(R.drawable.mascot_lumi_blink),
    GROOM(R.drawable.mascot_lumi_groom),
}

@Composable
fun AnimatedLumiMascot(modifier: Modifier = Modifier, reduceMotion: Boolean = false) {
    var pose by remember { mutableStateOf(LumiPose.IDLE) }
    var tapCount by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    val idleMotion = rememberInfiniteTransition(label = "Lumi idle motion")
    val floatY by idleMotion.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Lumi float",
    )
    val sway by idleMotion.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Lumi sway",
    )
    val breathe by idleMotion.animateFloat(
        initialValue = 0.995f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Lumi breathing",
    )

    LaunchedEffect(tapCount, reduceMotion) {
        if (reduceMotion) {
            pose = LumiPose.IDLE
            return@LaunchedEffect
        }
        if (tapCount > 0) {
            pose = LumiPose.GROOM
            delay(900)
            pose = LumiPose.BLINK
            delay(140)
            pose = LumiPose.IDLE
        }

        var cycle = 0
        while (true) {
            delay(2_800L + (cycle % 3) * 650L)
            pose = LumiPose.BLINK
            delay(140)
            pose = LumiPose.IDLE

            if (cycle % 3 == 2) {
                delay(500)
                pose = LumiPose.GROOM
                delay(900)
                pose = LumiPose.BLINK
                delay(140)
                pose = LumiPose.IDLE
            }
            cycle++
        }
    }

    Crossfade(
        targetState = pose,
        animationSpec = tween(90),
        label = "Lumi expression",
        modifier = modifier
            .graphicsLayer {
                translationY = if (reduceMotion) 0f else floatY
                rotationZ = if (reduceMotion) 0f else sway
                scaleX = if (reduceMotion) 1f else breathe
                scaleY = if (reduceMotion) 1f else breathe
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = "Play a cute animation",
            ) { tapCount++ },
    ) { currentPose ->
        Image(
            painter = painterResource(currentPose.drawable),
            contentDescription = "Lumi mascot. Tap for a cute animation",
            contentScale = ContentScale.Fit,
        )
    }
}
