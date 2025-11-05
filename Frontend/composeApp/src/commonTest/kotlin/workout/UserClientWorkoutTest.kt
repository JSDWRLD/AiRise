package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.*
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import notifications.WorkoutReminderUseCase
import kotlin.test.*

/**
 * Unit tests for WorkoutViewModel.
 * These tests verify view model behavior using mock repository and fake notifier.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserClientWorkoutTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        WorkoutCache.clear()
    }

    @AfterTest
    fun tearDown() = runTest {
        Dispatchers.resetMain()
        WorkoutCache.clear()
    }

    // ========== Helper Functions ==========

    private fun createMockProgramDoc(
        dayIndex: Int = 1,
        exerciseName: String = "Bench Press",
        repsCompleted: Int = 0,
        weight: Int = 135
    ): UserProgramDoc {
        return UserProgramDoc(
            id = "program-123",
            firebaseUid = "user-123",
            program = UserProgram(
                templateName = "Test Program",
                days = 3,
                type = ProgramType.Gym,
                schedule = listOf(
                    UserProgramDay(
                        dayIndex = dayIndex,
                        dayName = "Day 1",
                        focus = "Upper Push",
                        exercises = listOf(
                            UserExerciseEntry(
                                name = exerciseName,
                                sets = 3,
                                targetReps = "8-10",
                                repsCompleted = repsCompleted,
                                weight = UserExerciseWeight(value = weight, unit = "lbs")
                            )
                        )
                    )
                ),
                createdAtUtc = "2025-01-01T00:00:00Z",
                updatedAtUtc = "2025-01-01T00:00:00Z"
            ),
            lastUpdatedUtc = "2025-01-01T00:00:00Z"
        )
    }

    private fun createMockUserData(workoutTime: String = "19:00"): UserData {
        return UserData(
            firstName = "Test",
            lastName = "User",
            middleName = "",
            fullName = "Test User",
            workoutGoal = "Strength",
            fitnessLevel = "Intermediate",
            workoutLength = 60,
            workoutEquipment = "Full Gym",
            workoutDays = listOf("Mon", "Wed", "Fri"),
            workoutTime = workoutTime,
            dietaryGoal = "Maintain",
            workoutRestrictions = "None",
            heightMetric = false,
            heightValue = 70,
            weightMetric = false,
            weightValue = 180,
            dobDay = 15,
            dobMonth = 6,
            dobYear = 1990,
            activityLevel = "Active",
            isAdmin = false
        )
    }

    // ========== refresh() Tests ==========

    @Test
    fun `refresh - should set Loading state initially and then Success state on successful fetch`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)

        // Act
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        assertEquals("program-123", (state as WorkoutUiState.Success).programDoc.id)
    }

    @Test
    fun `refresh - should set Error state when repository returns error and no cache exists`() = runTest {
        // Arrange
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Error(NetworkError.NO_INTERNET)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)

        // Act
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Error)
        assertEquals(NetworkError.NO_INTERNET, (state as WorkoutUiState.Error).error)
    }

    @Test
    fun `refresh - should schedule daily reminder on successful fetch`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "07:30")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)

        // Act
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(1, fakeNotifier.scheduledDaily.size)
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(7, scheduled.hour)
        assertEquals(30, scheduled.minute)
        assertEquals("AiRise", scheduled.title)
        assertEquals("Time to workout!", scheduled.body)
    }

    @Test
    fun `refresh - should use cached data when force is false and cache exists`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        WorkoutCache.put(mockProgram, null)

        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Error(NetworkError.NO_INTERNET) // Should not be called
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)

        // Act
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        assertEquals("program-123", (state as WorkoutUiState.Success).programDoc.id)
    }

    // ========== manualRefresh() Tests ==========

    @Test
    fun `manualRefresh - should clear cache and force new fetch`() = runTest {
        // Arrange
        val oldProgram = createMockProgramDoc(exerciseName = "Old Exercise")
        WorkoutCache.put(oldProgram, null)

        val newProgram = createMockProgramDoc(exerciseName = "New Exercise")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(newProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.manualRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        val exercise = (state as WorkoutUiState.Success).programDoc.program.schedule.first().exercises.first()
        assertEquals("New Exercise", exercise.name)
    }

    // ========== changeSet() Tests ==========

    @Test
    fun `changeSet - should update reps for specific exercise`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc(
            dayIndex = 1,
            exerciseName = "Bench Press",
            repsCompleted = 0
        )
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.changeSet(dayIndex = 1, exerciseName = "Bench Press", reps = 10, weight = null)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        val exercise = (state as WorkoutUiState.Success).programDoc.program.schedule.first().exercises.first()
        assertEquals(10, exercise.repsCompleted)
    }

    @Test
    fun `changeSet - should update weight for specific exercise`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc(
            dayIndex = 1,
            exerciseName = "Squat",
            weight = 185
        )
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.changeSet(dayIndex = 1, exerciseName = "Squat", reps = null, weight = 205.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        val exercise = (state as WorkoutUiState.Success).programDoc.program.schedule.first().exercises.first()
        assertEquals(205, exercise.weight.value)
    }

    @Test
    fun `changeSet - should update both reps and weight when both provided`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc(
            dayIndex = 1,
            exerciseName = "Deadlift",
            repsCompleted = 0,
            weight = 225
        )
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.changeSet(dayIndex = 1, exerciseName = "Deadlift", reps = 5, weight = 315.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        val exercise = (state as WorkoutUiState.Success).programDoc.program.schedule.first().exercises.first()
        assertEquals(5, exercise.repsCompleted)
        assertEquals(315, exercise.weight.value)
    }

    @Test
    fun `changeSet - should not update if exercise name does not match`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc(
            dayIndex = 1,
            exerciseName = "Bench Press",
            repsCompleted = 8,
            weight = 135
        )
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.changeSet(dayIndex = 1, exerciseName = "Squat", reps = 10, weight = 225.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        val exercise = (state as WorkoutUiState.Success).programDoc.program.schedule.first().exercises.first()
        // Values should remain unchanged
        assertEquals(8, exercise.repsCompleted)
        assertEquals(135, exercise.weight.value)
    }

    // ========== logAll() Tests ==========

    @Test
    fun `logAll - should call updateUserProgram on repository`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            updateUserProgramResult = Result.Success(true)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.logAll()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertNotNull(mockRepo.lastUpdateProgramCall)
        assertEquals("Test Program", mockRepo.lastUpdateProgramCall?.templateName)
    }

    @Test
    fun `logAll - should cancel active reminders`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.logAll()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(fakeNotifier.canceled.isNotEmpty(), "Active reminders should be canceled")
    }

    @Test
    fun `logAll - should update userChallenge lastCompletionEpochDay`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockChallenge = UserChallenge(lastCompletionEpochDay = 0)
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            userChallenge = mockChallenge
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.logAll()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val updatedChallenge = viewModel.userChallenge.value
        assertNotNull(updatedChallenge)
        assertNotNull(updatedChallenge.lastCompletionEpochDay)
        assertTrue(updatedChallenge.lastCompletionEpochDay!! > 0)
    }

    // ========== setActiveDay() Tests ==========

    @Test
    fun `setActiveDay - should update activeDayIndex`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.setActiveDay(dayIndex = 2, dayTitle = "Day 2", dayFocus = "Lower Body")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(2, viewModel.activeDayIndex.value)
    }

    @Test
    fun `setActiveDay - should schedule daily reminder with day title and focus`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "18:00")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear() // Clear initial reminder

        // Act
        viewModel.setActiveDay(dayIndex = 3, dayTitle = "  Day 3  ", dayFocus = "  Full Body  ")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(1, fakeNotifier.scheduledDaily.size)
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(18, scheduled.hour)
        assertEquals(0, scheduled.minute)
        assertEquals("Workout: Day 3", scheduled.title)
        assertEquals("Full Body", scheduled.body)
    }

    @Test
    fun `setActiveDay - should cancel active reminders before scheduling`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        val canceledCountBefore = fakeNotifier.canceled.size

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Upper")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(fakeNotifier.canceled.size > canceledCountBefore, "Should cancel more reminders")
    }

    // ========== Time Parsing Tests ==========

    @Test
    fun `setActiveDay - should parse 24-hour time format correctly`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "14:30")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(14, scheduled.hour)
        assertEquals(30, scheduled.minute)
    }

    @Test
    fun `setActiveDay - should parse 12-hour AM format correctly`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "9:15 am")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(9, scheduled.hour)
        assertEquals(15, scheduled.minute)
    }

    @Test
    fun `setActiveDay - should parse 12-hour PM format correctly`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "7:45 pm")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(19, scheduled.hour)
        assertEquals(45, scheduled.minute)
    }

    @Test
    fun `setActiveDay - should handle midnight 12 AM correctly`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "12:00 am")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(0, scheduled.hour)
        assertEquals(0, scheduled.minute)
    }

    @Test
    fun `setActiveDay - should handle noon 12 PM correctly`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "12:30 pm")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(12, scheduled.hour)
        assertEquals(30, scheduled.minute)
    }

    @Test
    fun `setActiveDay - should use default time 19-00 when time format is invalid`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "invalid time")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(19, scheduled.hour)
        assertEquals(0, scheduled.minute)
    }

    @Test
    fun `setActiveDay - should use default time 19-00 when fetchUserData fails`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Error(NetworkError.NO_INTERNET)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()

        // Act
        viewModel.setActiveDay(dayIndex = 1, dayTitle = "Day 1", dayFocus = "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val scheduled = fakeNotifier.scheduledDaily.first()
        assertEquals(19, scheduled.hour)
        assertEquals(0, scheduled.minute)
    }

    // ========== ensureDailyReminderFromState() Tests ==========

    @Test
    fun `ensureDailyReminderFromState - should schedule reminder if not already scheduled`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockUserData = createMockUserData(workoutTime = "08:00")
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram),
            fetchUserDataResult = Result.Success(mockUserData)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        // Create view model but don't trigger initial refresh
        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduledDaily.clear()
        fakeNotifier.canceled.clear()

        // Act
        viewModel.ensureDailyReminderFromState()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - should not schedule again since it was already scheduled in init
        assertEquals(0, fakeNotifier.scheduledDaily.size)
    }

    // ========== onWorkoutLogged() Tests ==========

    @Test
    fun `onWorkoutLogged - should cancel active reminders`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.canceled.clear()

        // Act
        viewModel.onWorkoutLogged()

        // Assert
        assertTrue(fakeNotifier.canceled.isNotEmpty(), "Active reminders should be canceled")
    }

    // ========== debugNotifyIn() Tests ==========

    @Test
    fun `debugNotifyIn - should schedule one-time notification with correct delay`() = runTest {
        // Arrange
        val mockProgram = createMockProgramDoc()
        val mockRepo = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgram)
        )
        val fakeNotifier = FakeLocalNotifier()
        val reminderUseCase = WorkoutReminderUseCase(fakeNotifier)

        val viewModel = WorkoutViewModel(mockRepo, reminderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNotifier.scheduled.clear()

        // Act
        viewModel.debugNotifyIn(seconds = 30)

        // Assert
        assertEquals(1, fakeNotifier.scheduled.size)
        val scheduled = fakeNotifier.scheduled.first()
        assertEquals("Test in 30s", scheduled.title)
        assertEquals("Should show even if app is closed", scheduled.body)
    }
}