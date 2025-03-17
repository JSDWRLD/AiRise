package com.teamnotfound.airise.onboardingQuestions

//Define the different onboarding screens
sealed class OnboardingScreens(val route: String){
    data object WorkoutGoal : OnboardingScreens("workoutGoal")
    data object FitnessLevel : OnboardingScreens("fitnessLevel")
    data object WorkoutDuration : OnboardingScreens("workoutDuration")
    data object EquipmentAccess : OnboardingScreens("equipmentAccess")
    data object WorkoutDays : OnboardingScreens("workoutDays")
    data object WorkoutTime : OnboardingScreens("workoutTime")
    data object DietaryGoal : OnboardingScreens("dietaryGoal")
    data object RestrictionInjuries : OnboardingScreens("restrictionInjury")
    data object ActivityLevel : OnboardingScreens("activityLevel")
    data object ThankYou : OnboardingScreens("thankYou")
}