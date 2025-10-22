package com.teamnotfound.airise.generativeAi

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import com.teamnotfound.airise.generativeAi.GeminiApi
import com.teamnotfound.airise.generativeAi.GeminiSessionManager
import com.teamnotfound.airise.generativeAi.AiMessage

import androidx.compose.runtime.Immutable

@Immutable
data class AiMessage(
    val aiModel:String,
    val message: String
)
