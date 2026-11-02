package id.dietku.app.domain

import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastingWindowCalculatorTest {
    private val eatingStart = LocalTime.of(11, 0)

    @Test
    fun identifiesEatingWindowAndProgress() {
        val state = FastingWindowCalculator.calculate(
            LocalDateTime.of(2026, 6, 1, 15, 0),
            eatingStart,
            fastingHours = 16,
        )
        assertTrue(state.isEatingWindow)
        assertEquals(0.5f, state.elapsedFraction, 0.001f)
        assertEquals(19, state.nextChange.hour)
    }

    @Test
    fun identifiesFastBeforeEatingWindow() {
        val state = FastingWindowCalculator.calculate(
            LocalDateTime.of(2026, 6, 1, 9, 0),
            eatingStart,
            fastingHours = 16,
        )
        assertFalse(state.isEatingWindow)
        assertEquals(11, state.nextChange.hour)
        assertEquals(0.875f, state.elapsedFraction, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFastingLongerThanEighteenHours() {
        FastingWindowCalculator.calculate(
            LocalDateTime.of(2026, 6, 1, 9, 0),
            eatingStart,
            fastingHours = 20,
        )
    }
}