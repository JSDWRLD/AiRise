package com.teamnotfound.airise.customize

/**
 * Workout Customization Validator
 * Handles validation logic for workout days, length, and equipment
 */
object WorkoutValidator {
    
    /**
     * Validate workout days count
     * Must be between 3 and 6 days per week
     */
    fun isValidWorkoutDays(days: List<String>): Boolean {
        return days.size in 3..6
    }
    
    /**
     * Validate workout length
     * Must be one of the predefined options: 15, 30, 45, or 60 minutes
     */
    fun isValidWorkoutLength(length: Int): Boolean {
        return length in listOf(15, 30, 45, 60)
    }
    
    /**
     * Validate equipment selection
     * Must be one of: "bodyweight", "home", or "gym"
     */
    fun isValidEquipment(equipment: String): Boolean {
        return equipment in listOf("bodyweight", "home", "gym")
    }
}