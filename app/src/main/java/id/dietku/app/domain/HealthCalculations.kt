package id.dietku.app.domain

import kotlin.math.roundToInt

object HealthCalculations {
    fun bmi(weightKg: Double, heightCm: Double): Double {
        require(weightKg > 0.0) { "Berat harus lebih dari nol." }
        require(heightCm > 0.0) { "Tinggi harus lebih dari nol." }
        val heightMeters = heightCm / 100.0
        return weightKg / (heightMeters * heightMeters)
    }

    fun whoBmiCategory(bmi: Double): String = when {
        bmi < 18.5 -> "Berat badan kurang"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Berat badan berlebih"
        else -> "Obesitas"
    }

    fun asiaPacificBmiCategory(bmi: Double): String = when {
        bmi < 18.5 -> "Berat badan kurang"
        bmi < 23.0 -> "Normal"
        bmi < 25.0 -> "Berisiko meningkat"
        bmi < 30.0 -> "Obesitas tingkat I"
        else -> "Obesitas tingkat II"
    }

    fun bmrMifflinStJeor(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int,
        gender: String,
    ): Double {
        require(ageYears > 0) { "Usia harus lebih dari nol." }
        val sexAdjustment = if (gender == "Laki-laki") 5.0 else -161.0
        return (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears) + sexAdjustment
    }

    fun dailyEnergyNeedsKcal(bmr: Double, activityFactor: Double): Int {
        require(bmr > 0.0) { "BMR harus lebih dari nol." }
        require(activityFactor >= 1.0) { "Faktor aktivitas tidak valid." }
        return (bmr * activityFactor).roundToInt()
    }

    fun recommendedCalorieTargetKcal(dailyNeedsKcal: Int, deficitKcal: Int = 500): Int {
        require(dailyNeedsKcal > 0) { "Kebutuhan kalori harus lebih dari nol." }
        require(deficitKcal >= 0) { "Defisit tidak boleh negatif." }
        return ((dailyNeedsKcal - deficitKcal).coerceAtLeast(1500) / 50.0).roundToInt() * 50
    }

    fun stageOneWeightRange(startingWeightKg: Double): ClosedFloatingPointRange<Double> {
        require(startingWeightKg > 0.0) { "Berat awal harus lebih dari nol." }
        return (startingWeightKg * 0.90)..(startingWeightKg * 0.95)
    }
}