package com.teamnotfound.airise.health

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khealth.KHealth
import kotlinx.coroutines.launch

@Composable
fun HealthDashboardScreen(kHealth: KHealth) {
    val provider = remember { HealthDataProvider(kHealth) }
    val viewModel = remember { HealthDashboardViewModel(provider) }

    val healthData by viewModel.healthData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.requestAndLoadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Health Dashboard", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text("Error: $error", color = MaterialTheme.colors.error)
            healthData != null -> {
                Text("Active Calories Burned: ${healthData!!.activeCalories}")
                Text("Steps: ${healthData!!.steps}")
                Text("Heart Rate: ${healthData!!.heartRate} bpm")
            }
            else -> Text("No data available")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.requestAndLoadData() }) {
            Text("Refresh")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            coroutineScope.launch {
                val success = viewModel.writeHealthData()
                if (!success) {
                    println("Failed to write sample data.")
                }
            }
        }) {
            Text("Write Test Data")
        }
    }
}