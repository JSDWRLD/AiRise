package com.teamnotfound.airise.meal

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.FoodDiaryMonth
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Process-wide, in-memory cache for Meals screen.
 * - Survives VM recreation and screen hops
 * - Thread-safe (Mutex)
 */
object MealCache {
    private val lock = Mutex()

    // Month diary cache keyed by (year, month)
    private val months = mutableMapOf<Pair<Int, Int>, FoodDiaryMonth>()

    // Health snapshot cache (goal calories, calories burned)
    data class HealthSnapshot(val goalCalories: Int, val caloriesBurned: Int?)
    private var health: HealthSnapshot? = null

    fun snapshotMonth(year: Int, month: Int): FoodDiaryMonth? = months[year to month]
    fun snapshotHealth(): HealthSnapshot? = health

    suspend fun putMonth(monthDoc: FoodDiaryMonth) = lock.withLock {
        months[monthDoc.year to monthDoc.month] = monthDoc
    }
    suspend fun putHealth(snapshot: HealthSnapshot) = lock.withLock {
        health = snapshot
    }

    fun clearMonth(year: Int, month: Int) {
        months.remove(year to month)
    }
    fun clearHealth() { health = null }
    fun clearAll() {
        months.clear()
        health = null
    }

    suspend fun getOrFetchMonth(
        dataClient: DataClient,
        user: FirebaseUser,
        year: Int,
        month: Int,
        force: Boolean = false
    ): Result<FoodDiaryMonth, NetworkError> = lock.withLock {
        val key = year to month
        if (!force) {
            months[key]?.let { return Result.Success(it) }
        }
        return when (val res = dataClient.getFoodDiaryMonth(user, year, month)) {
            is Result.Success -> {
                months[key] = res.data
                Result.Success(res.data)
            }
            is Result.Error -> {
                months[key]?.let { Result.Success(it) } ?: Result.Error(res.error)
            }
        }
    }

    suspend fun getOrFetchHealth(
        userClient: UserClient,
        user: FirebaseUser,
        defaultGoal: Int = 2000,
        force: Boolean = false
    ): Result<HealthSnapshot, NetworkError> = lock.withLock {
        if (!force) {
            health?.let { return Result.Success(it) }
        }
        return when (val res = userClient.getHealthData(user)) {
            is Result.Success -> {
                val goal = res.data.caloriesTarget ?: defaultGoal
                val snap = HealthSnapshot(goalCalories = goal, caloriesBurned = res.data.caloriesBurned)
                health = snap
                Result.Success(snap)
            }
            is Result.Error -> {
                health?.let { Result.Success(it) } ?: Result.Error(res.error)
            }
        }
    }
}
