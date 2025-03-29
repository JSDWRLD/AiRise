package com.teamnotfound.airise

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.ComposeUIViewController
import com.teamnotfound.airise.AppContainer
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.network.createHttpClient
import io.ktor.client.engine.darwin.Darwin
import com.teamnotfound.airise.cache.FakeUserCache
import com.teamnotfound.airise.cache.FakeSummaryCache

fun MainViewController() = ComposeUIViewController {
    val platformConfig = defaultPlatformConfiguration()

    CompositionLocalProvider(
        LocalDensity provides platformConfig.density
    ) {
            App(
                container = AppContainer(
                    userClient = remember {
                        UserClient(createHttpClient(Darwin.create()))
                    },
                    userCache = FakeUserCache(),
                    summaryCache = FakeSummaryCache()

                )
            )
    }
}