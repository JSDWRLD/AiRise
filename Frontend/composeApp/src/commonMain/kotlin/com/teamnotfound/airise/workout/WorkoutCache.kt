package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.data.serializable.UserChallenge
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WorkoutCache {
    private val lock = Mutex()
    private var program: UserProgramDoc? = null
    private var challenge: UserChallenge? = null

    suspend fun getOrFetch(repo: IUserRepository, force: Boolean = false): Result<Pair<UserProgramDoc, UserChallenge?>, NetworkError> =
        lock.withLock {
            if (!force && program != null) {
                return Result.Success(program!! to challenge)
            }

            // fetch from network
            when (val r = repo.getUserProgram()) {
                is Result.Error -> {
                    // if we already had something cached, serve it; else bubble error
                    if (program != null) Result.Success(program!! to challenge)
                    else Result.Error(r.error)
                }
                is Result.Success -> {
                    program = r.data
                    // challenge is best-effort; keep old on error
                    challenge = try { repo.getUserChallengeOrNull() } catch (_: Throwable) { challenge }
                    Result.Success(program!! to challenge)
                }
            }
        }

    fun snapshot(): Pair<UserProgramDoc?, UserChallenge?> = program to challenge
    suspend fun clear() = lock.withLock { program = null; challenge = null }

    suspend fun put(programDoc: UserProgramDoc, challenge: UserChallenge?) = lock.withLock {
        program = programDoc
        this.challenge = challenge
    }
}
