package com.teamnotfound.airise.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

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
        viewModel.requestAndLoadData()
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
                HealthMetricCard(label = "Active Calories", value = "${healthData!!.activeCalories}", unit = "kcal")
                Spacer(modifier = Modifier.height(12.dp))
                HealthMetricCard(label = "Steps", value = "${healthData!!.steps}", unit = "steps")
                Spacer(modifier = Modifier.height(12.dp))
                HealthMetricCard(label = "Heart Rate", value = "${healthData!!.heartRate}", unit = "bpm")
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

@Composable
fun HealthMetricCard(label: String, value: String, unit: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepBlue)
            .padding(16.dp)
    ) {
        Text(text = label, color = Silver, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value $unit",
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}