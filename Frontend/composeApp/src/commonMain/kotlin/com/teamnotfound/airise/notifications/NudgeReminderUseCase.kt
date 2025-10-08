package notifications

class NudgeReminderUseCase(private val notifier: LocalNotifier) {
    private val loginId     = 5001
    private val challengeId = 5002

    fun scheduleDailyLogin(hour: Int = 10, minute: Int = 0) {
        notifier.cancel(loginId)
        notifier.scheduleDaily(loginId, "Hi from AiRise", "Open the app and keep the streak going!", hour, minute)
    }

    fun scheduleDailyChallenge(hour: Int = 18, minute: Int = 0) {
        notifier.cancel(challengeId)
        notifier.scheduleDaily(challengeId, "Challenge time", "Do today’s challenge and climb the leaderboard!", hour, minute)
    }

    fun cancelLogin()     = notifier.cancel(loginId)
    fun cancelChallenge() = notifier.cancel(challengeId)
}


