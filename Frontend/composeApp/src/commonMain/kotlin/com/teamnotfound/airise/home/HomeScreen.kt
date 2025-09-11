package com.teamnotfound.airise.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.White
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth


@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavHostController) {
    val uiState = viewModel.uiState.collectAsState()
    val bottomNavController = rememberNavController()

    val currentImageUrl = uiState.value.userProfilePicture

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                appNavController = navController,
                onCommunityClick = {
                    navController.navigate(AppScreen.CHALLENGES.name) {
                        launchSingleTop = true
                    }
                },
                onOverviewClick = {
                    navController.navigate(AppScreen.HOMESCREEN.name) {
                        launchSingleTop = true
                        navController.graph.startDestinationRoute?.let { startRoute ->
                            popUpTo(startRoute) { saveState = true }
                        }
                        restoreState = true
                    }
                }
            )
        },
        topBar = {
            TopNavBar(
                greeting = uiState.value.greeting,
                username = uiState.value.username,
                isLoaded = uiState.value.isUserDataLoaded,
                navController = navController,
                currentImageUrl = currentImageUrl
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(AppScreen.AI_CHAT.name)
                },
                backgroundColor = DeepBlue,
                contentColor = White,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Open Ai Chat"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TodaysOverview(
                    overview = uiState.value.overview,
                    isLoaded = uiState.value.isOverviewLoaded
                )

                Spacer(modifier = Modifier.height(10.dp))

                DailyProgressSection(
                    dailyProgressData = uiState.value.dailyProgressData,
                    isLoaded = uiState.value.isDailyProgressLoaded
                    )

                Spacer(modifier = Modifier.height(10.dp))

                FitnessSummarySection(
                    selectedTimeframe = uiState.value.selectedTimeFrame,
                    formattedDate = uiState.value.formattedDateRange,
                    healthData = uiState.value.healthData,
                    onTimeFrameSelected = { timeFrame ->
                        viewModel.onEvent(HomeUiEvent.SelectedTimeFrameChanged(timeFrame))
                    }
                )
            }
        }
    }
}