package com.teamnotfound.airise.generativeAi

import androidx.compose.runtime.Immutable

@Immutable
data class AiMessage(
    val aiModel:String,
    val message: String
)
