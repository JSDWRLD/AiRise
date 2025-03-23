package com.teamnotfound.airise

import com.teamnotfound.airise.login.LoginScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.home.HomeScreen
import com.teamnotfound.airise.login.LoginViewModel
import com.teamnotfound.airise.onboarding.signup.PrivacyPolicyScreen
import com.teamnotfound.airise.login.RecoverAccountScreen
import com.teamnotfound.airise.login.RecoverySentScreen
import com.teamnotfound.airise.navigationBar.NavBar
import com.teamnotfound.airise.onboarding.signup.SignUpScreen
import com.teamnotfound.airise.onboarding.signup.SignUpViewModel
import com.teamnotfound.airise.onboarding.WelcomeScreen
import com.teamnotfound.airise.onboarding.onboardingQuestions.OnboardingScreen
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth


// This is basically your main function.
@Composable
fun App(container: AppContainer) {
    val navController = rememberNavController()
    val authService = AuthService(
            auth = Firebase.auth,
    userClient = container.userClient
    )

    val appViewModel: AppViewModel = viewModel { AppViewModel(authService) }
    val isUserLoggedIn by appViewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        authService.signOut()
        if (isUserLoggedIn) {
            navController.navigate(AppScreen.HOMESCREEN.name) { popUpTo(0) }
        } else {
            navController.navigate(AppScreen.WELCOME.name) { popUpTo(0) }
        }
    }


    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            NavHost(
                navController = navController,
                startDestination = AppScreen.WELCOME.name
            ) {
                //Welcome Screen
                composable(route = AppScreen.WELCOME.name){
                    WelcomeScreen(
                        onStartClick = {navController.navigate(AppScreen.SIGNUP.name)},
                        onAlreadyHaveAnAccountClick = {navController.navigate(AppScreen.LOGIN.name)}
                    )
                }

                //login screen
                composable(route = AppScreen.LOGIN.name) {
                    val loginViewModel = viewModel { LoginViewModel(authService) }
                    LoginScreen(
                        viewModel = loginViewModel,
                        onPrivacyPolicyClick = { navController.navigate(AppScreen.PRIVACY_POLICY.name) },
                        onForgotPasswordClick = { navController.navigate(AppScreen.RECOVER_ACCOUNT.name) },
                        onSignUpClick = { navController.navigate(AppScreen.SIGNUP.name) },
                        onGoogleSignInClick = { /* google Sign-In */ },
                        onLoginSuccess = { email ->
                            navController.navigate("${AppScreen.HOMESCREEN.name}/$email")
                        }
                    )
                }

                // sign up screens
                composable(route = AppScreen.SIGNUP.name) {
                    val signUpViewModel = viewModel { SignUpViewModel(authService) }
                    SignUpScreen(
                        viewModel = signUpViewModel,
                        onLoginClick = { navController.popBackStack() },
                        onForgotPasswordClick = { navController.navigate(AppScreen.RECOVER_ACCOUNT.name) },
                        onGoogleSignUpClick = { /* Google Sign-Up */ },
                        onBackClick = { navController.popBackStack() },
                        onSignUpSuccess = { navController.navigate(AppScreen.ONBOARD.name) }
                    )
                }

                // recover account screen
                composable(route = AppScreen.RECOVER_ACCOUNT.name) {
                    RecoverAccountScreen(
                        onSendEmailClick = { navController.navigate(AppScreen.RECOVERY_SENT.name) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // recovery email sent screen
                composable(route = AppScreen.RECOVERY_SENT.name) {
                    RecoverySentScreen(
                        onBackToLoginClick = {
                            navController.popBackStack(
                                AppScreen.LOGIN.name,
                                inclusive = false
                            )
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                // Privacy Policy Screens
                composable(route = AppScreen.PRIVACY_POLICY.name) {
                    PrivacyPolicyScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Onboarding Screen
                composable(route = AppScreen.ONBOARD.name) {
                    OnboardingScreen()
                }

                // Home Screen
                composable(
                    route = "${AppScreen.WELCOME.name}/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email")
                    HomeScreen(
                        email.toString(),
                        onContinue = {navController.navigate(AppScreen.NAVBAR.name)})
                }

                //Navigation Bar and overview screen
                composable(route = AppScreen.NAVBAR.name) {
                    val bottomNavController = rememberNavController()
                    NavBar(navController = bottomNavController)
                }
            }
        }
    }
}

enum class AppScreen {
    WELCOME,
    LOGIN,
    SIGNUP,
    PRIVACY_POLICY,
    RECOVER_ACCOUNT,
    RECOVERY_SENT,
    ONBOARD,
    HOMESCREEN,
    NAVBAR
}
