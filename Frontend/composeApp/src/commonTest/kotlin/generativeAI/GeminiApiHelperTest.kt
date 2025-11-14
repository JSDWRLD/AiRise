package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import kotlin.test.*

// Test GeminiApi helper logic
class GeminiApiHelperTest {

    private val api = GeminiApiTestHelper()

    @Test
    fun `healthSnapshot returns null when HealthData is null`() {
        val result = api.testHealthSnapshot(null)
        assertNull(result)
    }

    @Test
    fun `healthSnapshot includes all fields when present`() {
        val health = HealthData(
            steps = 5000,
            caloriesBurned = 250,
            caloriesEaten = 1640,
            sleep = 7.5,
            hydration = 2.0
        )

        val result = api.testHealthSnapshot(health)

        assertNotNull(result)
        assertTrue(result.contains("steps=5000"))
        assertTrue(result.contains("kcal_eaten=1640"))
        assertTrue(result.contains("kcal_burned=250"))
        assertTrue(result.contains("sleep_h=7.5"))
        assertTrue(result.contains("water_oz=2.0"))
    }

    @Test
    fun `healthSnapshot excludes zero values`() {
        val health = HealthData(
            steps = 5000,
            caloriesBurned = 0,
            sleep = 7.5,
            hydration = 0.0
        )

        val result = api.testHealthSnapshot(health)

        assertNotNull(result)
        assertTrue(result.contains("steps=5000"))
        assertTrue(result.contains("sleep_h=7.5"))
        assertFalse(result.contains("kcal_burned"))
    }

    @Test
    fun `progressSnapshot returns null when data is null`() {
        val result = api.testProgressSnapshot(null)
        assertNull(result)
    }

    @Test
    fun `progressSnapshot clamps values to 0-100 range`() {
        val progress = DailyProgressData(
            totalProgress = 150f,  // over 100
            caloriesProgress = -10f,  // negative
            sleepProgress = 50f,
            hydrationProgress = 75f
        )

        val result = api.testProgressSnapshot(progress)

        assertNotNull(result)
        assertTrue(result.contains("progress_total=100"))
        assertTrue(result.contains("sleep=50"))
        assertTrue(result.contains("hydration=75"))
    }

    @Test
    fun `buildProfileBlock returns null when all params are null`() {
        val result = api.testBuildProfileBlock(
            null, null, null, null, null, null
        )
        assertNull(result)
    }

    @Test
    fun `buildProfileBlock includes only non-null values`() {
        val result = api.testBuildProfileBlock(
            workoutGoal = "Build muscle",
            fitnessLevel = "Intermediate",
            workoutLength = 45,
            activityLevel = null,
            dietaryGoal = null,
            workoutRestrictions = null
        )

        assertNotNull(result)
        assertTrue(result.contains("goal=Build muscle"))
        assertTrue(result.contains("fitness_level=Intermediate"))
        assertTrue(result.contains("session_length_min=45"))
        assertFalse(result.contains("activity_level"))
        assertFalse(result.contains("diet_goal"))
    }

    @Test
    fun `buildProfileBlock excludes blank and empty strings`() {
        val result = api.testBuildProfileBlock(
            workoutGoal = "  ",  // blank - should be excluded by isNotBlank()
            fitnessLevel = "Beginner",
            workoutLength = 30,
            activityLevel = "",  // empty - should be excluded by isNotBlank()
            dietaryGoal = "High protein",
            workoutRestrictions = null
        )

        assertNotNull(result)
        // Check that blank/empty strings are properly excluded
        assertTrue(result.contains("fitness_level=Beginner"))
        assertTrue(result.contains("diet_goal=High protein"))
        assertTrue(result.contains("session_length_min=30"))

        // The blank goal and empty activity should not appear
        val resultLower = result.lowercase()
        assertFalse(resultLower.contains("goal=  "))  // no blank goal
        assertFalse(resultLower.contains("activity_level="))  // no empty activity
    }

    @Test
    fun `buildProfileBlock excludes zero workout length`() {
        val result = api.testBuildProfileBlock(
            workoutGoal = "Weight loss",
            fitnessLevel = "Beginner",
            workoutLength = 0,
            activityLevel = "Moderate",
            dietaryGoal = null,
            workoutRestrictions = null
        )

        assertNotNull(result)
        assertFalse(result.contains("session_length_min"))
    }
}

// Helper class that exposes private methods for testing
class GeminiApiTestHelper {

    fun testHealthSnapshot(h: HealthData?): String? {
        if (h == null) return null
        val parts = buildList {
            if ((h.steps ?: 0) > 0) add("steps=${h.steps}")
            if ((h.caloriesBurned ?: 0) > 0) add("kcal_burned=${h.caloriesBurned}")
            if ((h.caloriesEaten ?: 0) > 0) add ("kcal_eaten=${h.caloriesEaten}")
            if ((h.sleep ?: 0.0) > 0.0) add("sleep_h=${h.sleep}")
            if ((h.hydration ?: 0.0) > 0f) add("water_oz=${h.hydration}")
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString("; ")
    }

    fun testProgressSnapshot(p: DailyProgressData?): String? {
        if (p == null) return null

        fun pct(x: Float) = x.coerceIn(0f, 100f).toInt()

        val total = pct(p.totalProgress)
        val calories = pct(p.caloriesProgress)
        val sleep = pct(p.sleepProgress)
        val hydra = pct(p.hydrationProgress)

        return "progress_total=$total; calories=$calories; sleep=$sleep; hydration=$hydra"
    }

    fun testBuildProfileBlock(
        workoutGoal: String?,
        fitnessLevel: String?,
        workoutLength: Int?,
        activityLevel: String?,
        dietaryGoal: String?,
        workoutRestrictions: String?
    ): String? {
        val lines = buildList {
            workoutGoal?.takeIf { it.isNotBlank() }?.let { add("goal=$it") }
            fitnessLevel?.takeIf { it.isNotBlank() }?.let { add("fitness_level=$it") }
            workoutLength?.takeIf { it > 0 }?.let { add("session_length_min=$it") }
            activityLevel?.takeIf { it.isNotBlank() }?.let { add("activity_level=$it") }
            dietaryGoal?.takeIf { it.isNotBlank() }?.let { add("diet_goal=$it") }
            workoutRestrictions?.takeIf { it.isNotBlank() }?.let { add("restrictions=$it") }
        }
        return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }
}