package com.teamnotfound.airise.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.navigationBar.BottomNavigationBar

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = Color(0xFF062022),
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp)
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
                    isLoading = uiState.value.isOverviewLoading
                )

                Spacer(modifier = Modifier.height(10.dp))

                DailyProgressSection(
                    dailyProgressData = uiState.value.dailyProgressData,
                    isLoading = uiState.value.isDailyProgressLoading
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