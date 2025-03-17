package com.teamnotfound.airise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.teamnotfound.airise.network.UserClient
import com.teamnotfound.airise.network.createHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                client = remember {
                    UserClient(createHttpClient(io.ktor.client.engine.okhttp.OkHttp.create()))
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        client = remember {
            UserClient(createHttpClient(io.ktor.client.engine.okhttp.OkHttp.create()))
        }
    )
}