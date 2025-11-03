package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.data.serializable.UserChallenge
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class WorkoutCacheTest {

    private class FakeRepo(
        private val programResult: Result<UserProgramDoc, NetworkError>,
        private val challengeResult: UserChallenge? = null
    ) : IUserRepository {

        override suspend fun getUserProgram(): Result<UserProgramDoc, NetworkError> = programResult
        override suspend fun getUserChallengeOrNull(): UserChallenge? = challengeResult

        override suspend fun fetchUserData() =
            Result.Error(NetworkError.UNKNOWN)

        override suspend fun searchUsers(query: String) =
            Result.Error(NetworkError.UNKNOWN)

        override suspend fun updateUserProgram(userProgram: com.teamnotfound.airise.data.serializable.UserProgram) =
            Result.Error(NetworkError.UNKNOWN)

        override suspend fun getHealthData() =
            Result.Error(NetworkError.UNKNOWN)

        override suspend fun updateHealthData(healthData: com.teamnotfound.airise.data.serializable.HealthData) =
            Result.Error(NetworkError.UNKNOWN)
    }



    @BeforeTest
    fun setup() = runTest {
        WorkoutCache.clear()
    }

    @Test
    fun `returns cached program when not forced`() = runTest {
        val program = UserProgramDoc("P1", "Strength")
        val challenge = UserChallenge("C1", "Challenge1")
        WorkoutCache.put(program, challenge)

        val fakeRepo = FakeRepo(Result.Error(NetworkError.SERVER_ERROR)) // should not be called
        val res = WorkoutCache.getOrFetch(fakeRepo, force = false)

        assertTrue(res is Result.Success)
        val (cachedProgram, cachedChallenge) = (res as Result.Success).data
        assertEquals(program, cachedProgram)
        assertEquals(challenge, cachedChallenge)
    }

    @Test
    fun `fetches and caches from repo on success`() = runTest {
        val program = UserProgramDoc("P2", "Cardio")
        val challenge = UserChallenge("C2", "Stamina")
        val fakeRepo = FakeRepo(Result.Success(program), challenge)

        val res = WorkoutCache.getOrFetch(fakeRepo)
        assertTrue(res is Result.Success)
        val (prog, chal) = (res as Result.Success).data
        assertEquals(program, prog)
        assertEquals(challenge, chal)

        // Check that it was cached
        val (snapProg, snapChal) = WorkoutCache.snapshot()
        assertEquals(program, snapProg)
        assertEquals(challenge, snapChal)
    }

    @Test
    fun `returns error if no cache and repo fails`() = runTest {
        val fakeRepo = FakeRepo(Result.Error(NetworkError.NO_INTERNET))
        val res = WorkoutCache.getOrFetch(fakeRepo)
        assertTrue(res is Result.Error)
        assertEquals(NetworkError.NO_INTERNET, (res as Result.Error).error)
    }

    @Test
    fun `returns cached data when repo fails but cache exists`() = runTest {
        val program = UserProgramDoc("P3", "Yoga")
        WorkoutCache.put(program, null)
        val fakeRepo = FakeRepo(Result.Error(NetworkError.SERVER_ERROR))

        val res = WorkoutCache.getOrFetch(fakeRepo)
        assertTrue(res is Result.Success)
        val (prog, chal) = (res as Result.Success).data
        assertEquals(program, prog)
        assertNull(chal)
    }
}
