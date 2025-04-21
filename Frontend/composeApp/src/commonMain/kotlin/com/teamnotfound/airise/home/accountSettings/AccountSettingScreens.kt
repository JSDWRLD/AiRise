package com.teamnotfound.airise.home.accountSettings

sealed class AccountSettingScreens(val route: String){
    data object AccountSettings : AccountSettingScreens("accountScreen")
    data object DOBSelect : AccountSettingScreens("dobSelect")
    data object HeightSelect : AccountSettingScreens("heightSelect")
    data object WeightSelect : AccountSettingScreens("weightSelect")
    data object AiPersonality : AccountSettingScreens("aiPersonality")
    data object Notifications : AccountSettingScreens("notifications")
    data object HealthDashboard : AccountSettingScreens("healthDashboard")
}