// MealCacheTest.kt
import com.teamnotfound.airise.meal.*
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.*
import com.teamnotfound.airise.util.NetworkError
import kotlin.test.*
import kotlinx.coroutines.test.*

class MealCacheTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun reset() = MealCache.clearAll()

    private fun month(y: Int = 2025, m: Int = 1) =
        FoodDiaryMonth(id=null, userId="u", year=y, month=m, days=List(31){ null })

    @Test
    fun month_cache_hit_then_force_refresh() = runTest(dispatcher) {
        val y = 2025; val m = 3
        // 1st fetch -> miss -> store M1
        val m1 = month(y,m).copy(id="M1")
        val r1 = MealCache.getOrFetchMonth(y,m,false) { Result.Success(m1) }
        assertTrue(r1 is Result.Success); assertEquals("M1", r1.data.id)

        // 2nd fetch (no force) -> hit M1, fetch not called
        var called = false
        val r2 = MealCache.getOrFetchMonth(y,m,false) { called = true; Result.Error(NetworkError.UNKNOWN) }
        assertTrue(r2 is Result.Success); assertEquals("M1", r2.data.id); assertFalse(called)

        // 3rd fetch (force) -> call fetch -> replace with M2
        val m2 = month(y,m).copy(id="M2")
        val r3 = MealCache.getOrFetchMonth(y,m,true) { Result.Success(m2) }
        assertTrue(r3 is Result.Success); assertEquals("M2", r3.data.id)
    }

    @Test
    fun month_fallback_to_cached_on_error() = runTest(dispatcher) {
        val y=2025; val m=4
        val m1 = month(y,m).copy(id="M1")
        MealCache.putMonth(m1)

        val r = MealCache.getOrFetchMonth(y,m,false) { Result.Error(NetworkError.SERVER_ERROR) }
        assertTrue(r is Result.Success)
        assertEquals("M1", r.data.id)
    }

    @Test
    fun health_cache_and_force_refresh() = runTest(dispatcher) {
        // 1st: MISS -> store goal 1800
        val h1 = HealthData(caloriesTarget = 1800, caloriesBurned = 200)
        val r1 = MealCache.getOrFetchHealth(defaultGoal = 2000, force=false) { Result.Success(h1) }
        assertTrue(r1 is Result.Success); assertEquals(1800, r1.data.goalCalories); assertEquals(200, r1.data.caloriesBurned)

        // 2nd: HIT -> don’t call fetch
        var called = false
        val r2 = MealCache.getOrFetchHealth(defaultGoal = 2000, force=false) {
            called = true; Result.Error(NetworkError.UNKNOWN)
        }
        assertTrue(r2 is Result.Success); assertFalse(called)

        // 3rd: FORCE -> overwrite with goal 1900
        val h2 = HealthData(caloriesTarget = 1900, caloriesBurned = 150)
        val r3 = MealCache.getOrFetchHealth(defaultGoal = 2000, force=true) { Result.Success(h2) }
        assertTrue(r3 is Result.Success); assertEquals(1900, r3.data.goalCalories); assertEquals(150, r3.data.caloriesBurned)
    }
}
