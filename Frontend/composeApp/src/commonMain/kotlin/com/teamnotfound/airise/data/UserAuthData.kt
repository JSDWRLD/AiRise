package com.teamnotfound.airise.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable

@Serializable
class UserAuthData {
    // ADD JWT TOKEN LATER HERE
    var email: MutableState<String> = mutableStateOf("")
    var username: MutableState<String> = mutableStateOf("")
    var password: MutableState<String> = mutableStateOf("")
}
