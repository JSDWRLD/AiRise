package com.teamnotfound.airise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.teamnotfound.airise.util.White
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Silver
import androidx.navigation.NavController
import androidx.compose.material.Scaffold
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.navigationBar.BottomNavigationBar

@Composable
fun HomeScreen(viewModel: HomeViewModel, email: String) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                TodaysOverview(
                    overview = uiState.value.overview,
                    isLoading = uiState.value.isOverviewLoading
                )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome $username!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You have successfully signed in.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(250.dp))

            FitnessSummarySection(
                selectedTimeframe = uiState.value.selectedTimeFrame,
                formattedDate = uiState.value.formattedDateRange,
                calories = uiState.value.calories,
                steps = uiState.value.steps,
                heartRate = uiState.value.heartRate,
                onTimeFrameSelected = { timeFrame ->
                    viewModel.onEvent(HomeUiEvent.SelectedTimeFrameChanged(timeFrame))
                }
            )
        }
    }
}

@Composable
fun TodaysOverview(overview: String, isLoading: Boolean) {
    Column {
        Text(
            text = "Today's Overview",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = White
        )
        if(isLoading){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = DeepBlue)
        }else{
            Text(
                text = overview,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Silver
            )
        }
    }
}



//displays stats based on user time selection and includes dropdown
@Composable
fun FitnessSummarySection(
    selectedTimeframe: String,
    formattedDate: String,
    calories: Int,
    steps: Int,
    heartRate: Int,
    onTimeFrameSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF062022))
            .padding(16.dp)
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
                color = Color.White
            )

            // dropdown menu for time
            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E3A3A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = selectedTimeframe, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Outlined.ArrowDropDown,
                            contentDescription = "Select timeframe",
                            tint = Color.White
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1B424B))
                ) {
                    listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { timeframe ->
                        DropdownMenuItem(onClick = {
                            onTimeFrameSelected(timeframe)
                            expanded = false
                        }) {
                            Text(text = timeframe, color = Color.White)
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
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // summary layout for information such as calories, steps, and heart rate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FitnessStatBox("Calories", calories.toString(), "Kcal", Icons.Outlined.LocalFireDepartment)
                Spacer(modifier = Modifier.height(12.dp))
                FitnessStatBox("Steps", steps.toString(), "Steps", Icons.AutoMirrored.Outlined.DirectionsRun)
            }
            HeartRateBox("Heart", heartRate.toString(), "bpm", Modifier.weight(1.2f))
        }
    }
}

//displays the stat for the factors measured
@Composable
fun FitnessStatBox(label: String, value: String, unit: String, iconType: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .background(Color(0xFF062022))
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
                tint = Color(0xFFFFA500),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        //format to display heart rate, calories, steps
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White)

        Spacer(modifier = Modifier.height(4.dp))

        //format to display the unit
        Text(
            text = unit,
            fontSize = 12.sp,
            color = Color.Gray)
    }
}

//displays the heart rate with depiction
@Composable
fun HeartRateBox(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .background(Color(0xFF062022))
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
                color = Color.White
            )
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Heart Rate Icon",
                tint = Color(0xFFFFA500)
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
                color = Color(0xFFFFA500).copy(alpha = 0.3f),
                style = Fill
            )

            //main line
            drawPath(
                path = path,
                color = Color(0xFFFFA500),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        //displays heart rate
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

        //displays unit bpm
        Text(
            text = unit,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}
