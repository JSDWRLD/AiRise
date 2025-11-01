package com.teamnotfound.airise

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.ComposeUIViewController
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.network.createHttpClient
import io.ktor.client.engine.darwin.Darwin
import com.khealth.KHealth
import com.teamnotfound.airise.cache.FakeUserCache
import com.teamnotfound.airise.cache.FakeSummaryCache
import com.teamnotfound.airise.data.auth.FirebaseTokenManager
import com.teamnotfound.airise.data.network.clients.DataClient
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import notifications.TempNotifier
import notifications.WorkoutReminderUseCase
import notifications.MealReminderUseCase
import notifications.WaterReminderUseCase
import notifications.NudgeReminderUseCase

fun MainViewController() = ComposeUIViewController {
    val platformConfig = defaultPlatformConfiguration()
    val kHealth = KHealth()

    val notifier = TempNotifier()
    val workoutReminder = WorkoutReminderUseCase(notifier)
    val mealReminder    = MealReminderUseCase(notifier)
    val waterReminder   = WaterReminderUseCase(notifier)
    val nudgeReminder   = NudgeReminderUseCase(notifier)

    val auth = Firebase.auth
    val tokenManager = remember { FirebaseTokenManager(auth) }
    val http = remember { createHttpClient(Darwin.create(), tokenManager) }

    CompositionLocalProvider(LocalDensity provides platformConfig.density) {
        App(
            container = AppContainer(
                httpClient = http,
                userClient = remember { UserClient(http) },
                dataClient = remember { DataClient(http) },
                kHealth = kHealth,
                userCache = FakeUserCache(),
                summaryCache = FakeSummaryCache()
            ),
            reminder = workoutReminder
        )
    }
}
