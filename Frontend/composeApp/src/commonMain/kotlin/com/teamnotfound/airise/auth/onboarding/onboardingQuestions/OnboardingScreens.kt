package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

//Define the different onboarding screens
sealed class OnboardingScreens(val route: String){
    data object NameInput : OnboardingScreens("nameInput")
    data object WorkoutGoal : OnboardingScreens("workoutGoal")
    data object FitnessLevel : OnboardingScreens("fitnessLevel")
    data object WorkoutLength : OnboardingScreens("workoutLength")
    data object EquipmentAccess : OnboardingScreens("equipmentAccess")
    data object WorkoutDays : OnboardingScreens("workoutDays")
    data object WorkoutTime : OnboardingScreens("workoutTime")
    data object DietaryGoal : OnboardingScreens("dietaryGoal")
    data object WorkoutRestrictions : OnboardingScreens("workoutRestrictions")
    data object HeightSelection : OnboardingScreens("heightSelection")
    data object WeightSelection : OnboardingScreens("weightSelection")
    data object AgeSelection : OnboardingScreens("ageSelection")
    data object ActivityLevel : OnboardingScreens("activityLevel")
    data object ThankYou : OnboardingScreens("thankYou")
}