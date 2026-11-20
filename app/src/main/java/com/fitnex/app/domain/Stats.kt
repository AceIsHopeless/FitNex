package com.fitnex.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** One column of the weekly rep chart. */
data class DailyReps(
    val epochDay: Long,
    val label: String,
    val pushUps: Int,
)

/**
 * Reps per calendar day for the last [dayCount] days, oldest first.
 * Buckets are computed from workout timestamps, so no extra storage is needed.
 */
fun repsByDay(
    history: List<WorkoutRecord>,
    dayCount: Int = 7,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): List<DailyReps> {
    if (dayCount <= 0) return emptyList()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val byDay = history.groupBy { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
    return (dayCount - 1 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        DailyReps(
            epochDay = day.toEpochDay(),
            label = day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            pushUps = byDay[day].orEmpty().sumOf { it.pushUps },
        )
    }
}

fun PlayerState.repsLastSevenDays(
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): List<DailyReps> = repsByDay(history, dayCount = 7, nowMillis = nowMillis, zone = zone)
