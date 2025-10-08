package notifications

import kotlin.test.Test
import kotlin.test.assertEquals

class MealReminderUseCaseTest {

    @Test
    fun scheduleDailyMeals_schedulesThreeFixedTimes() {
        val fake = FakeLocalNotifier()
        val meals = MealReminderUseCase(fake)

        meals.scheduleDailyMeals(
            breakfastHour = 7,  breakfastMinute = 15,
            lunchHour = 12,     lunchMinute = 0,
            dinnerHour = 18,    dinnerMinute = 30
        )

        assertEquals(3, fake.dailyCalls.size)

        val times = fake.dailyCalls.map { it.hour to it.minute }
        assertEquals(listOf(7 to 15, 12 to 0, 18 to 30), times)

        val titles = fake.dailyCalls.map { it.title }
        assertEquals(listOf("Breakfast log", "Lunch log", "Dinner log"), titles)
    }
}
