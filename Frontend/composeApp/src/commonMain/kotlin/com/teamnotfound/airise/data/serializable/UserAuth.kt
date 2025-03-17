package com.teamnotfound.airise.data.serializable

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable

@Serializable
class UserAuth {
    var email: MutableState<String> = mutableStateOf("")
    var username: MutableState<String> = mutableStateOf("")
    var password: MutableState<String> = mutableStateOf("")
}
