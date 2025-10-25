package com.teamnotfound.airise.health

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.khealth.KHealth
import kotlinx.coroutines.launch
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.NeonGreen
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.White

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
                elevation = 8.dp,
                modifier = Modifier.statusBarsPadding()
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

            // Compute whether the device is effectively empty / not synced.
            val allZero = healthData?.let {
                it.caloriesBurned == 0 && it.steps == 0 && it.hydration == 0.0 && it.sleep == 0.0
            } ?: true

            if (isLoading) {
                CircularProgressIndicator(color = Orange)
            } else if (error != null) {
                Text("Error: $error", color = Orange)
            } else {
                val statusText = if (allZero) "Device not available" else "Device synced"
                Text(
                    text = statusText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show the Permissions button only when the device appears not synced.
                if (allZero) {
                    Button(
                        onClick = { viewModel.requestAndLoadData() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Permissions", color = White)
                    }
                }

                // Show Sync and Write only when we have non-zero data (device synced).
                if (!allZero) {
                    Button(
                        onClick = { viewModel.requestAndLoadData() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Sync", color = White)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val success = viewModel.writeHealthData()
                                if (!success) {
                                    println("Failed to write sample data.")
                                } else {
                                    viewModel.requestAndLoadData()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = NeonGreen),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Write Sample", color = BgBlack)
                    }
                }
            }
        }
    }
}
