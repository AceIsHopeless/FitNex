package com.fitnex.app.ui

import com.fitnex.app.domain.Rank
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatClock(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", safe / 60, safe % 60)
}

/**
 * How far the hero has climbed inside the current rank band, 0f..1f.
 * At the top rank the ring is simply full.
 */
fun rankProgressFor(level: Int): Float {
    val rank = Rank.forLevel(level)
    val next = Rank.entries.firstOrNull { it.minLevel > level } ?: return 1f
    val span = (next.minLevel - rank.minLevel).coerceAtLeast(1)
    return ((level - rank.minLevel + 1).toFloat() / span.toFloat()).coerceIn(0f, 1f)
}

private val historyFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.getDefault())

fun formatHistoryDate(timestampMillis: Long): String =
    historyFormatter.format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
