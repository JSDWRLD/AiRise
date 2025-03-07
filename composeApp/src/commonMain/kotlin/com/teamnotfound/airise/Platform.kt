package com.teamnotfound.airise

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform