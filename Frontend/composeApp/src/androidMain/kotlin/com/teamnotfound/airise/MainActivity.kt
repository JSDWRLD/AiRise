package com.teamnotfound.airise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.network.createHttpClient
import io.ktor.client.engine.okhttp.OkHttp
import com.khealth.KHealth

class MainActivity : ComponentActivity() {
    private val kHealth = KHealth(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //This is REQUIRED for the library to work properly (on Android only)
        kHealth.initialise()

        setContent {
            App(
                container = AppContainer(
                    userClient = remember {
                        UserClient(createHttpClient(OkHttp.create()))
                    },
                    kHealth = kHealth
                )
            )
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
            }
        )
    )

}
*/