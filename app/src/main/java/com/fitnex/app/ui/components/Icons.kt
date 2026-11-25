package com.fitnex.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One icon set, drawn on a shared 24dp grid with a single stroke weight so the
 * whole app reads as one hand. No mixed icon libraries.
 */
enum class FitIcon {
    Home,
    Swords,
    Chart,
    Helm,
    Close,
    Check,
    Lock,
    Skull,
    Flame,
    Clock,
    Reset,
    ArrowRight,
    Trophy,
    ChevronRight,
    Camera,
    Shield,
    Spark,
}

@Composable
fun FitIcon(
    icon: FitIcon,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    iconSize: Dp = 22.dp,
    stroke: Dp = 1.6.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val u = this.size.minDimension / 24f
        val sw = stroke.toPx()
        fun pt(x: Float, y: Float) = Offset(x * u, y * u)
        fun ln(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = sw) =
            drawLine(tint, pt(x1, y1), pt(x2, y2), strokeWidth = width, cap = StrokeCap.Round)

        fun poly(points: List<Pair<Float, Float>>, close: Boolean = false) {
            val path = Path()
            points.forEachIndexed { index, (x, y) ->
                if (index == 0) path.moveTo(x * u, y * u) else path.lineTo(x * u, y * u)
            }
            if (close) path.close()
            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        when (icon) {
            FitIcon.Home -> {
                poly(listOf(4f to 11.5f, 12f to 4f, 20f to 11.5f))
                poly(listOf(6.2f to 10.5f, 6.2f to 19.5f, 17.8f to 19.5f, 17.8f to 10.5f))
                poly(listOf(10f to 19.5f, 10f to 14f, 14f to 14f, 14f to 19.5f))
            }

            FitIcon.Swords -> {
                ln(6f, 18.4f, 17.2f, 7.2f)
                ln(4.4f, 16.8f, 7.6f, 20f)
                ln(17.9f, 19.6f, 6.6f, 8.4f)
                ln(16.4f, 21.2f, 19.6f, 18f)
                ln(14.6f, 6.2f, 17.8f, 5.2f, width = sw * 0.9f)
            }

            FitIcon.Chart -> {
                ln(4f, 20f, 20f, 20f)
                ln(7f, 20f, 7f, 14f, width = sw * 1.7f)
                ln(12f, 20f, 12f, 9.2f, width = sw * 1.7f)
                ln(17f, 20f, 17f, 5.4f, width = sw * 1.7f)
            }

            FitIcon.Helm -> {
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = pt(4.5f, 6f),
                    size = Size(15f * u, 15f * u),
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
                ln(5.4f, 13.5f, 5.4f, 18.4f)
                ln(18.6f, 13.5f, 18.6f, 18.4f)
                ln(5.4f, 18.4f, 18.6f, 18.4f)
                ln(12f, 13.5f, 12f, 18.4f)
            }

            FitIcon.Close -> {
                ln(7f, 7f, 17f, 17f)
                ln(17f, 7f, 7f, 17f)
            }

            FitIcon.Check -> poly(listOf(5f to 12.4f, 9.8f to 17.4f, 19f to 6.4f))

            FitIcon.Lock -> {
                drawRoundRect(
                    color = tint,
                    topLeft = pt(5.8f, 10.8f),
                    size = Size(12.4f * u, 9.2f * u),
                    cornerRadius = CornerRadius(1.6f * u, 1.6f * u),
                    style = Stroke(width = sw),
                )
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = pt(8.2f, 5.2f),
                    size = Size(7.6f * u, 7.6f * u),
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
            }

            FitIcon.Skull -> {
                poly(
                    listOf(
                        7.2f to 16.6f, 6.4f to 12.2f, 8.2f to 8.4f, 12f to 6.6f,
                        15.8f to 8.4f, 17.6f to 12.2f, 16.8f to 16.6f,
                        15f to 16.6f, 15f to 19.4f, 9f to 19.4f, 9f to 16.6f,
                    ),
                    close = true,
                )
                drawCircle(tint, radius = 1.15f * u, center = pt(9.9f, 12.4f))
                drawCircle(tint, radius = 1.15f * u, center = pt(14.1f, 12.4f))
            }

            FitIcon.Flame -> {
                val path = Path().apply {
                    moveTo(12f * u, 3f * u)
                    cubicTo(15.6f * u, 7.4f * u, 18f * u, 10.8f * u, 18f * u, 14.2f * u)
                    cubicTo(18f * u, 18.2f * u, 15.2f * u, 21f * u, 12f * u, 21f * u)
                    cubicTo(8.8f * u, 21f * u, 6f * u, 18.2f * u, 6f * u, 14.2f * u)
                    cubicTo(6f * u, 10.8f * u, 8.4f * u, 7.4f * u, 12f * u, 3f * u)
                    close()
                }
                drawPath(path, tint, style = Stroke(width = sw, join = StrokeJoin.Round))
            }

            FitIcon.Clock -> {
                drawCircle(tint, radius = 8f * u, center = pt(12f, 12f), style = Stroke(width = sw))
                ln(12f, 12f, 12f, 7.4f)
                ln(12f, 12f, 15.4f, 13.6f)
            }

            FitIcon.Reset -> {
                drawArc(
                    color = tint,
                    startAngle = -50f,
                    sweepAngle = 285f,
                    useCenter = false,
                    topLeft = pt(4.2f, 4.2f),
                    size = Size(15.6f * u, 15.6f * u),
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
                poly(listOf(17.4f to 3.2f, 20.6f to 7.2f, 15.8f to 8.6f))
            }

            FitIcon.ArrowRight -> {
                ln(4.5f, 12f, 19f, 12f)
                poly(listOf(13.4f to 6.4f, 19f to 12f, 13.4f to 17.6f))
            }

            FitIcon.Trophy -> {
                poly(
                    listOf(
                        7.4f to 4f, 16.6f to 4f, 16.2f to 10.4f, 15.2f to 13.2f,
                        8.8f to 13.2f, 7.8f to 10.4f, 7.4f to 4f,
                    ),
                    close = true,
                )
                ln(12f, 13.4f, 12f, 17.4f)
                ln(8.6f, 20f, 15.4f, 20f)
                ln(9.8f, 17.4f, 14.2f, 17.4f)
                drawArc(
                    color = tint,
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = pt(4.4f, 5f),
                    size = Size(3.6f * u, 3.6f * u),
                    style = Stroke(width = sw),
                )
                drawArc(
                    color = tint,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = pt(16f, 5f),
                    size = Size(3.6f * u, 3.6f * u),
                    style = Stroke(width = sw),
                )
            }

            FitIcon.ChevronRight -> poly(listOf(9.2f to 5f, 15.8f to 12f, 9.2f to 19f))

            FitIcon.Camera -> {
                drawRoundRect(
                    color = tint,
                    topLeft = pt(3.4f, 6.8f),
                    size = Size(17.2f * u, 13.2f * u),
                    cornerRadius = CornerRadius(2.4f * u, 2.4f * u),
                    style = Stroke(width = sw),
                )
                drawCircle(tint, radius = 3.5f * u, center = pt(12f, 13.4f), style = Stroke(width = sw))
                ln(9f, 6.8f, 10.2f, 4.2f)
                ln(14.6f, 4.2f, 15.8f, 6.8f)
            }

            FitIcon.Shield -> {
                poly(
                    listOf(
                        12f to 3f, 19.6f to 5.8f, 18.8f to 13.6f, 12f to 21f,
                        5.2f to 13.6f, 4.4f to 5.8f,
                    ),
                    close = true,
                )
            }

            FitIcon.Spark -> {
                poly(
                    listOf(
                        12f to 2.6f, 13.7f to 10.3f, 21.4f to 12f, 13.7f to 13.7f,
                        12f to 21.4f, 10.3f to 13.7f, 2.6f to 12f, 10.3f to 10.3f,
                    ),
                    close = true,
                )
            }
        }
    }
}
