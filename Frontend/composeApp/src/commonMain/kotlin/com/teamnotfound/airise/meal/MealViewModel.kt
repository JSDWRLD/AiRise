package com.teamnotfound.airise.meal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.teamnotfound.airise.meal.MealCache
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.DiaryDay
import com.teamnotfound.airise.data.serializable.FoodDiaryMonth
import com.teamnotfound.airise.data.serializable.FoodEntry
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.Meals
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 *  MealViewModel is designed to handle the following:
 * - Fetches current month's food diary via DataClient
 * - Allows previous/next day navigation using a 1-based day offset (0=yesterday, 1=today, 2=tomorrow)
 * - Add/Edit/Delete food entries, syncing the server then refreshing local month cache
 * - Computes daily totals by summing entries in a day
 */
class MealViewModel private constructor(
    private val dataClient: DataClient?,
    private val userClient: UserClient?,
    private val firebaseUser: FirebaseUser?,
    private val useNetwork: Boolean,
    startOffset: Int = 1
) {
    // Ui States
    data class UiState(
        val dayOffset: Int,     // 1=today, 0=yesterday, 2=tomorrow
        val goal: Int,
        val exercise: Int,
        val day: DiaryDay,      // contains Meals
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    // Date Helpers
    private val tz: TimeZone = TimeZone.currentSystemDefault()
    private fun today(): LocalDate = Clock.System.todayIn(tz)
    private fun offsetToDate(offset: Int, base: LocalDate = today()): LocalDate =
        base.plus((offset - 1), DateTimeUnit.DAY)
    private fun dateToOffset(date: LocalDate, base: LocalDate = today()): Int =
        1 + base.daysUntil(date)

    // Month Cache Logic
    private val months = mutableMapOf<Pair<Int, Int>, FoodDiaryMonth>()
    private fun monthKey(date: LocalDate) = date.year to date.monthNumber

    private fun emptyMonth(date: LocalDate): FoodDiaryMonth =
        FoodDiaryMonth(
            id = null,
            userId = firebaseUser?.uid ?: "local",
            year = date.year,
            month = date.monthNumber,
            days = List(31) { null } // index 0..30 => day 1..31
        )

    private fun getOrCreateMonth(date: LocalDate): FoodDiaryMonth =
        months.getOrPut(monthKey(date)) { emptyMonth(date) }

    private fun readDay(date: LocalDate): DiaryDay {
        val idx = date.dayOfMonth - 1
        val monthDoc = if (useNetwork) {
            MealCache.snapshotMonth(date.year, date.monthNumber) ?: emptyMonth(date)
        } else {
            getOrCreateMonth(date)
        }
        return monthDoc.days[idx] ?: DiaryDay(day = date.dayOfMonth)
    }

    private fun writeDay(date: LocalDate, newDay: DiaryDay) {
        val idx = date.dayOfMonth - 1
        if (useNetwork) {
            val monthDoc = MealCache.snapshotMonth(date.year, date.monthNumber) ?: emptyMonth(date)
            val newDays = monthDoc.days.toMutableList()
            while (newDays.size <= idx) newDays.add(null)
            newDays[idx] = newDay
            val newMonth = monthDoc.copy(days = newDays)

            _ui = _ui.copy(day = newDay)
            scope.launch { MealCache.putMonth(newMonth) }
        } else {
            val m = getOrCreateMonth(date)
            val newDays = m.days.toMutableList().apply { this[idx] = newDay }
            months[monthKey(date)] = m.copy(days = newDays)
            _ui = _ui.copy(day = newDay)
        }
    }


    // UI States
    private var _ui by mutableStateOf(
        run {
            val d = offsetToDate(startOffset)
            UiState(
                dayOffset = startOffset,
                goal = 2000, // Default
                exercise = 0,
                day = readDay(d),
                isLoading = useNetwork,
                errorMessage = null
            )
        }
    )
    val uiState: UiState get() = _ui
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        if (useNetwork) {
            // Load the current month up-front
            val date = offsetToDate(_ui.dayOffset)
            refreshMonth(date)
            loadGoalAndExerciseFromHealth()
        }
    }

    //
    val totalFood: Int
        get() = (_ui.day.meals.breakfast + _ui.day.meals.lunch + _ui.day.meals.dinner)
            .sumOf { entry: FoodEntry -> entry.calories }
            .toInt()

    val remaining: Int
        get() = (_ui.goal - totalFood + _ui.exercise).coerceAtLeast(0)

    // Set the current day offset
    fun setDayOffset(newOffset: Int) {
        val date = offsetToDate(newOffset)
        _ui = _ui.copy(dayOffset = newOffset, day = readDay(date), errorMessage = null)
        if (useNetwork) refreshMonth(date)
    }

    fun previousDay() = setDayOffset(_ui.dayOffset - 1)
    fun nextDay() = setDayOffset(_ui.dayOffset + 1)

    // Set the daily calorie goal
    fun setGoal(goal: Int) {
        if (!useNetwork || userClient == null || firebaseUser == null) return
        _ui = _ui.copy(isLoading = true, errorMessage = null)
        scope.launch {
            val res = userClient.updateHealthData(firebaseUser, HealthData(caloriesTarget = goal))
            _ui = when (res) {
                is Result.Success -> {
                    MealCache.clearHealth()
                    _ui.copy(goal = goal, isLoading = false, errorMessage = null)
                }

                is Result.Error -> {
                    _ui.copy(isLoading = false, errorMessage = networkErrorMessage(res.error))
                }
            }
        }
    }

    private fun loadGoalAndExerciseFromHealth(force: Boolean = false) {
        if (!useNetwork || userClient == null || firebaseUser == null) return

        // Fast path
        if (!force) {
            MealCache.snapshotHealth()?.let { snap ->
                _ui = _ui.copy(goal = snap.goalCalories, exercise = snap.caloriesBurned ?: _ui.exercise, isLoading = false, errorMessage = null)
                return
            }
        }

        _ui = _ui.copy(isLoading = true, errorMessage = null)
        scope.launch {
            when (val res = MealCache.getOrFetchHealth(
                userClient = userClient!!,
                user = firebaseUser!!,
                defaultGoal = 2000,
                force = force
            )) {
                is Result.Success -> {
                    val snap = res.data
                    _ui = _ui.copy(goal = snap.goalCalories, exercise = snap.caloriesBurned ?: _ui.exercise, isLoading = false, errorMessage = null)
                }
                is Result.Error -> {
                    _ui = _ui.copy(isLoading = false, errorMessage = networkErrorMessage(res.error))
                }
            }
        }
    }

    fun manualRefresh() {
        val date = offsetToDate(_ui.dayOffset)
        MealCache.clearMonth(date.year, date.monthNumber)
        MealCache.clearHealth()
        refreshMonth(date, force = true)
        loadGoalAndExerciseFromHealth(force = true)
    }

    // Sync and refresh the current month
    private fun refreshMonth(date: LocalDate, force: Boolean = false) {
        if (!useNetwork || dataClient == null || firebaseUser == null) {
            // still serve snapshot if we have it
            MealCache.snapshotMonth(date.year, date.monthNumber)?.let {
                _ui = _ui.copy(day = readDay(date), isLoading = false, errorMessage = null)
            }
            return
        }

        //serve cache immediately
        if (!force) {
            MealCache.snapshotMonth(date.year, date.monthNumber)?.let {
                _ui = _ui.copy(day = readDay(date), isLoading = false, errorMessage = null)
                return
            }
        }

        _ui = _ui.copy(isLoading = true, errorMessage = null)
        scope.launch {
            when (val res = MealCache.getOrFetchMonth(
                dataClient = dataClient!!,
                user = firebaseUser!!,
                year = date.year,
                month = date.monthNumber,
                force = force
            )) {
                is Result.Success -> {
                    _ui = _ui.copy(day = readDay(date), isLoading = false, errorMessage = null)
                }
                is Result.Error -> {
                    _ui = _ui.copy(isLoading = false, errorMessage = networkErrorMessage(res.error))
                }
            }
        }
    }


    private fun syncAndRefresh(date: LocalDate, block: suspend () -> Result<Unit, NetworkError>) {
        if (!useNetwork || dataClient == null || firebaseUser == null) return
        _ui = _ui.copy(isLoading = true, errorMessage = null)
        scope.launch {
            val res = block()
            when (res) {
                is Result.Success -> refreshMonth(date, force = true) // this will also turn off loading
                is Result.Error -> _ui = _ui.copy(isLoading = false, errorMessage = networkErrorMessage(res.error))
            }
        }
    }

    /* Add / Edit / Delete food entries */
    fun addQuickFood(
        meal: MealType,
        calories: Int,
        name: String,
        serving: String,
        fats: Double,
        carbs: Double,
        proteins: Double
    ) {
        val date = offsetToDate(_ui.dayOffset)
        val entry = FoodEntry(
            id = randomId(),
            name = if (name.isBlank()) "Quick Add" else name,
            calories = calories.toDouble(),
            fats = fats,
            carbs = carbs,
            proteins = proteins
        )

        if (useNetwork && dataClient != null && firebaseUser != null) {
            val mealPath = when (meal) {
                MealType.Breakfast -> "breakfast"
                MealType.Lunch -> "lunch"
                MealType.Dinner -> "dinner"
            }
            syncAndRefresh(date) {
                dataClient.addFoodEntry(firebaseUser, date.year, date.monthNumber, date.dayOfMonth, mealPath, entry)
            }
        } else {
            // Write locally only
            val day = readDay(date)
            val m = day.meals
            val newMeals = when (meal) {
                MealType.Breakfast -> m.copy(breakfast = m.breakfast + entry)
                MealType.Lunch -> m.copy(lunch = m.lunch + entry)
                MealType.Dinner -> m.copy(dinner = m.dinner + entry)
            }
            val newTotal: Double = (newMeals.breakfast + newMeals.lunch + newMeals.dinner)
                .sumOf { e: FoodEntry -> e.calories }
            val newDay = day.copy(meals = newMeals, totalCalories = newTotal)
            writeDay(date, newDay)
            _ui = _ui.copy(day = newDay)
        }
    }

    fun editEntry(entryId: String, updated: FoodEntry) {
        val date = offsetToDate(_ui.dayOffset)
        if (useNetwork && dataClient != null && firebaseUser != null) {
            syncAndRefresh(date) { dataClient.editFoodEntry(firebaseUser, entryId, updated) }
        } else {
            // Replace matching id in local cache
            val day = readDay(date)
            val m = day.meals
            fun List<FoodEntry>.replace(): List<FoodEntry> = map { if (it.id == entryId) updated else it }
            val newMeals = Meals(
                breakfast = m.breakfast.replace(),
                lunch = m.lunch.replace(),
                dinner = m.dinner.replace()
            )
            val newTotal: Double = (newMeals.breakfast + newMeals.lunch + newMeals.dinner)
                .sumOf { e: FoodEntry -> e.calories }
            val newDay = day.copy(meals = newMeals, totalCalories = newTotal)
            writeDay(date, newDay)
            _ui = _ui.copy(day = newDay)
        }
    }

    fun deleteEntry(entryId: String) {
        val date = offsetToDate(_ui.dayOffset)
        if (useNetwork && dataClient != null && firebaseUser != null) {
            syncAndRefresh(date) { dataClient.deleteFoodEntry(firebaseUser, entryId) }
        } else {
            // Remove matching id in local cache
            val day = readDay(date)
            val m = day.meals
            fun List<FoodEntry>.remove(): List<FoodEntry> = filter { it.id != entryId }
            val newMeals = Meals(
                breakfast = m.breakfast.remove(),
                lunch = m.lunch.remove(),
                dinner = m.dinner.remove()
            )
            val newTotal: Double = (newMeals.breakfast + newMeals.lunch + newMeals.dinner)
                .sumOf { e: FoodEntry -> e.calories }
            val newDay = day.copy(meals = newMeals, totalCalories = newTotal)
            writeDay(date, newDay)
            _ui = _ui.copy(day = newDay)
        }
    }

    // ----- Errors -----
    private fun networkErrorMessage(err: NetworkError): String = when (err) {
        NetworkError.NO_INTERNET      -> "You're offline. Changes will sync when back online."
        NetworkError.SERIALIZATION    -> "Data error. Please try again."
        NetworkError.SERVER_ERROR     -> "Server error. Please try again later."
        NetworkError.BAD_REQUEST      -> "Bad request."
        NetworkError.CONFLICT         -> "Conflict. Please refresh."
        NetworkError.UNAUTHORIZED     -> "Please sign in again."
        NetworkError.REQUEST_TIMEOUT  -> "Request timed out. Please try again."
        NetworkError.TOO_MANY_REQUESTS-> "Too many requests. Please slow down."
        NetworkError.PAYLOAD_TOO_LARGE-> "Payload too large."
        NetworkError.UNKNOWN          -> "Unknown error."
        NetworkError.FORBIDDEN -> TODO()
    }

    companion object {
        /** Real, network-backed VM */
        fun network(
            dataClient: DataClient,
            userClient: UserClient,
            firebaseUser: FirebaseUser,
            startOffset: Int = 1,
        ): MealViewModel = MealViewModel(
            dataClient = dataClient,
            userClient = userClient,
            firebaseUser = firebaseUser,
            useNetwork = true,
            startOffset = startOffset
        )

        /** Offline preview VM */
        fun fake(startOffset: Int = 1, startGoal: Int = 1900): MealViewModel {
            MealCache.clearAll()
            return MealViewModel(
                dataClient = null,
                userClient =  null,
                firebaseUser = null,
                useNetwork = false,
                startOffset = startOffset,
            )
        }


        private fun randomId(): String =
            kotlin.random.Random.nextBytes(8).joinToString("") { b ->
                b.toUByte().toString(16).padStart(2, '0')
            }
    }
}

enum class MealType { Breakfast, Lunch, Dinner }
