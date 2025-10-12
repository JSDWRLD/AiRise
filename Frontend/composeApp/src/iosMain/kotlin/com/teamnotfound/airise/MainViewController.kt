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
import com.teamnotfound.airise.data.network.clients.DataClient

fun MainViewController() = ComposeUIViewController {
    val platformConfig = defaultPlatformConfiguration()
    val kHealth = KHealth()
    CompositionLocalProvider(
        LocalDensity provides platformConfig.density
    ) {
        App(
            container = AppContainer(
                httpClient = createHttpClient(Darwin.create()),
                userClient = remember {
                    UserClient(createHttpClient(Darwin.create()))
                },
                dataClient = remember {
                    DataClient(createHttpClient(Darwin.create()))
                },
                kHealth = kHealth,
                userCache = FakeUserCache(),
                summaryCache = FakeSummaryCache(),
            ),
            reminder = TODO()
        )
    }
}