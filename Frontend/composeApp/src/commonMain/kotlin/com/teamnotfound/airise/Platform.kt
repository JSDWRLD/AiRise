package com.teamnotfound.airise

interface Platform {
    val name: String
}

// expect declares a function that can be used in common
// that is implemented different in ios and android.
expect fun getPlatform(): Platform