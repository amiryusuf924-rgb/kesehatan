package id.dietku.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCalculationsTest {
    @Test
    fun bmiUsesWeightAndHeight() {
        assertEquals(31.588, HealthCalculations.bmi(86.0, 165.0), 0.01)
    }

    @Test
    fun categoriesUseWhoAndAsiaPacificThresholds() {
        assertEquals("Berat badan berlebih", HealthCalculations.whoBmiCategory(27.0))
        assertEquals("Obesitas tingkat I", HealthCalculations.asiaPacificBmiCategory(27.0))
    }

    @Test
    fun maleMifflinStJeorAndDefaultTargetMatchProfile() {
        val bmr = HealthCalculations.bmrMifflinStJeor(86.0, 165.0, 27, "Laki-laki")
        assertEquals(1741.25, bmr, 0.01)
        assertEquals(2089, HealthCalculations.dailyEnergyNeedsKcal(bmr, 1.2))
        assertEquals(1600, HealthCalculations.recommendedCalorieTargetKcal(2089))
    }

    @Test
    fun suggestedTargetNeverDropsBelowSafetyFloor() {
        assertEquals(1500, HealthCalculations.recommendedCalorieTargetKcal(1700))
    }

    @Test
    fun firstStageTargetsFiveToTenPercentLoss() {
        val range = HealthCalculations.stageOneWeightRange(86.0)
        assertEquals(77.4, range.start, 0.001)
        assertEquals(81.7, range.endInclusive, 0.001)
        assertTrue(range.start < range.endInclusive)
    }
}