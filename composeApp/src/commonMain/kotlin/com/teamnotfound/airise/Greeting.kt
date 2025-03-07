package com.teamnotfound.airise

import kotlin.random.Random

class Greeting {
    private val platform = getPlatform()

    /*
    fun greet(): String {
        val firstWord = if (Random.nextBoolean()) "Hi!" else "Hello!"
        return "Hello, ${platform.name}!"
    }
     */

    // Now returns a list
    fun greet(): List<String> = buildList {
        add(if (Random.nextBoolean()) "Hi!" else "Hello!")
        add("Guess what this is! > ${platform.name.reversed()}!")
    }

}