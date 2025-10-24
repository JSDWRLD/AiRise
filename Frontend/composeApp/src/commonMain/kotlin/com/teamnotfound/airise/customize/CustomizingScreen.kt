package com.teamnotfound.airise.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.*

@Composable
fun CustomizingScreen(
    appNavController: NavHostController,
    viewModel: CustomizationViewModel,
    modifier: Modifier = Modifier,
) {
    val ui = viewModel.uiState.collectAsState().value

    if (!ui.isLoaded) {
        val bottomNavController = rememberNavController()
        Scaffold(
            backgroundColor = BgBlack,
            bottomBar = {
                BottomNavigationBar(
                    navController = bottomNavController,
                    appNavController = appNavController
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgBlack)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Orange)
            }
        }
        return
    }

    var selectedDays by remember(ui.initialDays) { mutableStateOf(ui.initialDays.toMutableSet()) }
    var selectedLength by remember(ui.initialLength) { mutableStateOf(ui.initialLength) }
    var selectedEquipmentKey by remember(ui.initialEquipmentKey) { mutableStateOf(ui.initialEquipmentKey) }

    val scroll = rememberScrollState()
    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                appNavController = appNavController
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(padding)
                .statusBarsPadding()
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                title = "Customize Your Plan",
                subtitle = "Adjust your training schedule and equipment preferences."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Workout Days
            CustomizationCard(
                title = "Training Days",
                caption = "Pick 3–6 days per week.",
                saving = ui.isSaving,
                onSave = {
                    if (selectedDays.size in 3..6) {
                        viewModel.save(OnboardingDataUpdate(workoutDays = selectedDays.toList()))
                    }
                }
            ) {
                DayOfWeekChips(
                    selected = selectedDays,
                    onToggle = { day ->
                        selectedDays = selectedDays.toMutableSet().apply {
                            if (day in this) {
                                if (size > 3) remove(day)   // prevent < 3
                            } else if (size < 6) {           // prevent > 6
                                add(day)
                            }
                        }
                    }
                )
            }

            // Workout Times
            CustomizationCard(
                title = "Workout Length",
                caption = "Choose your session duration.",
                saving = ui.isSaving,
                onSave = {
                    if (selectedLength in listOf(15, 30, 45, 60)) {
                        viewModel.save(OnboardingDataUpdate(workoutLength = selectedLength))
                    }
                }
            ) {
                LengthChips(
                    selectedMinutes = selectedLength,
                    onSelect = { selectedLength = it }
                )
            }


            // Equipment
            CustomizationCard(
                title = "Equipment",
                caption = "We'll tailor workouts to what you have.",
                saving = ui.isSaving,
                onSave = {
                    viewModel.save(
                        OnboardingDataUpdate(workoutEquipment = selectedEquipmentKey)
                    )
                }
            ) {
                EquipmentSegmented(
                    selectedKey = selectedEquipmentKey,
                    onSelect = { selectedEquipmentKey = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            // --- TDEE Calculator Placeholder Box ---

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, DeepBlue), RoundedCornerShape(16.dp))
                    .background(BgBlack)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                TDEEWidget()
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}


@Composable
private fun Header(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(DeepBlue.copy(alpha = 0.9f), Cyan.copy(alpha = 0.4f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 26.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Silver,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun CustomizationCard(
    title: String,
    caption: String?,
    saving: Boolean,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        backgroundColor = BgBlack,
        border = BorderStroke(1.dp, DeepBlue),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    AnimatedVisibility(visible = !caption.isNullOrBlank()) {
                        Text(
                            text = caption.orEmpty(),
                            color = Silver,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                SaveButton(
                    enabled = !saving,
                    saving = saving,
                    onClick = onSave
                )
            }

            content()
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    saving: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) DeepBlue else DeepBlue.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, DeepBlue),
        modifier = Modifier
            .height(36.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = saving, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp),
                    color = Orange,
                    strokeWidth = 2.dp
                )
            }
            Icon(
                imageVector = if (saving) Icons.Default.Timer else Icons.Default.Check,
                contentDescription = null,
                tint = White,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = if (saving) "Saving…" else "Save",
                color = White,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOfWeekChips(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val ordered = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ordered.forEach { day ->
            SelectableChip(
                text = day,
                selected = day in selected,
                onClick = { onToggle(day) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeChips(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val options = listOf("Morning", "Afternoon", "Evening")
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { opt ->
            SelectableChip(
                text = opt,
                selected = opt in selected,
                onClick = { onToggle(opt) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LengthChips(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(15, 30, 45, 60)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { min ->
            SelectableChip(
                text = "$min min",
                selected = (min == selectedMinutes),
                onClick = { onSelect(min) }
            )
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) DeepBlue.copy(alpha = 0.4f) else BgBlack
    val borderColor = if (selected) Cyan else DeepBlue
    val labelColor = if (selected) White else Silver

    Surface(
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .defaultMinSize(minHeight = 36.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            AnimatedVisibility(visible = selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
            }
            AnimatedVisibility(visible = selected) { Spacer(Modifier.width(6.dp)) }
            Text(text = text, color = labelColor, fontSize = 14.sp)
        }
    }
}

@Composable
private fun EquipmentSegmented(
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("Bodyweight", "Home", "Gym")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgBlack)
            .border(BorderStroke(1.dp, DeepBlue), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { label ->
            val key = EQUIPMENT_LABEL_TO_KEY[label] ?: label.lowercase()
            val selected = key == selectedKey
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) DeepBlue.copy(alpha = 0.5f) else Transparent)
                    .border(
                        BorderStroke(1.dp, if (selected) Cyan else DeepBlue),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(key) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) White else Silver,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private val EQUIPMENT_LABEL_TO_KEY = mapOf(
    "Bodyweight" to "bodyweight",
    "Home" to "home",
    "Gym" to "gym"
)

data class OnboardingDataUpdate(
    val workoutDays: List<String>? = null,
    val workoutLength: Int? = null,
    val workoutEquipment: String? = null
)
