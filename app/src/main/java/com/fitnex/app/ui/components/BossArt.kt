package com.fitnex.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fitnex.app.R
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Charcoal

/**
 * Boss portrait. The bottom fade blends the artwork into the app background so
 * the panel reads as one surface instead of a pasted photo.
 */
@Composable
fun BossArt(
    bossId: String,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(Charcoal),
    ) {
        Image(
            painter = painterResource(artFor(bossId)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.62f to Color.Transparent,
                        1f to Ash.copy(alpha = 0.92f),
                    ),
                ),
        )
    }
}

private fun artFor(bossId: String): Int = when (bossId) {
    "zombie" -> R.drawable.boss_zombie
    "skeleton" -> R.drawable.boss_skeleton
    "vampire" -> R.drawable.boss_vampire
    "dark_knight" -> R.drawable.boss_dark_knight
    else -> R.drawable.boss_slime
}
