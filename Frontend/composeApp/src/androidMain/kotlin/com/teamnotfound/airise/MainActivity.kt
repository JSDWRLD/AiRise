package com.teamnotfound.airise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.teamnotfound.airise.network.AppContainer
import com.teamnotfound.airise.network.UserClient
import com.teamnotfound.airise.network.createHttpClient
import io.ktor.client.engine.okhttp.OkHttp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                container = AppContainer(
                    userClient = remember {
                        UserClient(createHttpClient(OkHttp.create()))
                    }
                )
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        container = AppContainer(
            userClient = remember {
                UserClient(createHttpClient(io.ktor.client.engine.okhttp.OkHttp.create()))
            }
        )
    )

}