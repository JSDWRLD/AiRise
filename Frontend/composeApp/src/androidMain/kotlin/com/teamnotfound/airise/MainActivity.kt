package com.teamnotfound.airise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.teamnotfound.airise.AppContainer
import com.teamnotfound.airise.cache.UserCache
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.network.createHttpClient
import io.ktor.client.engine.okhttp.OkHttp
import com.teamnotfound.airise.cache.UserCacheAndroid
import com.teamnotfound.airise.cache.FakeUserCache

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userClient = UserClient(createHttpClient(OkHttp.create()))
        val userCache = UserCacheAndroid(applicationContext)

        val container = AppContainer(
            userClient = userClient,
            userCache = userCache
        )

        setContent {
            App(container = container)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        container = AppContainer(
            userClient = remember {
                UserClient(createHttpClient(OkHttp.create()))
            },
            userCache = FakeUserCache()
        )
    )

}