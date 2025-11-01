package com.teamnotfound.airise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.teamnotfound.airise.AppContainer
import com.teamnotfound.airise.data.cache.UserCache
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.network.createHttpClient
import io.ktor.client.engine.okhttp.OkHttp
import com.teamnotfound.airise.data.auth.User
import com.teamnotfound.airise.room.DatabaseProvider
import com.teamnotfound.airise.cache.SummaryCacheAndroid
import com.teamnotfound.airise.cache.FakeSummaryCache
import io.ktor.client.HttpClient

//Just for testing purposes for now
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.khealth.KHealth
import com.teamnotfound.airise.notifications.LocalNotifierAndroid
import notifications.WorkoutReminderUseCase
import notifications.MealReminderUseCase
import notifications.NudgeReminderUseCase
import notifications.WaterReminderUseCase
import android.os.Build
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts

import com.teamnotfound.airise.data.auth.FirebaseTokenManager
import com.teamnotfound.airise.data.network.clients.DataClient
import dev.gitlive.firebase.auth.auth
import notifications.LocalNotifier

class MainActivity : ComponentActivity() {
    private val kHealth = KHealth(this)
    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun requestExactAlarmIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //This is REQUIRED for the library to work properly (on Android only)
        kHealth.initialise()

        val auth = dev.gitlive.firebase.Firebase.auth
        val tokenManager = FirebaseTokenManager(auth)

        val http = createHttpClient(
            OkHttp.create(),
            tokenManager
        )

        val userClient = UserClient(http)
        val dataClient = DataClient(http)

        val summaryCache = SummaryCacheAndroid(applicationContext)
        val notifier: LocalNotifier = LocalNotifierAndroid(this)
        val reminder = WorkoutReminderUseCase(notifier)
        val mealReminder = MealReminderUseCase(notifier)
        val waterReminder = WaterReminderUseCase(notifier)
        val nudgeReminder = NudgeReminderUseCase(notifier)

        val container = AppContainer(
            userClient = userClient,
            dataClient = dataClient,
            kHealth = kHealth,
            summaryCache = summaryCache,
            httpClient = http
        )
        //For debugging
//        lifecycleScope.launch(Dispatchers.IO) {
//            val dummyUser = User(
//                id = "dummy",
//                email = "dummy@example.com"
//            )
//            userCache.cacheUserData(dummyUser)
//        }

        setContent {
            App(container = container, reminder = reminder)
        }
        //For Android 13+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        requestExactAlarmIfNeeded()

        mealReminder.scheduleDailyMeals()
        waterReminder.scheduleEvery2h()
        nudgeReminder.scheduleDailyLogin()
        nudgeReminder.scheduleDailyChallenge()


    }
}

/*
@Preview
@Composable
fun AppAndroidPreview() {
    App(
        container = AppContainer(
            userClient = remember {
                UserClient(createHttpClient(OkHttp.create()))
            },
            //Just placeholders for now
            userCache = FakeUserCache(),
            summaryCache = FakeSummaryCache()
        )
    )

}
*/