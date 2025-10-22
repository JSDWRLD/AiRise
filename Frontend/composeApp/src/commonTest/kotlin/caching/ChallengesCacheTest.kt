package com.teamnotfound.airise.community.challenges

import com.teamnotfound.airise.community.challenges.*
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

import com.teamnotfound.airise.data.network.Result as NetResult

import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class ChallengesCacheTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var scope: TestScope

    @BeforeTest
    fun setUp() {
        scope = TestScope(dispatcher)
    }

    @AfterTest
    fun tearDown() { }

    @Test
    fun resultSuccessWorks() = runTest(dispatcher) {
        val r: NetResult<Int, Nothing> = NetResult.Success(42)
        assertTrue(r is NetResult.Success)
        assertEquals(42, (r as NetResult.Success).data)
    }
}
