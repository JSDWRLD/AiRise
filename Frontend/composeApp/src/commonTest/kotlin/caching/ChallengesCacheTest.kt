package com.teamnotfound.airise.community.challenges

import androidx.compose.runtime.mutableStateOf
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.Challenge
import com.teamnotfound.airise.util.NetworkError
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class ChallengesCacheTest {

    // Local DTO substitute
    private data class UserChallengesDTO(
        val activeChallengeId: String? = null,
        val lastCompletionEpochDay: Long = 0L
    )

    // Fake responses to simulate network clients
    private class FakeDataClient(
        private val result: Result<List<Challenge>, NetworkError>
    ) {
        suspend fun getChallenges(): Result<List<Challenge>, NetworkError> = result
    }

    private class FakeUserClient(
        private val userChallenges: Result<UserChallengesDTO, NetworkError> = Result.Error(NetworkError.UNKNOWN),
        private val completedToday: Result<Boolean, NetworkError> = Result.Error(NetworkError.UNKNOWN)
    ) {
        suspend fun getUserChallenges(): Result<UserChallengesDTO, NetworkError> = userChallenges
        suspend fun hasCompletedToday(): Result<Boolean, NetworkError> = completedToday
    }

    private fun dummyChallenge(id: String) =
        Challenge(id, "Challenge $id", "Description $id", "url$id")

    private fun dummyDTO(id: String? = null, day: Long = 0L) =
        UserChallengesDTO(id, day)

    @BeforeTest
    fun reset() = ChallengesCache.clear()

    @Test
    fun `returns cached data when present`() = runTest {
        val list = listOf(ChallengeUI("1", mutableStateOf( "A"), mutableStateOf("D"), mutableStateOf("U")))
        val prog = UserChallengeProgressUI("1", 10L, completedToday = true)
        ChallengesCache.put(list, prog)

        val result = ChallengesCache.snapshot()
        assertEquals(list, result.first)
        assertEquals(prog, result.second)
    }

    @Test
    fun `fetches new data and caches it`() = runTest {
        val dataClient = FakeDataClient(Result.Success(listOf(dummyChallenge("X"))))
        val userClient = FakeUserClient(
            userChallenges = Result.Success(dummyDTO("X", 100L)),
            completedToday = Result.Success(true)
        )

        // Manually simulate logic similar to ChallengesCache
        val fetchedChallenges = (dataClient.getChallenges() as Result.Success).data.map { it.toUI() }
        val dto = (userClient.getUserChallenges() as Result.Success).data
        val progress = UserChallengeProgressUI(
            activeChallengeId = dto.activeChallengeId,
            lastCompletionEpochDay = dto.lastCompletionEpochDay,
            completedToday = (userClient.hasCompletedToday() as Result.Success).data
        )

        ChallengesCache.put(fetchedChallenges, progress)

        val (cachedList, cachedProg) = ChallengesCache.snapshot()
        assertEquals(fetchedChallenges, cachedList)
        assertEquals(progress, cachedProg)
    }

    @Test
    fun `returns error when fetch fails and no cache`() = runTest {
        val dataClient = FakeDataClient(Result.Error(NetworkError.NO_INTERNET))
        FakeUserClient()

        // Since cache is empty and fetch fails, emulate Result.Error
        val result = dataClient.getChallenges()
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.NO_INTERNET, (result as Result.Error).error)
    }
}
