package com.teamnotfound.airise.meal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import com.teamnotfound.airise.data.serializable.FoodDiaryMonth
import com.teamnotfound.airise.data.serializable.DiaryDay
import com.teamnotfound.airise.data.serializable.Meals
import com.teamnotfound.airise.data.serializable.FoodEntry

//fake vm for foodlog screen
//offset 1= today, 0 = yesterday, 2 = tomorrow
class MealViewModel private constructor(
    startOffset: Int = 1,
    startGoal: Int = 1900
) {

    data class UiState(
        val dayOffset: Int,     // 1=today, 0=yesterday, 2=tomorrow
        val goal: Int,
        val exercise: Int,
        val day: DiaryDay       // contains Meals
    )

    //date helpers
    private val tz: TimeZone = TimeZone.currentSystemDefault()
    private fun today(): LocalDate = Clock.System.todayIn(tz)
    private fun offsetToDate(offset: Int, base: LocalDate = today()): LocalDate =
        base.plus((offset - 1), DateTimeUnit.DAY)
    private fun dateToOffset(date: LocalDate, base: LocalDate = today()): Int =
        1 + base.daysUntil(date)

    //month
    private val months = mutableMapOf<Pair<Int, Int>, FoodDiaryMonth>()
    private fun monthKey(date: LocalDate) = date.year to date.monthNumber

    private fun getOrCreateMonth(date: LocalDate): FoodDiaryMonth =
        months.getOrPut(monthKey(date)) {
            FoodDiaryMonth(
                id = null,
                userId = "local",
                year = date.year,
                month = date.monthNumber,
                days = List(31) { null } // index 0..30 => day 1..31
            )
        }

    private fun readDay(date: LocalDate): DiaryDay {
        val m = getOrCreateMonth(date)
        val idx = date.dayOfMonth - 1
        return m.days[idx] ?: DiaryDay(day = date.dayOfMonth)
    }

    private fun writeDay(date: LocalDate, newDay: DiaryDay) {
        val m = getOrCreateMonth(date)
        val idx = date.dayOfMonth - 1
        // IMPORTANT: copy to a mutable list, write, then put back
        val newDays = m.days.toMutableList().apply { this[idx] = newDay }
        months[monthKey(date)] = m.copy(days = newDays)
    }

    //ex ui state
    private var _ui by mutableStateOf(
        run {
            val d = offsetToDate(startOffset)
            UiState(
                dayOffset = startOffset,
                goal = startGoal,
                exercise = 0,
                day = readDay(d)
            )
        }
    )
    val uiState: UiState get() = _ui

    //total
    val totalFood: Int
        get() = (_ui.day.meals.breakfast + _ui.day.meals.lunch + _ui.day.meals.dinner)
            .sumOf { entry: FoodEntry -> entry.calories }
            .toInt()

    val remaining: Int
        get() = (_ui.goal - totalFood + _ui.exercise).coerceAtLeast(0)

    //navigation
    fun setDayOffset(newOffset: Int) {
        val date = offsetToDate(newOffset)
        _ui = _ui.copy(dayOffset = newOffset, day = readDay(date))
    }

    fun previousDay() = setDayOffset(_ui.dayOffset - 1)
    fun nextDay() = setDayOffset(_ui.dayOffset + 1)

    //goal
    fun setGoal(goal: Int) {
        _ui = _ui.copy(goal = goal.coerceAtLeast(0))
    }

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
        val day = readDay(date)

        val entry = FoodEntry(
            id = randomId(),
            name = if (name.isBlank()) "Quick Add" else name,
            calories = calories.toDouble(),
            fats = fats,
            carbs = carbs,
            proteins = proteins
        )

        val m = day.meals
        val newMeals = when (meal) {
            MealType.Breakfast -> m.copy(breakfast = m.breakfast + entry)
            MealType.Lunch     -> m.copy(lunch     = m.lunch + entry)
            MealType.Dinner    -> m.copy(dinner    = m.dinner + entry)
        }

        val newTotal = (newMeals.breakfast + newMeals.lunch + newMeals.dinner)
            .sumOf { e: FoodEntry -> e.calories }

        val newDay = day.copy(
            meals = newMeals,
            totalCalories = newTotal
        )

        writeDay(date, newDay)
        _ui = _ui.copy(day = newDay)
    }

    companion object {
        fun fake(): MealViewModel = MealViewModel()
        private fun randomId(): String =
            kotlin.random.Random.nextBytes(8).joinToString("") { b ->
                b.toUByte().toString(16).padStart(2, '0')
            }
    }
}

enum class MealType { Breakfast, Lunch, Dinner }
