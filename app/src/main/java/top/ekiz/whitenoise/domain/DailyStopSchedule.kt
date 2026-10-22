package top.ekiz.whitenoise.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

data class DailyStopSchedule(
    val enabled: Boolean = false,
    val minuteOfDay: Int = 7 * 60,
    val days: Set<DayOfWeek> = DayOfWeek.entries.take(5).toSet()
) {
    val hour: Int
        get() = minuteOfDay / 60

    val minute: Int
        get() = minuteOfDay % 60

    fun nextStopAt(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long? {
        if (!enabled || days.isEmpty()) return null
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val time = LocalTime.of(hour, minute)
        return (0L..7L)
            .asSequence()
            .map { now.toLocalDate().plusDays(it).atTime(time).atZone(zone) }
            .firstOrNull { it.dayOfWeek in days && it.isAfter(now) }
            ?.toInstant()
            ?.toEpochMilli()
    }
}
