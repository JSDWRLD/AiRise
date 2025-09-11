package com.teamnotfound.airise.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun ImagePlatformPicker(): (suspend () -> ByteArray?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var resumeWithBytes by remember { mutableStateOf<((ByteArray?) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        scope.launch {
            val bytes = uri?.let {
                context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
            }
            resumeWithBytes?.invoke(bytes)
            resumeWithBytes = null
        }
    }

    return remember {
        suspend {
            suspendCancellableCoroutine { cont ->
                resumeWithBytes = { bytes -> if (cont.isActive) cont.resume(bytes) }
                launcher.launch("image/*")
            }
        }
    }
}
