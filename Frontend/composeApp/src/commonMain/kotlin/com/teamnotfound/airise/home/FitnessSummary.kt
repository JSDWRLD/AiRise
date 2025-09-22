package com.teamnotfound.airise.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
    onWriteSample: () -> Unit
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
            HeartRateBox("Heart", healthData.avgHeartRate.toString(), "bpm", Modifier.weight(1.2f))
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

//displays the heart rate with depiction
@Composable
fun HeartRateBox(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(255.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Silver, RoundedCornerShape(16.dp))
            .background(Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {

        //title and the heart icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Heart Rate Icon",
                tint = Orange
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        //draws the squiggly line graph
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
        ) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.7f)//starting position

                //draws line using quadratic
                quadraticTo(size.width * 0.12f, 0f, size.width * 0.25f, size.height * 0.7f)
                quadraticTo(size.width * 0.37f, size.height, size.width * 0.5f, size.height * 0.6f)
                quadraticTo(size.width * 0.62f, 0f, size.width * 0.75f, size.height * 0.7f)
                quadraticTo(size.width * 0.87f, size.height, size.width, size.height * 0.6f)

                // end area to create the fade effect
                lineTo(size.width, size.height * 1.5f)
                lineTo(0f, size.height * 1.5f)
                close()
            }

            // area with transparency
            drawPath(
                path = path,
                color = Orange.copy(alpha = 0.3f),
                style = Fill
            )

            //main line
            drawPath(
                path = path,
                color = Orange,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        //displays heart rate
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.align(Alignment.Start)
        )

        //displays unit bpm
        Text(
            text = unit,
            fontSize = 12.sp,
            color = Silver,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}