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
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.home.HomeScreen
import com.teamnotfound.airise.home.AiChat
import com.teamnotfound.airise.auth.login.LoginViewModel
import com.teamnotfound.airise.auth.general.PrivacyPolicyScreen
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
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.platform.ImagePlatformPicker
import com.teamnotfound.airise.community.friends.screens.FriendsListScreen
import com.teamnotfound.airise.community.friends.data.FriendsClient
import com.teamnotfound.airise.community.friends.repos.FriendsNetworkRepositoryImpl
import com.teamnotfound.airise.community.challenges.ChallengesScreen
import com.teamnotfound.airise.community.challenges.ChallengesViewModelImpl
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBarViewModel
import com.teamnotfound.airise.community.friends.models.FriendsViewModel
import com.teamnotfound.airise.community.leaderboard.LeaderboardScreen
import com.teamnotfound.airise.community.leaderboard.LeaderboardViewModel
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.workout.WorkoutScreen
import com.teamnotfound.airise.health.HealthDataProvider
import com.teamnotfound.airise.meal.FoodLogScreen
import com.teamnotfound.airise.meal.MealViewModel
import com.teamnotfound.airise.customize.CustomizingScreen
import androidx.compose.runtime.LaunchedEffect
import com.teamnotfound.airise.auth.admin.AdminVerifyViewModel
import com.teamnotfound.airise.auth.general.TermsOfUseScreen
import com.teamnotfound.airise.community.challenges.challengeEditor.ChallengeEditorViewModel
import com.teamnotfound.airise.customize.CustomizationViewModel

@Composable
fun App(container: AppContainer, reminder: notifications.WorkoutReminderUseCase) {
    val navController = rememberNavController()
    val auth = Firebase.auth
    val authService = AuthService(
        auth = auth,
        userClient = container.userClient
    )

    val appViewModel: AppViewModel = viewModel { AppViewModel(authService) }
    val isUserLoggedIn by appViewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        val route = navController.currentBackStackEntry?.destination?.route
        val user = Firebase.auth.currentUser

        // 1) Not logged in → Welcome (but don't disrupt auth screens)
        if (!isUserLoggedIn || user == null) {
            if (route !in listOf(
                    AppScreen.WELCOME.name,
                    AppScreen.LOGIN.name,
                    AppScreen.SIGNUP.name,
                    AppScreen.RECOVER_ACCOUNT.name,
                    AppScreen.RECOVERY_SENT.name
                )
            ) {
                navController.navigate(AppScreen.WELCOME.name) { popUpTo(0) }
            }
            return@LaunchedEffect
        }

        // 2) Email/password must verify before anything else
        if (!authService.isUsingProvider && !user.isEmailVerified) {
            if (route != AppScreen.EMAIL_VERIFICATION.name) {
                navController.navigate(AppScreen.EMAIL_VERIFICATION.name) { popUpTo(0) }
            }
            return@LaunchedEffect
        }

        // 3) Pull server profile to decide if onboarding is needed
        val needsOnboarding = runCatching {
            when (val res = container.userClient.getUserData(user)) {
                is com.teamnotfound.airise.data.network.Result.Success -> {
                    val u = res.data
                    // define your “minimum viable profile”
                    val noName = (u.fullName.isBlank() &&
                            (u.firstName.isBlank() || u.lastName.isBlank()))
                    val noWorkoutDays = u.workoutDays.isEmpty()
                    val noGoal = u.workoutGoal.isBlank()
                    val noFitness = u.fitnessLevel.isBlank()
                    noName || noWorkoutDays || noGoal || noFitness
                }
                // If we can't load user data (404/500/etc.), treat as not onboarded
                else -> true
            }
        }.getOrDefault(true)

        if (needsOnboarding) {
            if (route != AppScreen.ONBOARD.name) {
                navController.navigate(AppScreen.ONBOARD.name) { popUpTo(0) }
            }
            return@LaunchedEffect
        }

        // 4) Profile complete → Home
        if (route != AppScreen.HOMESCREEN.name) {
            navController.navigate(AppScreen.HOMESCREEN.name) { popUpTo(0) }
        }
    }


    val userRepository: IUserRepository = UserRepository(
        auth = auth,
        userClient = container.userClient,
    )

    val apiBase = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"
    val friendsRepository = remember {
        val friendsClient = FriendsClient(
            container.httpClient,
            apiBase
        )
        FriendsNetworkRepositoryImpl(friendsClient)
    }

    val sharedHomeVM: HomeViewModel = viewModel { HomeViewModel(
        UserRepository(auth = auth, container.userClient),
        container.userClient, HealthDataProvider(container.kHealth)
    )}

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
                        onTermsClick = { navController.navigate(AppScreen.TERMS.name) },
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
                    val signUpViewModel = viewModel { SignUpViewModel(authService) }
                    SignUpScreen(
                        viewModel = signUpViewModel,
                        onLoginClick = { navController.popBackStack() },
                        onPrivacyPolicyClick = { navController.navigate(AppScreen.PRIVACY_POLICY.name) },
                        onTermsClick = { navController.navigate(AppScreen.TERMS.name) },
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
                        viewModel = sharedHomeVM,
                        navController = navController
                    )
                }

                val communityNavBarViewModel = CommunityNavBarViewModel(
                    userRepository,
                    container.userClient,
                    container.dataClient
                )


                // Friends List
                composable(route = AppScreen.FRIENDS_LIST.name) {
                    val vm = viewModel { FriendsViewModel(
                        auth = authService,
                        friendRepo = friendsRepository,
                        userRepo = userRepository,
                    ) }
                    FriendsListScreen(viewModel = vm,
                        navController = navController,
                        communityNavBarViewModel = communityNavBarViewModel,
                    )
                }
                // challenges list
                composable(route = AppScreen.CHALLENGES.name) {
                    val vm: ChallengesViewModelImpl = viewModel {
                        ChallengesViewModelImpl(container.dataClient, container.userClient)
                    }

                    ChallengesScreen(
                        viewModel = vm,
                        adminVerifyViewModel = AdminVerifyViewModel(authService, userRepository),
                        editorViewModel = ChallengeEditorViewModel(container.dataClient),
                        navController = navController,
                        communityNavBarViewModel = communityNavBarViewModel
                    )
                }

                // Workout Screen
                composable(route = AppScreen.WORKOUT.name) {
                    WorkoutScreen(
                        userRepository = userRepository,
                        navController = navController,
                        reminder = reminder
                    )
                }

                // Meal/food log
                composable(route = AppScreen.MEAL.name) {
                    val vm = remember {
                        MealViewModel.network(
                            dataClient = container.dataClient,
                            userClient = container.userClient,
                            firebaseUser = Firebase.auth.currentUser
                                ?: error("Not logged in")
                        )
                    }
                    FoodLogScreen(appNavController = navController, vm = vm)
                }

                // Customize Screen
                composable(route = AppScreen.CUSTOMIZE.name) {
                    val customizeVM = viewModel {
                        CustomizationViewModel(
                            authService = authService,
                            summaryCache = container.summaryCache,
                            userClient = container.userClient
                        )
                    }

                    LaunchedEffect(Unit) {
                        customizeVM.loadFromServer()
                    }

                    CustomizingScreen(
                        appNavController = navController,
                        viewModel = customizeVM
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
                    //Reusing the data retrieved for HomeViewModel, to avoid too many API calls
                    val homeUi by sharedHomeVM.uiState.collectAsState()
                    val workoutGoal: String? = homeUi.userData?.workoutGoal?.takeIf { it.isNotBlank() }
                    val dietaryGoal: String? = homeUi.userData?.dietaryGoal?.takeIf { it.isNotBlank() }
                    val activityLevel: String? = homeUi.userData?.activityLevel?.takeIf { it.isNotBlank() }
                    val fitnessLevel: String? = homeUi.userData?.fitnessLevel?.takeIf { it.isNotBlank() }
                    val workoutLength: Int? = homeUi.userData?.workoutLength
                    val workoutRestrictions: String? = homeUi.userData?.workoutRestrictions?.takeIf { it.isNotBlank() }
                    val healthData: HealthData? = homeUi.healthData
                    val dailyProgressData: DailyProgressData? = homeUi.dailyProgressData

                    val pickImage = ImagePlatformPicker()

                    AiChat(
                        navController = navController,
                        workoutGoal = workoutGoal,
                        dietaryGoal= dietaryGoal,
                        activityLevel= activityLevel,
                        fitnessLevel= fitnessLevel,
                        workoutLength= workoutLength,
                        workoutRestrictions= workoutRestrictions,
                        healthData= healthData,
                        dailyProgressData= dailyProgressData,
                        onPickImageBytes = pickImage
                    )
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

                // Terms & Conditions Screen
                composable(route = AppScreen.TERMS.name) {
                    TermsOfUseScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Leaderboard Screen
                composable(route = AppScreen.LEADERBOARD.name) {
                    val leaderboardViewModel = viewModel { LeaderboardViewModel(container.dataClient) }
                    LeaderboardScreen(navController = navController, communityNavBarViewModel = communityNavBarViewModel, leaderboardViewModel)
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
    TERMS,
    RECOVER_ACCOUNT,
    RECOVERY_SENT,
    ONBOARD,
    HOMESCREEN,
    NAVBAR,
    HEALTH_DASHBOARD,
    ACCOUNT_SETTINGS,
    AI_CHAT,
    EMAIL_VERIFICATION,
    FRIENDS_LIST,
    CHALLENGES,
    LEADERBOARD,
    WORKOUT,
    MEAL,
    CUSTOMIZE
}
