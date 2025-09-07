package com.teamnotfound.airise

import com.teamnotfound.airise.auth.login.LoginScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamnotfound.airise.communityNavBar.UserProfile
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.home.HomeScreen
import com.teamnotfound.airise.home.AiChat
import com.teamnotfound.airise.auth.login.LoginViewModel
import com.teamnotfound.airise.auth.signup.PrivacyPolicyScreen
import com.teamnotfound.airise.auth.recovery.RecoverAccountScreen
import com.teamnotfound.airise.auth.recovery.RecoverySentScreen
import com.teamnotfound.airise.navigationBar.NavBar
import com.teamnotfound.airise.auth.signup.SignUpScreen
import com.teamnotfound.airise.auth.signup.SignUpViewModel
import com.teamnotfound.airise.auth.WelcomeScreen
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.OnboardingScreen
import com.teamnotfound.airise.auth.recovery.RecoveryViewModel
import com.teamnotfound.airise.auth.email.EmailVerificationScreen
import com.teamnotfound.airise.auth.email.EmailVerificationViewModel
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.home.HomeViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import com.teamnotfound.airise.health.HealthDashboardScreen
import com.teamnotfound.airise.home.accountSettings.AccountSettings
import com.teamnotfound.airise.home.accountSettings.AccountSettingsViewModel
import com.teamnotfound.airise.auth.onboarding.OnboardingViewModel
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.OnboardingScreen
import com.teamnotfound.airise.challenges.ChallengeDetailsScreen
import com.teamnotfound.airise.friends.FriendsListScreen
import com.teamnotfound.airise.friends.FriendsListViewModel
import com.teamnotfound.airise.friends.ExFriendRepository
import com.teamnotfound.airise.challenges.ChallengesScreen
import com.teamnotfound.airise.challenges.ChallengesViewModel
import com.teamnotfound.airise.challenges.ExChallengesViewModel
import com.teamnotfound.airise.challenges.ChallengeEditorScreen




@Composable
fun App(container: AppContainer) {
    val navController = rememberNavController()
    val auth = Firebase.auth
    val authService = AuthService(
        auth = auth,
        userClient = container.userClient
    )

    val appViewModel: AppViewModel = viewModel { AppViewModel(authService) }
    val isUserLoggedIn by appViewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navController.navigate(AppScreen.HOMESCREEN.name) { popUpTo(0) }
        } else {
            navController.navigate(AppScreen.WELCOME.name) { popUpTo(0) }
        }
    }
    val userRepository = UserRepository(
        auth = auth,
        container.userClient,
        container.userCache
    )

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
                    val loginViewModel = viewModel { LoginViewModel(authService, container.userCache) }
                    val loginUiState by loginViewModel.uiState.collectAsState()

                    // Navigate to verification screen when flagged
                    LaunchedEffect(loginUiState.isEmailNotVerified) {
                        if (loginUiState.isEmailNotVerified) {
                            navController.navigate(AppScreen.EMAIL_VERIFICATION.name)

                        }
                    }

                    LoginScreen(
                        viewModel = loginViewModel,
                        onPrivacyPolicyClick = { navController.navigate(AppScreen.PRIVACY_POLICY.name) },
                        onForgotPasswordClick = { navController.navigate(AppScreen.RECOVER_ACCOUNT.name) },
                        onSignUpClick = { navController.navigate(AppScreen.SIGNUP.name) },
                        onLoginSuccess = { email ->
                            navController.navigate(AppScreen.HOMESCREEN.name)
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // sign up screen
                composable(route = AppScreen.SIGNUP.name) {
                    val signUpViewModel = viewModel { SignUpViewModel(authService, container.userCache) }
                    SignUpScreen(
                        viewModel = signUpViewModel,
                        onLoginClick = { navController.popBackStack() },
                        onForgotPasswordClick = { navController.navigate(AppScreen.RECOVER_ACCOUNT.name) },
                        onBackClick = { navController.popBackStack() },
                        onSignUpSuccessWithUser = {
                            if(authService.isUsingProvider){
                                navController.navigate(AppScreen.ONBOARD.name)
                            }else {
                                navController.navigate(AppScreen.EMAIL_VERIFICATION.name)
                            }
                        }
                    )
                }

                // recover account screen
                composable(route = AppScreen.RECOVER_ACCOUNT.name) {
                    val recoveryViewModel = viewModel { RecoveryViewModel(authService) }
                    RecoverAccountScreen(
                        viewModel = recoveryViewModel,
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
                    val onboardingViewModel = viewModel {
                        OnboardingViewModel(authService, container.summaryCache, container.userClient)
                    }
                    OnboardingScreen(viewModel = onboardingViewModel, appNavController = navController)
                }

                // Home Screen
                composable(
                    route = AppScreen.HOMESCREEN.name,
                ) {
                    HomeScreen(
                        viewModel = HomeViewModel(userRepository, container.userClient),
                        navController = navController
                    )
                }

                // Friends (Activity Feed)
                //need to update with actual data for activity feeds
                composable(route = AppScreen.FRIENDS.name) {
                    val vm = viewModel { FriendsListViewModel(ExFriendRepository()) }
                    FriendsListScreen(viewModel = vm, navController = navController)
                }

                // challenges list
                composable(route = AppScreen.CHALLENGES.name) {
                    //use same viewmodel across all challenges screens
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppScreen.CHALLENGES.name)
                    }
                    val vm: ExChallengesViewModel = viewModel(parentEntry)

                    //onaddclick goes to creating a challenge
                    //oneditclick goes to details of the challenge clicked on and can edit from there
                    ChallengesScreen(
                        viewModel = vm,
                        navController,
                        onAddClick = { navController.navigate(AppScreen.CHALLENGE_NEW.name) },
                        onEditClick = { navController.navigate(AppScreen.CHALLENGE_DETAILS.name) }
                    )
                }

                // create challenge
                composable(route = AppScreen.CHALLENGE_NEW.name) {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppScreen.CHALLENGES.name)
                    }
                    val vm: ExChallengesViewModel = viewModel(parentEntry)

                    //used for creating new challenges
                    ChallengeEditorScreen(
                        navController = navController,
                        viewModel = vm,
                        onBackClick = { navController.popBackStack() }//returns to the list
                    )
                }

                // edit challenge
                composable(route = AppScreen.CHALLENGE_EDIT.name) {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppScreen.CHALLENGES.name)
                    }
                    val vm: ExChallengesViewModel = viewModel(parentEntry)

                    //used for updating an known challenge
                    ChallengeEditorScreen(
                        navController = navController,
                        viewModel = vm,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // challenge details
                composable(route = AppScreen.CHALLENGE_DETAILS.name) {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppScreen.CHALLENGES.name)
                    }
                    val vm: ExChallengesViewModel = viewModel(parentEntry)

                    //read challenges details
                    ChallengeDetailsScreen(
                        viewModel = vm,
                        onBackClick = {
                            navController.popBackStack(AppScreen.CHALLENGES.name, inclusive = false)
                        }
                    )
                }

                //Navigation Bar and overview screen
                composable(route = AppScreen.NAVBAR.name) {
                    val bottomNavController = rememberNavController()
                    NavBar(navController = bottomNavController, appNavController = navController)
                }

                // Health Dashboard
                composable(route = AppScreen.HEALTH_DASHBOARD.name) {
                    HealthDashboardScreen(
                        kHealth = container.kHealth,
                        onBackClick = { navController.popBackStack() }
                    )
                }


                // Account Settings Screen
                composable(route = AppScreen.ACCOUNT_SETTINGS.name) {
                    val accountSettingViewModel = viewModel { AccountSettingsViewModel(authService,container.userClient) }
                    // TODO: Fill with actual user data
                    AccountSettings(navController = navController, accountSettingViewModel, kHealth = container.kHealth)

                }

                // Ai Chat Screen
                composable(route = AppScreen.AI_CHAT.name) {
                    AiChat(navController = navController)
                }

                // Email verification
                composable(route = AppScreen.EMAIL_VERIFICATION.name) {
                    val emailVerificationViewModel = viewModel { EmailVerificationViewModel() }

                    EmailVerificationScreen(
                        viewModel = emailVerificationViewModel,
                        onVerified = {
                            // Decide dynamically where to go
                            val user = Firebase.auth.currentUser
                            navController.navigate(AppScreen.ONBOARD.name) {
                                popUpTo(0)
                            }
                        },
                        onBackToLogin = {
                            navController.navigate(AppScreen.LOGIN.name) {
                                popUpTo(0)
                            }
                        }
                    )
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
    NAVBAR,
    HEALTH_DASHBOARD,
    ACCOUNT_SETTINGS,
    AI_CHAT,
    EMAIL_VERIFICATION,
    FRIENDS,
    CHALLENGES,
    CHALLENGE_NEW,
    CHALLENGE_EDIT,
    CHALLENGE_DETAILS
}
