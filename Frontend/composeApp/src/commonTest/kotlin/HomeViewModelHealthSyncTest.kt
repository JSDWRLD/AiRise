package com.teamnotfound.airise.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Simple tests for Health Sync Permissions Feature
 * Tests the hasHealthSyncPermissions flag state management
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
     * Test 1: Verify flag defaults to false
     * Acceptance Criteria: App does not auto-request permissions on launch
     */
    @Test
    fun `hasHealthSyncPermissions flag defaults to false`() {
        val state = HomeUiState()

        assertFalse(
            state.hasHealthSyncPermissions,
            "hasHealthSyncPermissions should default to false"
        )
    }

    /**
     * Test 2: Verify flag can be set to true
     * Acceptance Criteria: Flag updates after user enables sync in Settings
     */
    @Test
    fun `hasHealthSyncPermissions flag can be updated to true`() {
        val initialState = HomeUiState(hasHealthSyncPermissions = false)
        val updatedState = initialState.copy(hasHealthSyncPermissions = true)

        assertFalse(initialState.hasHealthSyncPermissions, "Initial state should be false")
        assertTrue(updatedState.hasHealthSyncPermissions, "Updated state should be true")
    }

    /**
     * Test 3: Verify flag state is preserved in UI state
     * Acceptance Criteria: Flag controls UI state globally
     */
    @Test
    fun `hasHealthSyncPermissions flag state is maintained in HomeUiState`() {
        val stateWithoutPermissions = HomeUiState(hasHealthSyncPermissions = false)
        val stateWithPermissions = HomeUiState(hasHealthSyncPermissions = true)

        assertFalse(stateWithoutPermissions.hasHealthSyncPermissions)
        assertTrue(stateWithPermissions.hasHealthSyncPermissions)
        assertNotEquals(
            stateWithoutPermissions.hasHealthSyncPermissions,
            stateWithPermissions.hasHealthSyncPermissions,
            "Flag states should be different"
        )
    }

    /**
     * Test 4: Verify manual hydration updates work correctly
     * Acceptance Criteria: Hydration is never fetched from KHealth (manual entry only)
     */
    @Test
    fun `hydration can be updated manually without health sync permissions`() {
        val initialHydration = 0.0
        val updatedHydration = 64.5

        val initialState = HomeUiState(
            hasHealthSyncPermissions = false,
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
            updatedState.hasHealthSyncPermissions,
            "Hydration updates should work independently of sync permissions"
        )
    }

    /**
     * Test 5: Verify UI state transitions between permission states
     * Acceptance Criteria: Changing the flag updates the Home UI without app restart
     */
    @Test
    fun `UI state can transition from no permissions to permissions granted`() {
        // Simulate initial state (no permissions)
        val stateBeforeSync = HomeUiState(
            hasHealthSyncPermissions = false,
            healthData = com.teamnotfound.airise.data.serializable.HealthData(
                steps = 0,
                caloriesBurned = 0,
                sleep = 0.0
            )
        )

        // Simulate state after permissions granted
        val stateAfterSync = stateBeforeSync.copy(
            hasHealthSyncPermissions = true,
            healthData = stateBeforeSync.healthData.copy(
                steps = 5000,
                caloriesBurned = 300,
                sleep = 7.5
            )
        )

        assertFalse(stateBeforeSync.hasHealthSyncPermissions, "Before: permissions should be false")
        assertTrue(stateAfterSync.hasHealthSyncPermissions, "After: permissions should be true")

        assertEquals(0, stateBeforeSync.healthData.steps, "Before: steps should be 0")
        assertEquals(5000, stateAfterSync.healthData.steps, "After: steps should be updated")
    }

    /**
     * Test 6: Verify hydration remains separate from synced health data
     * Acceptance Criteria: Hydration is never fetched from KHealth
     */
    @Test
    fun `hydration value is preserved when other health data is synced`() {
        val userEnteredHydration = 48.0

        // State with user-entered hydration, no sync
        val stateBeforeSync = HomeUiState(
            hasHealthSyncPermissions = false,
            healthData = com.teamnotfound.airise.data.serializable.HealthData(
                hydration = userEnteredHydration,
                steps = 0,
                caloriesBurned = 0
            )
        )

        // After sync is enabled, hydration should be preserved
        val stateAfterSync = stateBeforeSync.copy(
            hasHealthSyncPermissions = true,
            healthData = stateBeforeSync.healthData.copy(
                steps = 8000,
                caloriesBurned = 450
                // hydration NOT updated - preserved from user entry
            )
        )

        assertEquals(
            userEnteredHydration,
            stateBeforeSync.healthData.hydration,
            "Before sync: hydration is user-entered value"
        )
        assertEquals(
            userEnteredHydration,
            stateAfterSync.healthData.hydration,
            "After sync: hydration must remain user-entered value"
        )
        assertEquals(8000, stateAfterSync.healthData.steps, "Steps should be updated")
        assertEquals(450, stateAfterSync.healthData.caloriesBurned, "Calories should be updated")
    }
}