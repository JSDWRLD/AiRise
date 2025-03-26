package com.teamnotfound.airise.home

sealed class HomeUiEvent {
//    data class GenerateGreeting(val greeting: String) : HomeUiEvent()
    data object GenerateOverview : HomeUiEvent()
    data class SelectedTimeFrameChanged(val selectedTimeFrame: String) : HomeUiEvent()
}