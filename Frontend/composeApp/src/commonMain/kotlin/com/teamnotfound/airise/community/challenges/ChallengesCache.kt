package com.teamnotfound.airise.community.challenges

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


object ChallengesCache {
    private val lock = Mutex()

    private var items: List<ChallengeUI>? = null
    private var progress: UserChallengeProgressUI? = null

    fun snapshot(): Pair<List<ChallengeUI>?, UserChallengeProgressUI?> = items to progress

    fun clear() {
        items = null
        progress = null
    }

    suspend fun put(newItems: List<ChallengeUI>?, newProgress: UserChallengeProgressUI?) = lock.withLock {
        if (newItems != null) items = newItems
        if (newProgress != null) progress = newProgress
    }


    suspend fun getOrFetch(
        dataClient: DataClient,
        userClient: UserClient,
        user: FirebaseUser,
        force: Boolean = false
    ): Result<Pair<List<ChallengeUI>, UserChallengeProgressUI>, NetworkError> = lock.withLock {
        if (!force && items != null && progress != null) {
            return Result.Success(items!! to progress!!)
        }

        val fetchedItems = when (val r = dataClient.getChallenges()) {
            is Result.Success -> r.data.map { it.toUI() }
            is Result.Error   -> return if (items != null && progress != null) {
                Result.Success(items!! to progress!!)
            } else Result.Error(r.error)
        }

        val fetchedProgress = when (val pr = userClient.getUserChallenges(user)) {
            is Result.Success -> {
                val dto = pr.data
                UserChallengeProgressUI(
                    activeChallengeId = dto.activeChallengeId?.ifBlank { null },
                    lastCompletionEpochDay = dto.lastCompletionEpochDay.takeIf { it != 0L }
                )
            }
            is Result.Error -> UserChallengeProgressUI()
        }

        val completedToday = when (val ct = userClient.hasCompletedToday(user)) {
            is Result.Success -> ct.data
            is Result.Error   -> false
        }

        val mergedProgress = fetchedProgress.copy(completedToday = completedToday)

        items = fetchedItems
        progress = mergedProgress
        Result.Success(fetchedItems to mergedProgress)
    }
}
