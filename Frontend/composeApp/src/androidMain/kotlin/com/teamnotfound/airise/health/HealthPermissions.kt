package com.teamnotfound.airise.health

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
actual fun rememberHealthPermissionState(): State<Boolean> {
    var permissionGranted by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requiredPermissions.add(Manifest.permission.BODY_SENSORS)
        }
        permissionLauncher.launch(requiredPermissions.toTypedArray())
    }

    return rememberUpdatedState(permissionGranted)
}