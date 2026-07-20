package com.spell.master.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spell.master.ui.theme.BeeYellow
import com.spell.master.ui.theme.LockedGray
import kotlinx.coroutines.delay

/**
 * Five-star rating, either drawn instantly (dashboard tiles) or with a
 * staggered pop-in (level result celebration screen).
 */
@Composable
fun StarRatingRow(
    rating: Int,
    modifier: Modifier = Modifier,
    starSizeSp: Int = 20,
    animated: Boolean = false,
    filledColor: Color = BeeYellow,
    emptyColor: Color = LockedGray
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val filled = index < rating
            var visible by remember(rating) { mutableStateOf(!animated) }
            LaunchedEffect(rating, animated) {
                if (animated) {
                    delay(index * 180L)
                    visible = true
                }
            }
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "starScale"
            )
            Text(
                text = if (filled) "★" else "☆",
                color = if (filled) filledColor else emptyColor,
                fontSize = starSizeSp.sp,
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .scale(scale)
            )
        }
    }
}
