package com.teamnotfound.airise.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Tests for Health Sync Permissions Feature (Updated Logic)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelHealthSyncTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test 1: Verify canReadHealthData defaults to false
     */
    @Test
    fun `canReadHealthData defaults to false`() {
        val state = HomeUiState()

        assertFalse(
            state.canReadHealthData,
            "canReadHealthData should default to false until we verify we can read"
        )
    }

    /**
     * Test 2: Verify isHealthSyncAvailable is based only on canReadHealthData
     */
    @Test
    fun `isHealthSyncAvailable is true when canReadHealthData is true`() {
        val stateWithPermissions = HomeUiState(canReadHealthData = true)
        val stateWithoutPermissions = HomeUiState(canReadHealthData = false)

        assertTrue(
            stateWithPermissions.isHealthSyncAvailable,
            "Health sync should be available when we can read health data"
        )
        assertFalse(
            stateWithoutPermissions.isHealthSyncAvailable,
            "Health sync should not be available when we cannot read health data"
        )
    }

    /**
     * Test 3: Verify isHealthSyncAvailable is true with permissions even if values are zero
     * Acceptance Criteria: If we can read platform, sync is available regardless of values
     */
    @Test
    fun `isHealthSyncAvailable is true when canReadHealthData is true even with zero values`() {
        val stateWithPermissionsButZeroActivity = HomeUiState(
            canReadHealthData = true,
            healthData = com.teamnotfound.airise.data.serializable.HealthData(
                steps = 0,
                caloriesBurned = 0,
                sleep = 0.0,
                hydration = 0.0
            )
        )

        assertTrue(
            stateWithPermissionsButZeroActivity.canReadHealthData,
            "Should be able to read platform data"
        )
        assertTrue(
            stateWithPermissionsButZeroActivity.isHealthSyncAvailable,
            "Health sync should be available when we can read, even if values are 0"
        )
    }

    /**
     * Test 4: Verify manual hydration updates work independently
     */
    @Test
    fun `hydration can be updated manually without health sync permissions`() {
        val initialHydration = 0.0
        val updatedHydration = 64.5

        val initialState = HomeUiState(
            canReadHealthData = false,
            healthData = com.teamnotfound.airise.data.serializable.HealthData(
                hydration = initialHydration
            )
        )

        val updatedState = initialState.copy(
            healthData = initialState.healthData.copy(hydration = updatedHydration)
        )

        assertEquals(initialHydration, initialState.healthData.hydration)
        assertEquals(updatedHydration, updatedState.healthData.hydration)
        assertFalse(
            updatedState.canReadHealthData,
            "Hydration updates should work independently of sync permissions"
        )
    }

    /**
     * Test 5: Verify UI state transitions from no permissions to permissions granted
     */
    @Test
    fun `UI state transitions correctly when permissions are granted`() {
        // Before: No permissions
        val stateBeforeSync = HomeUiState(
            canReadHealthData = false,
            healthData = com.teamnotfound.airise.data.serializable.HealthData(
                steps = 0,
                caloriesBurned = 0,
                sleep = 0.0
            )
        )

        val stateAfterSync = stateBeforeSync.copy(
            canReadHealthData = true,
            healthData = stateBeforeSync.healthData.copy(
                steps = 5000,
                caloriesBurned = 300,
                sleep = 7.5
            )
        )

        assertFalse(stateBeforeSync.canReadHealthData, "Before: should not have read access")
        assertFalse(stateBeforeSync.isHealthSyncAvailable, "Before: sync not available")

        assertTrue(stateAfterSync.canReadHealthData, "After: should have read access")
        assertTrue(stateAfterSync.isHealthSyncAvailable, "After: sync should be available")

        assertEquals(0, stateBeforeSync.healthData.steps, "Before: steps should be 0")
        assertEquals(5000, stateAfterSync.healthData.steps, "After: steps should be synced")
    }
}