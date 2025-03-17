package com.teamnotfound.airise

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.ComposeUIViewController
import com.teamnotfound.airise.network.UserClient
import com.teamnotfound.airise.network.createHttpClient
import io.ktor.client.engine.darwin.Darwin

fun MainViewController() = ComposeUIViewController {
    val platformConfig = defaultPlatformConfiguration()

    CompositionLocalProvider(
        LocalDensity provides platformConfig.density
    ) {
            App(
                container = AppContainer(
                    userClient = remember {
                        UserClient(createHttpClient(Darwin.create()))
                    }
                )
            )
    }
}