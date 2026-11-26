package com.fitnex.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fitnex.app.R
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.Night
import com.fitnex.app.ui.theme.Panel

/**
 * Boss portrait framed like a codex plate: art, a cool rim, a bottom fade into
 * the page and a low ember bloom behind the subject. [dim] lets the same art sit
 * quietly behind text on the result screen.
 */
@Composable
fun BossArt(
    bossId: String,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    cornerRadius: Dp = 22.dp,
    dim: Float = 0f,
    showScrim: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(Panel),
    ) {
        Image(
            painter = painterResource(artFor(bossId)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 1f - dim,
        )
        if (showScrim) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Night.copy(alpha = 0.55f),
                            0.35f to Color.Transparent,
                            0.70f to Night.copy(alpha = 0.55f),
                            1f to Night.copy(alpha = 0.97f),
                        ),
                    ),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.Transparent, Night.copy(alpha = 0.55f)),
                            center = Offset(0.5f, 0.35f),
                            radius = 700f,
                        ),
                    ),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Ember.copy(alpha = 0.05f), Color.Transparent),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun BossThumb(
    bossId: String,
    size: Dp,
    modifier: Modifier = Modifier,
    dim: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(Panel),
    ) {
        Image(
            painter = painterResource(artFor(bossId)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = if (dim) 0.35f else 1f,
            colorFilter = if (dim) {
                androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                    androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) },
                )
            } else {
                null
            },
        )
        if (dim) {
            Box(Modifier.fillMaxSize().background(Panel.copy(alpha = 0.45f)))
        }
    }
}

private fun artFor(bossId: String): Int = when (bossId) {
    "zombie" -> R.drawable.boss_zombie
    "skeleton" -> R.drawable.boss_skeleton
    "vampire" -> R.drawable.boss_vampire
    "dark_knight" -> R.drawable.boss_dark_knight
    else -> R.drawable.boss_slime
}
