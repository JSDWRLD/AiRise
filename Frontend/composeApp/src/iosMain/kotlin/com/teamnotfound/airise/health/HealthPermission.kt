package com.teamnotfound.airise.health

import androidx.compose.runtime.*

@Composable
actual fun rememberHealthPermissionState(): State<Boolean> {
    return rememberUpdatedState(true)
}