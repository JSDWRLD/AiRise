package notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class WaterReminderUseCaseTest {

    @Test
    fun scheduleDailyEvery_120min_from9to21_makes7Reminders() {
        val fake = FakeLocalNotifier()
        val useCase = WaterReminderUseCase(fake)

        useCase.scheduleEvery2h(
            startHour = 9, startMinute = 0,
            endHour = 21, endMinute = 0,
            intervalMinutes = 120
        )

        assertEquals(7, fake.dailyCalls.size)
        val expected = listOf(9 to 0, 11 to 0, 13 to 0, 15 to 0, 17 to 0, 19 to 0, 21 to 0)
        assertEquals(expected, fake.dailyCalls.map { it.hour to it.minute })

        val ids = fake.dailyCalls.map { it.id }
        assertEquals((4000..4006).toList(), ids)
    }

    @Test
    fun scheduleDailyEvery_30min_advancesCorrectly() {
        val fake = FakeLocalNotifier()
        val useCase = WaterReminderUseCase(fake)

        useCase.scheduleEvery2h(
            startHour = 10, startMinute = 0,
            endHour = 11, endMinute = 30,
            intervalMinutes = 30
        )

        val expected = listOf(10 to 0, 10 to 30, 11 to 0, 11 to 30)
        assertEquals(expected, fake.dailyCalls.map { it.hour to it.minute })
    }

    @Test
    fun scheduleDailyEvery_23min_advancesCorrectly() {
        val fake = FakeLocalNotifier()
        val useCase = WaterReminderUseCase(fake)

        useCase.scheduleEvery2h(
            startHour = 9, startMinute = 0,
            endHour = 9, endMinute = 69,
            intervalMinutes = 23
        )
        val expected = listOf(9 to 0, 9 to 23, 9 to 46)
        assertEquals(expected, fake.dailyCalls.map { it.hour to it.minute })
    }

    @Test
    fun scheduleDailyEvery_invalidInterval_throws() {
        val fake = FakeLocalNotifier()
        val useCase = WaterReminderUseCase(fake)

        assertFailsWith<IllegalArgumentException> {
            useCase.scheduleEvery2h(intervalMinutes = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            useCase.scheduleEvery2h(intervalMinutes = 2000)
        }
    }

    @Test
    fun cancelAll_cancelsReasonableRange() {
        val fake = FakeLocalNotifier()
        val useCase = WaterReminderUseCase(fake)

        useCase.scheduleEvery2h(
            startHour = 9, endHour = 10, intervalMinutes = 60
        )
        useCase.cancelAll()

        val scheduledIds = fake.dailyCalls.map { it.id }.toSet()
        val canceledIds = fake.canceledIds.toSet()
        assertTrue(scheduledIds.all { it in canceledIds })
    }
}
