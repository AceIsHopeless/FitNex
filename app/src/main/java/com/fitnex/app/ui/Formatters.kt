package com.fitnex.app.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatClock(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", safe / 60, safe % 60)
}

private val historyFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.getDefault())

fun formatHistoryDate(timestampMillis: Long): String =
    historyFormatter.format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
