package com.teamnotfound.airise.platform

import androidx.compose.runtime.Composable

@Composable
expect fun ImagePlatformPicker(): (suspend () -> ByteArray?)