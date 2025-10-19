package com.teamnotfound.airise.health

import kotlinx.coroutines.flow.MutableSharedFlow

object HealthEvents {
    // Emitted when platform health data changes (e.g., after write sample)
    val updates = MutableSharedFlow<Unit>(replay = 0)
}
