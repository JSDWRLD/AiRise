package notifications

import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class MealReminderUseCase(private val notifier: LocalNotifier) {
    private val idBreakfast = 3001
    private val idLunch     = 3002
    private val idDinner    = 3003

    fun scheduleDailyMeals(
        breakfastHour: Int = 8,  breakfastMinute: Int = 0, // For now I will keep the default values
        lunchHour: Int = 12,     lunchMinute: Int = 0, //until we implement and type of preference schedules
        dinnerHour: Int = 19,    dinnerMinute: Int = 0
    ) {
        notifier.cancel(idBreakfast)
        notifier.cancel(idLunch)
        notifier.cancel(idDinner)

        notifier.scheduleDaily(idBreakfast, "Breakfast log", "Log your breakfast", breakfastHour, breakfastMinute)

        notifier.scheduleDaily(idLunch, "Lunch log", "Log your lunch", lunchHour, lunchMinute)
        notifier.scheduleDaily(idDinner, "Dinner log", "Log your dinner", dinnerHour, dinnerMinute)
    }

    fun cancelBreakfast() = notifier.cancel(idBreakfast)
    fun cancelLunch()     = notifier.cancel(idLunch)
    fun cancelDinner()    = notifier.cancel(idDinner)
}

