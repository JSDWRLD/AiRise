package com.teamnotfound.airise

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import platform.UIKit.UIScreen

data class PlatformConfiguration(
    val density: Density,
    val layoutDirection: LayoutDirection
)

fun defaultPlatformConfiguration() = PlatformConfiguration(
    density = Density(
        density = UIScreen.mainScreen.scale.toFloat() * 0.90f,
        fontScale = 1f
    ),
    layoutDirection = LayoutDirection.Ltr // Force Left-to-Right layout
)
