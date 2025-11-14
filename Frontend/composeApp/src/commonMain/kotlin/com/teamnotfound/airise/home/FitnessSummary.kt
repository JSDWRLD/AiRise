package com.teamnotfound.airise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.Transparent
import com.teamnotfound.airise.util.White

// Displays stats based on user time selection and includes dropdown
@Composable
fun FitnessSummarySection(
    formattedDate: String,
    healthData: HealthData,
    onHydrationUpdated: (Double) -> Unit,
    isHealthSyncAvailable: Boolean,
    onEnableHealthSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Transparent)
    ) {
        // title and time dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fitness Summary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )

            Text(
                text = formattedDate,
                fontSize = 14.sp,
                color = Silver
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary layout for information such as calories, steps, and hydration
        if (isHealthSyncAvailable) {
            // Show normal Steps and Calories cards when health sync is available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FitnessStatBox("Calories", healthData.caloriesBurned.toString(), "Kcal", Icons.Outlined.LocalFireDepartment)
                    Spacer(modifier = Modifier.height(16.dp))
                    FitnessStatBox("Steps", healthData.steps.toString(), "Steps", Icons.AutoMirrored.Outlined.DirectionsRun)
                }
                HydrationBox(
                    hydration = healthData.hydration ?: 0.0,
                    onHydrationUpdated = onHydrationUpdated,
                    modifier = Modifier.weight(1.2f)
                )
            }
        } else {
            // Show Enable Health Sync card when health data cannot be read
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnableHealthSyncCard(
                    onEnableHealthSync = onEnableHealthSync,
                    modifier = Modifier.weight(1f)
                )
                HydrationBox(
                    hydration = healthData.hydration ?: 0.0,
                    onHydrationUpdated = onHydrationUpdated,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

// Enable Health Sync CTA card - shown when health data read fails
@Composable
fun EnableHealthSyncCard(
    onEnableHealthSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(320.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Silver, RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepBlue.copy(alpha = 0.3f),
                        Orange.copy(alpha = 0.2f)
                    )
                )
            )
            .clickable { onEnableHealthSync() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Sync,
            contentDescription = "Enable Health Sync",
            tint = Orange,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enable Health Sync",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connect your health data to track steps, calories burned, and sleep automatically",
            fontSize = 12.sp,
            color = Silver,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEnableHealthSync,
            colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = "Connect Now",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Displays the stat for the factors measured
@Composable
fun FitnessStatBox(label: String, value: String, unit: String, iconType: ImageVector) {
    Column(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Silver, RoundedCornerShape(16.dp))
            .background(Transparent)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Icon(
            imageVector = iconType,
            contentDescription = label,
            tint = Orange,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = unit,
            fontSize = 14.sp,
            color = Silver
        )
    }
}

@Composable
fun HydrationBox(
    hydration: Double,
    onHydrationUpdated: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val maxHydration = 128.0
    val bottleCount = 8
    val ozPerBottle = maxHydration / bottleCount

    Column(
        modifier = modifier
            .height(320.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Silver, RoundedCornerShape(16.dp))
            .background(Transparent)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.WaterDrop,
                contentDescription = "Hydration",
                tint = Orange,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "${(hydration / maxHydration * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Silver
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Water bottles visualization
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(bottleCount) { i ->
                val bottleStartOz = i * ozPerBottle
                val bottleEndOz = (i + 1) * ozPerBottle

                val bottleFillPercentage = when {
                    hydration <= bottleStartOz -> 0.0
                    hydration >= bottleEndOz -> 1.0
                    else -> (hydration - bottleStartOz) / ozPerBottle
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Transparent)
                    ) {
                        // Filled portion
                        if (bottleFillPercentage > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(bottleFillPercentage.toFloat())
                                    .align(Alignment.BottomStart)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF4FC3F7),
                                                Color(0xFF29B6F6)
                                            )
                                        )
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Silver.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )

                        // Bottle number
                        Text(
                            text = "${i + 1}",
                            color = if (bottleFillPercentage > 0.5) White else Silver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Current hydration value
        Text(
            text = "${hydration.toInt()} oz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Add 8 oz button
            Button(
                onClick = {
                    val newHydration = (hydration + 8.0).coerceAtMost(maxHydration)
                    onHydrationUpdated(newHydration)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                enabled = hydration < maxHydration
            ) {
                Text("Add 8oz", fontSize = 12.sp, color = White)
            }

            // Add 16 oz button
            Button(
                onClick = {
                    val newHydration = (hydration + 16.0).coerceAtMost(maxHydration)
                    onHydrationUpdated(newHydration)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                enabled = hydration < maxHydration
            ) {
                Text("Add 16oz", fontSize = 12.sp, color = White)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Custom button
            Button(
                onClick = { showCustomInput = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                enabled = hydration < maxHydration
            ) {
                Text("Custom", fontSize = 12.sp, color = White)
            }

            // Reset button
            Button(
                onClick = { onHydrationUpdated(0.0)},
                colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                enabled = hydration > 0
            ) {
                Text("Reset", fontSize = 12.sp, color = White)
            }
        }
    }

    // Custom input dialog
    if (showCustomInput) {
        AlertDialog(
            onDismissRequest = {
                showCustomInput = false
                customInput = ""
                inputError = null
            },
            title = {
                Text("Add Custom Amount", color = White)
            },
            text = {
                Column {
                    Text(
                        "Enter amount to add (oz):",
                        color = Silver,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { newValue ->
                            if (newValue.matches("^[0-9]*\\.?[0-9]*$".toRegex())) {
                                customInput = newValue
                                inputError = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Enter ounces", color = Silver)
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = White,
                            cursorColor = Orange,
                            focusedBorderColor = Orange,
                            unfocusedBorderColor = DeepBlue,
                            focusedLabelColor = Orange,
                            unfocusedLabelColor = Silver
                        ),
                        singleLine = true,
                        isError = inputError != null
                    )

                    inputError?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountToAdd = customInput.toDoubleOrNull()
                        when {
                            amountToAdd == null -> {
                                inputError = "Please enter a valid number"
                            }
                            amountToAdd <= 0 -> {
                                inputError = "Please enter a positive amount"
                            }
                            amountToAdd > (maxHydration - hydration) -> {
                                inputError = "Amount exceeds daily recommended amount"
                            }
                            else -> {
                                val newHydration = hydration + amountToAdd
                                onHydrationUpdated(newHydration)
                                showCustomInput = false
                                customInput = ""
                                inputError = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Orange)
                ) {
                    Text("Add", color = White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCustomInput = false
                        customInput = ""
                        inputError = null
                    }
                ) {
                    Text("Cancel", color = Silver)
                }
            },
            backgroundColor = BgBlack
        )
    }
}