package com.teamnotfound.airise.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.filled.Bed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khealth.KHealth
import kotlinx.coroutines.launch
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.NeonGreen
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.White
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.home.FitnessStatBox

@Composable
fun HealthDashboardScreen(
    kHealth: KHealth,
    onBackClick: () -> Unit
) {
    val provider = remember { HealthDataProvider(kHealth) }
    val viewModel = remember { HealthDashboardViewModel(provider) }

    val healthData by viewModel.healthData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Dashboard", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = White)
                    }
                },
                backgroundColor = DeepBlue,
                elevation = 8.dp
            )
        },
        backgroundColor = BgBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Orange)
            } else if (error != null) {
                Text("Error: $error", color = Orange)
            } else if (healthData != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FitnessStatBox("Calories", healthData!!.caloriesBurned.toString(), "Kcal", Icons.Outlined.LocalFireDepartment)
                        FitnessStatBox("Steps", healthData!!.steps.toString(), "Steps", Icons.AutoMirrored.Outlined.DirectionsRun)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FitnessStatBox("Hydration", healthData!!.hydration.toString(), "fl oz", Icons.Outlined.WaterDrop)
                        FitnessStatBox("Sleep", healthData!!.sleep.toString(), "hrs", Icons.Filled.Bed)
                    }
                }
            } else {
                Text("No data available", color = Silver)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.requestAndLoadData() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Cyan)
                ) {
                    Text("Refresh", color = White)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Re-open permissions flow and reload if granted
                            val granted = provider.requestPermissions()
                            if (granted) {
                                viewModel.requestAndLoadData()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue)
                ) {
                    Text("Permissions", color = White)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.writeHealthData()
                            if (!success) println("Failed to write sample data.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = NeonGreen)
                ) {
                    Text("Write Sample", color = BgBlack)
                }
            }
        }
    }
}
