package com.teamnotfound.airise.customize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onCancel: () -> Unit = {}
) {
    val ui = viewModel.uiState.collectAsState().value

    // not loaded yet, show a loader
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

    // editable state derived from VM seeds
    var selectedDays by remember(ui.initialDays) { mutableStateOf(ui.initialDays.toMutableSet()) }
    var selectedTimes by remember(ui.initialTimesCsv) {
        mutableStateOf(
            ui.initialTimesCsv.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toMutableSet()
        )
    }
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Customize Onboarding",
                color = White,
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // workout days
            CustomizationCard(
                title = "Workout Days",
                saving = ui.isSaving,
                onSave = {
                    viewModel.save(
                        OnboardingDataUpdate(
                            workoutDays = selectedDays.toList()
                        )
                    )
                }
            ) {
                DayOfWeekChecklist(
                    selected = selectedDays,
                    onToggle = { day ->
                        selectedDays = selectedDays.toMutableSet().apply {
                            if (!add(day)) remove(day)
                        }
                    }
                )
            }

            // workout times
            CustomizationCard(
                title = "Workout Times",
                saving = ui.isSaving,
                onSave = {
                    viewModel.save(
                        OnboardingDataUpdate(
                            workoutTime = selectedTimes.joinToString(", ")
                        )
                    )
                }
            ) {
                TimesChecklist(
                    selected = selectedTimes,
                    onToggle = { pref ->
                        selectedTimes = selectedTimes.toMutableSet().apply {
                            if (!add(pref)) remove(pref)
                        }
                    }
                )
            }

            // equipment
            CustomizationCard(
                title = "Equipment",
                saving = ui.isSaving,
                onSave = {
                    viewModel.save(
                        OnboardingDataUpdate(
                            workoutEquipment = selectedEquipmentKey
                        )
                    )
                }
            ) {
                EquipmentRadioGroup(
                    selectedKey = selectedEquipmentKey,
                    onSelect = { selectedEquipmentKey = it }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CustomizationCard(
    title: String,
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
            .wrapContentHeight()
            .padding(bottom = 4.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = Orange,
                fontSize = 18.sp
            )

            content()

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        color = Orange,
                        strokeWidth = 2.dp
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DeepBlue,
                        contentColor = White,
                        disabledBackgroundColor = DeepBlue.copy(alpha = 0.5f),
                        disabledContentColor = White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text("Save", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekChecklist(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val ordered = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ordered.forEach { day ->
            OptionRow(
                checked = day in selected,
                label = day,
                onClick = { onToggle(day) }
            )
        }
    }
}

@Composable
private fun TimesChecklist(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val options = listOf("Morning", "Afternoon", "Evening")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { opt ->
            OptionRow(
                checked = opt in selected,
                label = opt,
                onClick = { onToggle(opt) }
            )
        }
    }
}

@Composable
private fun EquipmentRadioGroup(
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("Bodyweight", "Home", "Gym")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { label ->
            val key = EQUIPMENT_LABEL_TO_KEY[label] ?: label.lowercase()
            RadioOptionRow(
                selected = (key == selectedKey),
                label = label,
                onClick = { onSelect(key) }
            )
        }
    }
}

@Composable
private fun OptionRow(
    checked: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, DeepBlue), RoundedCornerShape(10.dp))
            .background(BgBlack, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = Orange,
                uncheckedColor = White
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, color = White, fontSize = 15.sp)
    }
}

@Composable
private fun RadioOptionRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, DeepBlue), RoundedCornerShape(10.dp))
            .background(BgBlack, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Orange,
                unselectedColor = White
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, color = White, fontSize = 15.sp)
    }
}

private val EQUIPMENT_LABEL_TO_KEY = mapOf(
    "Bodyweight" to "bodyweight",
    "Home"       to "home",
    "Gym"        to "gym"
)

data class OnboardingDataUpdate(
    val workoutDays: List<String>? = null,
    val workoutTime: String? = null,
    val workoutEquipment: String? = null
)
