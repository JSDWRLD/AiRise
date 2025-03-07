package com.teamnotfound.airise

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Im Android"
}

actual fun getPlatform(): Platform = AndroidPlatform()