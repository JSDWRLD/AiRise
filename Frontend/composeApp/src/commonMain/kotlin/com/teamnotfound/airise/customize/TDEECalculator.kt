package com.teamnotfound.airise.customize

/**
 * TDEE (Total Daily Energy Expenditure) Calculator
 * Handles all calculation logic for calorie targets based on user metrics
 */
object TDEECalculator {
    
    /**
     * Convert feet and inches to centimeters
     */
    fun feetInchesToCm(feet: Int, inches: Int): Double {
        val totalInches = (feet * 12) + inches
        return totalInches * 2.54
    }
    
    /**
     * Convert pounds to kilograms
     */
    fun lbsToKg(lbs: Double): Double {
        return lbs * 0.453592
    }
    
    /**
     * Calculate TDEE using Mifflin-St Jeor Equation
     * @param gender "Male" or "Female"
     * @param goalType "Bulk", "Cut", or "Maintain"
     * @param heightCm Height in centimeters
     * @param weightKg Weight in kilograms
     * @param age Age in years
     * @param activityLevel Activity level string
     * @return Target calories per day
     */
    fun calculateTDEE(
        gender: String,
        goalType: String,
        heightCm: Double,
        weightKg: Double,
        age: Int,
        activityLevel: String
    ): Int {
        // Calculate BMR using Mifflin-St Jeor Equation
        val bmr = if (gender == "Male") {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
        }

        // Activity multipliers
        val activityMultiplier = when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extremely Active" -> 1.9
            else -> 1.2
        }

        // Calculate TDEE
        val tdee = bmr * activityMultiplier

        // Adjust for goal
        val targetCalories = when (goalType) {
            "Bulk" -> tdee + 300  // Surplus for muscle gain
            "Cut" -> tdee - 500   // Deficit for fat loss
            "Maintain" -> tdee    // Maintenance
            else -> tdee
        }

        return targetCalories.toInt()
    }
    
    /**
     * Validate height input
     * @param feet Must be 0-8
     * @param inches Must be 0-11
     */
    fun isValidHeight(feet: Int, inches: Int): Boolean {
        return feet in 0..8 && inches in 0..11
    }
    
    /**
     * Validate weight input
     * @param weight Must be positive
     */
    fun isValidWeight(weight: Double): Boolean {
        return weight > 0
    }
    
    /**
     * Check if all required fields are filled for calculation
     */
    fun isCalculateButtonEnabled(
        gender: String,
        goalType: String,
        age: String,
        heightFeet: String,
        heightInches: String,
        weightLbs: String,
        activityLevel: String
    ): Boolean {
        return gender.isNotBlank() &&
                goalType.isNotBlank() &&
                age.isNotBlank() &&
                heightFeet.isNotBlank() &&
                heightInches.isNotBlank() &&
                weightLbs.isNotBlank() &&
                activityLevel.isNotBlank()
    }
}