package id.dietku.app.domain

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

data class FastingWindowState(
    val isEatingWindow: Boolean,
    val periodStart: ZonedDateTime,
    val nextChange: ZonedDateTime,
    val elapsedFraction: Float,
) {
    val remaining: Duration
        get() = Duration.between(ZonedDateTime.now(nextChange.zone), nextChange).coerceAtLeast(Duration.ZERO)
}

object FastingWindowCalculator {
    val supportedFastingHours = setOf(12, 14, 16, 18)

    fun calculate(
        now: ZonedDateTime,
        eatingStart: LocalTime,
        fastingHours: Int,
    ): FastingWindowState {
        require(fastingHours in supportedFastingHours) {
            "Puasa hanya didukung sampai 18 jam."
        }
        val eatingHours = 24 - fastingHours
        val todayStart = now.toLocalDate().atTime(eatingStart).atZone(now.zone)
        val todayEnd = todayStart.plusHours(eatingHours.toLong())

        return when {
            !now.isBefore(todayStart) && now.isBefore(todayEnd) ->
                FastingWindowState(
                    isEatingWindow = true,
                    periodStart = todayStart,
                    nextChange = todayEnd,
                    elapsedFraction = fraction(now, todayStart, todayEnd),
                )

            now.isBefore(todayStart) -> {
                val previousFastStart = todayStart.minusDays(1).plusHours(eatingHours.toLong())
                FastingWindowState(
                    isEatingWindow = false,
                    periodStart = previousFastStart,
                    nextChange = todayStart,
                    elapsedFraction = fraction(now, previousFastStart, todayStart),
                )
            }

            else -> {
                val nextEatingStart = todayStart.plusDays(1)
                FastingWindowState(
                    isEatingWindow = false,
                    periodStart = todayEnd,
                    nextChange = nextEatingStart,
                    elapsedFraction = fraction(now, todayEnd, nextEatingStart),
                )
            }
        }
    }

    fun calculate(
        now: LocalDateTime,
        eatingStart: LocalTime,
        fastingHours: Int,
    ): FastingWindowState = calculate(now.atZone(java.time.ZoneId.systemDefault()), eatingStart, fastingHours)

    private fun fraction(
        now: ZonedDateTime,
        start: ZonedDateTime,
        end: ZonedDateTime,
    ): Float {
        val total = Duration.between(start, end).toMillis()
        if (total <= 0L) return 0f
        return (Duration.between(start, now).toMillis().toFloat() / total).coerceIn(0f, 1f)
    }
}