package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import kotlin.test.*
import kotlinx.coroutines.test.*

class GeminiSessionManagerTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var api: FakeChatApi
    private var overviewCalls = 0
    private var fakeNow = 0L

    private fun makeManager(
        debounceMs: Long = 50,
        maxChars: Int = 100,
        overviewText: String = "Overview generated"
    ) = GeminiSessionManager(
        api = api,
        debounceMs = debounceMs,
        maxChars = maxChars,
        overviewFetcher = { _, _ ->
            overviewCalls++
            overviewText
        },
        nowMillis = { fakeNow }
    )

    @BeforeTest
    fun setup() {
        api = FakeChatApi()
        overviewCalls = 0
        fakeNow = 0L
    }

    @Test
    fun promptCache_hit_on_normalized_input() = runTest(dispatcher) {
        val mgr = makeManager()
        val r1 = mgr.sendPrompt("Hello", emptyList())
        val r2 = mgr.sendPrompt("  hello   ", emptyList()) // same after normalize
        assertEquals(r1, r2)
        assertEquals(1, api.chatCalls) // only one API call
    }

    @Test
    fun prompt_is_truncated_when_too_long() = runTest(dispatcher) {
        val mgr = makeManager(maxChars = 10)
        mgr.sendPrompt("x".repeat(50), emptyList())
        assertNotNull(api.lastPrompt)
        assertTrue(api.lastPrompt!!.length <= 11) // 10 + ellipsis
        assertTrue(api.lastPrompt!!.endsWith("…"))
    }

    @Test
    fun debounce_respects_min_interval() = runTest(dispatcher) {
        val mgr = makeManager(debounceMs = 50)

        // First call at t=0
        fakeNow = 0
        mgr.sendPrompt("ping", emptyList())

        // Second call at t=10 (inside debounce window)
        fakeNow = 10
        mgr.sendPrompt("pong", emptyList())

        // allow delayed tasks to finish
        advanceUntilIdle()

        // Different prompts: two API calls (debounce enforces delay, caching is by prompt)
        assertEquals(2, api.chatCalls)
    }

    @Test
    fun identical_prompts_within_window_use_cache() = runTest(dispatcher) {
        val mgr = makeManager(debounceMs = 50)

        fakeNow = 0
        mgr.sendPrompt("same", emptyList())
        fakeNow = 10
        // normalized same prompt, if cache hit
        mgr.sendPrompt("  SAME  ", emptyList())

        advanceUntilIdle()
        assertEquals(1, api.chatCalls)
    }

    @Test
    fun overview_cached_then_forced_refresh() = runTest(dispatcher) {
        val mgr = makeManager()
        val h = HealthData(caloriesTarget = 1800, caloriesBurned = 100)

        val first = mgr.getOverview(h)
        val second = mgr.getOverview(h)
        assertEquals(first, second)
        assertEquals(1, overviewCalls, "Second should be cache hit")

        val third = mgr.getOverview(h, forceRefresh = true)
        assertEquals("Overview generated", third)
        assertEquals(2, overviewCalls, "Force refresh should increment call count")
    }

    // Fake chat only API
    private class FakeChatApi : GeminiApi() {
        var chatCalls = 0
        var lastPrompt: String? = null

        override suspend fun chatReplyWithContext(
            userMsg: String,
            priorTurns: List<AiMessage>,
            workoutGoal: String?,
            dietaryGoal: String?,
            activityLevel: String?,
            fitnessLevel: String?,
            workoutLength: Int?,
            workoutRestrictions: String?,
            healthData: HealthData?,
            dailyProgressData: DailyProgressData?
        ): String {
            chatCalls++
            lastPrompt = userMsg
            return "reply: $userMsg"
        }
    }
}
