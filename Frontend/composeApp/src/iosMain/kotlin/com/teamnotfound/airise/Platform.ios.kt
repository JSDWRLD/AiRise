package com.teamnotfound.airise

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = "Im iOS"
}

actual fun getPlatform(): Platform = IOSPlatform()