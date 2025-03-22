package com.teamnotfound.airise.health

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
expect fun rememberHealthPermissionState(): State<Boolean>