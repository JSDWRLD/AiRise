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
import com.teamnotfound.airise.cache.UserCacheAndroid
import com.teamnotfound.airise.room.UserEntity
import com.teamnotfound.airise.cache.FakeUserCache
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
import com.teamnotfound.airise.data.network.clients.DataClient

class MainActivity : ComponentActivity() {
    private val kHealth = KHealth(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //This is REQUIRED for the library to work properly (on Android only)
        kHealth.initialise()

        val userClient = UserClient(createHttpClient(OkHttp.create()))
        val dataClient = DataClient(createHttpClient(OkHttp.create()))
        val userCache = UserCacheAndroid(applicationContext)
        val summaryCache = SummaryCacheAndroid(applicationContext)
        val httpClient = createHttpClient(OkHttp.create())

        val container = AppContainer(
            userClient = userClient,
            dataClient = dataClient,
            kHealth = kHealth,
            userCache = userCache,
            summaryCache = summaryCache,
            httpClient = httpClient
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
            App(container = container)
        }
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