package notifications

import kotlin.test.Test
import kotlin.test.assertEquals

class NudgeReminderUseCaseTest {

    @Test
    fun scheduleDailyLogin_and_Challenge() {
        val fake = FakeLocalNotifier()
        val nudge = NudgeReminderUseCase(fake)

        nudge.scheduleDailyLogin(hour = 10, minute = 5)
        nudge.scheduleDailyChallenge(hour = 18, minute = 40)

        assertEquals(
            listOf("Hi from AiRise", "Challenge time"),
            fake.dailyCalls.map { it.title }
        )
        assertEquals(
            listOf(10 to 5, 18 to 40),
            fake.dailyCalls.map { it.hour to it.minute }
        )
    }
}
