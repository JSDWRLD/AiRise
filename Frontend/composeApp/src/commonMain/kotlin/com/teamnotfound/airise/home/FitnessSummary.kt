package com.teamnotfound.airise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.LocalFireDepartment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.Transparent
import com.teamnotfound.airise.util.White

//displays stats based on user time selection and includes dropdown
@Composable
fun FitnessSummarySection(
    selectedTimeframe: String,
    formattedDate: String,
    healthData: HealthData,
    onTimeFrameSelected: (String) -> Unit,
    onRefreshHealth: () -> Unit,
    onWriteSample: () -> Unit,
    onHydrationUpdated: (Float) -> Unit
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Fitness Summary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )

            // dropdown menu for time
            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                    shape = RoundedCornerShape(180.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = selectedTimeframe, color = White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Outlined.ArrowDropDown,
                            contentDescription = "Select timeframe",
                            tint = White
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(DeepBlue)
                ) {
                    listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { timeframe ->
                        DropdownMenuItem(onClick = {
                            onTimeFrameSelected(timeframe)
                            expanded = false
                        }) {
                            Text(text = timeframe, color = White)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(0.dp))

        // displays date in format
        Text(
            text = formattedDate,
            fontSize = 14.sp,
            color = Silver
        )

        Spacer(modifier = Modifier.height(16.dp))

        // summary layout for information such as calories, steps, and heart rate
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
                hydration = healthData.hydration,
                onHydrationUpdated = onHydrationUpdated,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Write sample
        Button(
            onClick = onWriteSample,
            colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
            shape = RoundedCornerShape(180.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) { Text("Write sample", color = White, fontSize = 14.sp) }
    }
}

//displays the stat for the factors measured
@Composable
fun FitnessStatBox(label: String, value: String, unit: String, iconType: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Silver, RoundedCornerShape(16.dp))
            .background(Transparent)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        //title and icon on the saw row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label, //format title to each section
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                imageVector = iconType, //displays the icons for each section
                contentDescription = "$label Icon",
                tint = Orange,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        //format to display heart rate, calories, steps
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = White)

        Spacer(modifier = Modifier.height(4.dp))

        //format to display the unit
        Text(
            text = unit,
            fontSize = 12.sp,
            color = Silver
        )
    }
}

@Composable
fun HydrationBox(
    hydration: Float,
    onHydrationUpdated: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var manualInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val waterBottleSize = 16.9f // oz per water bottle
    val totalBottles = 8
    val maxHydration = 200f

    Column(
        modifier = modifier
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Silver, RoundedCornerShape(16.dp))
            .background(Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hydration",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Icon(
                imageVector = Icons.Outlined.WaterDrop,
                contentDescription = "Hydration Icon",
                tint = Color(0xFF4FC3F7)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Water bottle visualization with 8 chunks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Text(
                text = "Water Bottles: ${(hydration / waterBottleSize).toInt()}",
                fontSize = 12.sp,
                color = Silver,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until totalBottles) {
                    val bottleFillPercentage = ((hydration - (i * waterBottleSize)) / waterBottleSize).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepBlue.copy(alpha = 0.5f))
                    ) {
                        // Water fill for this bottle
                        if (bottleFillPercentage > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(bottleFillPercentage)
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
                            color = if (bottleFillPercentage > 0.5f) White else Silver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Text(
                text = "${hydration.toInt()} oz / ${maxHydration.toInt()} oz",
                fontSize = 10.sp,
                color = Silver,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Current hydration value
        Text(
            text = "${hydration.toInt()} oz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input water amount in oz
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Update Amount:",
                fontSize = 12.sp,
                color = Silver,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { newValue ->
                        if (newValue.matches("^\\d*\\.?\\d*\$".toRegex())) {
                            manualInput = newValue
                            inputError = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        cursorColor = Orange,
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = Silver,
                        focusedLabelColor = Orange,
                        unfocusedLabelColor = Silver
                    ),
                    singleLine = true,
                    placeholder = {
                        Text("oz", color = Silver)
                    },
                    isError = inputError != null
                )

                Button(
                    onClick = {
                        val newHydration = manualInput.toFloatOrNull()
                        when {
                            newHydration == null -> {
                                inputError = "Please enter a valid number"
                            }
                            !isHydrationInputValid(newHydration, maxHydration) -> {
                                inputError = "Enter 0-${maxHydration.toInt()} oz"
                            }
                            else -> {
                                onHydrationUpdated(newHydration)
                                manualInput = ""
                                inputError = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
                    enabled = manualInput.isNotBlank()
                ) {
                    Text("Update", fontSize = 12.sp, color = White)
                }
            }

            inputError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

private fun isHydrationInputValid(input: Float, maxHydration: Float): Boolean {
    return input in 0f..maxHydration
}