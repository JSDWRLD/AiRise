package com.teamnotfound.airise.meal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.data.serializable.FoodEntry
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.teamnotfound.airise.util.*
import kotlinx.datetime.*

//date selector can be edited by arrows/manually - within a year
@Composable
fun FoodLogScreen(
    appNavController: NavHostController,
    vm: MealViewModel = remember { MealViewModel.fake() }
) {
    val s = vm.uiState

    var showQuickAdd by remember { mutableStateOf(false) }
    var quickAddMeal by remember { mutableStateOf(MealType.Breakfast) }
    var showGoalEdit by remember { mutableStateOf(false) }
    var showMealPicker by remember { mutableStateOf(false) }
    var showDateInput by remember { mutableStateOf(false) }

    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                appNavController = appNavController
            )
        },
        topBar = {},
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Log") },
                icon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                onClick = { showMealPicker = true },
                backgroundColor = DeepBlue,
                contentColor = White,
                shape = RoundedCornerShape(28.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(padding)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            //shows actual date from offset
            DateSelectorDropdown(
                currentOffset = s.dayOffset,
                onPrev = vm::previousDay,
                onNext = vm::nextDay,
                onSelect = vm::setDayOffset,
                onOpenManual = { showDateInput = true }
            )

            Spacer(Modifier.height(8.dp))

            SummaryBanner(
                goal = s.goal,
                food = vm.totalFood,
                exercise = s.exercise,
                remaining = vm.remaining,
                onEditGoalClick = { showGoalEdit = true }
            )

            Spacer(Modifier.height(8.dp))

            val meals = s.day.meals
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MealSection("Breakfast", meals.breakfast) {
                        quickAddMeal = MealType.Breakfast; showQuickAdd = true
                    }
                }
                item {
                    MealSection("Lunch", meals.lunch) {
                        quickAddMeal = MealType.Lunch; showQuickAdd = true
                    }
                }
                item {
                    MealSection("Dinner", meals.dinner) {
                        quickAddMeal = MealType.Dinner; showQuickAdd = true
                    }
                }
            }
        }
    }

    //dialogs
    if (showMealPicker) {
        MealPickerDialog(
            onDismiss = { showMealPicker = false },
            onPick = { meal ->
                quickAddMeal = meal
                showMealPicker = false
                showQuickAdd = true
            }
        )
    }

    if (showQuickAdd) {
        QuickAddDialog(
            meal = quickAddMeal,
            onDismiss = { showQuickAdd = false },
            onAdd = { cal, name, serving, fats, carbs, proteins ->
                vm.addQuickFood(quickAddMeal, cal, name, serving, fats, carbs, proteins)
                showQuickAdd = false
            }
        )
    }

    if (showGoalEdit) {
        EditNumberDialog(
            title = "Set Daily Calorie Goal",
            initial = s.goal.toString(),
            onConfirm = { vm.setGoal(it); showGoalEdit = false },
            onDismiss = { showGoalEdit = false }
        )
    }


    if (showDateInput) {
        //converts manual date to vm offset and applies
        DateInputDialog(
            currentOffset = s.dayOffset,
            onDismiss = { showDateInput = false },
            onConfirmOffset = { newOffset ->
                vm.setDayOffset(newOffset)
                showDateInput = false
            }
        )
    }
}

//summary card
@Composable
private fun SummaryBanner(
    goal: Int,
    food: Int,
    exercise: Int,
    remaining: Int,
    onEditGoalClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp)) {
        //added glow for each card
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.radialGradient(colors = listOf(Cyan.copy(alpha = 0.18f), Transparent)))
                .blur(22.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        Card(
            backgroundColor = BgBlack,
            contentColor = White,
            elevation = 0.dp,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(Cyan.copy(alpha = 0.55f), Silver.copy(alpha = 0.25f)))
                    ),
                    shape
                )
                .clip(shape)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(White.copy(alpha = 0.05f), Transparent)))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Calories Remaining", color = White.copy(alpha = .9f), fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryCell(title = "Goal", value = goal.toString(), onValueClick = onEditGoalClick)
                        OperatorCell("–")
                        SummaryCell(title = "Food", value = food.toString())
                        OperatorCell("+")
                        SummaryCell(title = "Exercise", value = exercise.toString())
                        OperatorCell("=")
                        SummaryCell(title = "Remaining", value = remaining.toString(), highlight = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorCell(symbol: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(22.dp)
    ) {
        Text(symbol, color = Silver.copy(alpha = 0.8f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryCell(
    title: String,
    value: String,
    highlight: Boolean = false,
    onValueClick: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val valueText = @Composable {
            Text(
                value,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (onValueClick != null) {
            Box(Modifier.clickable { onValueClick() }) { valueText() }
        } else valueText()
        Spacer(Modifier.height(4.dp))
        Text(title, color = Silver, fontSize = 12.sp)
    }
}

//meals
@Composable
private fun MealSection(
    title: String,
    items: List<FoodEntry>,
    onAddFood: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        color = BgBlack,
        contentColor = White,
        elevation = 0.dp,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(listOf(Silver.copy(alpha = .35f), Cyan.copy(alpha = .25f)))
                ),
                shape
            )
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                val total = items.sumOf { it.calories }.toInt()
                if (total > 0) Text("$total", color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            items.forEach { item -> FoodRow(item) }

            OutlinedButton(
                onClick = onAddFood,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                border = BorderStroke(1.dp, Cyan.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("ADD FOOD", color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FoodRow(item: FoodEntry) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        color = DeepBlue.copy(alpha = 0.20f),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        elevation = 0.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.name, color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("1 item", color = Silver, fontSize = 12.sp)
            }
            Text(item.calories.toInt().toString(), color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

//dialogs
@Composable
private fun EditNumberDialog(
    title: String,
    initial: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val fieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = White,
        focusedBorderColor = Cyan,
        unfocusedBorderColor = Silver,
        cursorColor = White,
        focusedLabelColor = Silver,
        unfocusedLabelColor = Silver
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v.filter { it.isDigit() }.take(5) },
                label = { Text("Calories") },
                singleLine = true,
                colors = fieldColors
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text.toIntOrNull() ?: 0) }) { Text("Save", color = White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = White) } },
        backgroundColor = BgBlack,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun MealPickerDialog(
    onDismiss: () -> Unit,
    onPick: (MealType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log to", color = White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MealPickRow("Breakfast") { onPick(MealType.Breakfast) }
                MealPickRow("Lunch") { onPick(MealType.Lunch) }
                MealPickRow("Dinner") { onPick(MealType.Dinner) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = White) } },
        backgroundColor = BgBlack,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun MealPickRow(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        color = DeepBlue.copy(alpha = 0.25f),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() }
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(label, color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuickAddDialog(
    meal: MealType,
    onDismiss: () -> Unit,
    onAdd: (cal: Int, name: String, serving: String, fats: Double, carbs: Double, proteins: Double) -> Unit
) {
    var calories by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var serving by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }

    fun numericDecimal(s: String): String {
        val filtered = s.filter { it.isDigit() || it == '.' }
        val dots = filtered.count { it == '.' }
        return if (dots <= 1) filtered.take(7)
        else filtered.replaceFirst(".", "#").replace(".", "").replace("#", ".").take(7)
    }

    val fieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = White,
        focusedBorderColor = Cyan,
        unfocusedBorderColor = Silver,
        cursorColor = White,
        focusedLabelColor = Silver,
        unfocusedLabelColor = Silver
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add — ${meal.name}", color = White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter(Char::isDigit).take(5) },
                    label = { Text("Calories") },
                    singleLine = true,
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = serving,
                    onValueChange = { serving = it },
                    label = { Text("Serving") },
                    singleLine = true,
                    colors = fieldColors
                )
                Divider(color = DeepBlue.copy(alpha = 0.35f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = numericDecimal(it) },
                        label = { Text("Fat   (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = numericDecimal(it) },
                        label = { Text("Carbs (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = proteins,
                        onValueChange = { proteins = numericDecimal(it) },
                        label = { Text("Protein (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(
                    calories.toIntOrNull() ?: 0,
                    name,
                    serving,
                    fats.toDoubleOrNull() ?: 0.0,
                    carbs.toDoubleOrNull() ?: 0.0,
                    proteins.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Add", color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = White) } },
        backgroundColor = BgBlack,
        shape = MaterialTheme.shapes.large
    )
}

//date selector - vm keeps offset, arrows disabled if past a year, dropdown for entering date
@Composable
private fun DateSelectorDropdown(
    currentOffset: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: (Int) -> Unit,
    onOpenManual: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    var expanded by remember { mutableStateOf(false) }

    //label real date
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(tz) }
    val dateLabel = remember(currentOffset, today) {
        offsetToDate(currentOffset, today).formatYmd()
    }

    //cannot reach exactly ±1 year
    val minOffsetExclusive = 1 - 364
    val maxOffsetExclusive = 1 + 364
    val canPrev = currentOffset > minOffsetExclusive
    val canNext = currentOffset < maxOffsetExclusive

    Surface(
        color = BgBlack,
        contentColor = White,
        elevation = 0.dp,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                BorderStroke(1.5.dp, Brush.linearGradient(listOf(Silver.copy(.35f), Cyan.copy(.25f)))),
                shape
            )
    ) {
        Box {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "←",
                    color = if (canPrev) White else Silver,
                    modifier = Modifier
                        .padding(6.dp)
                        .let { if (canPrev) it.clickable { onPrev() } else it }
                )
                Text(
                    text = dateLabel,
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
                Text(
                    "→",
                    color = if (canNext) White else Silver,
                    modifier = Modifier
                        .padding(6.dp)
                        .let { if (canNext) it.clickable { onNext() } else it }
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth().background(BgBlack)
            ) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onOpenManual()
                }) {
                    Text("Enter Date", color = White)
                }
            }
        }
    }
}

//manual date input with three fields
@Composable
private fun DateInputDialog(
    currentOffset: Int,
    onDismiss: () -> Unit,
    onConfirmOffset: (Int) -> Unit
) {
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(tz) }

    // strictly within (today-1year, today+1year)
    val minInclusive = remember { today.minus(DatePeriod(years = 1)).plus(1, DateTimeUnit.DAY) }
    val maxInclusive = remember { today.plus(DatePeriod(years = 1)).minus(1, DateTimeUnit.DAY) }

    //current offset
    val prefill = remember { offsetToDate(currentOffset, today) }

    var year by remember { mutableStateOf(prefill.year.toString()) }
    var month by remember { mutableStateOf(prefill.monthNumber.toString()) }
    var day by remember { mutableStateOf(prefill.dayOfMonth.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun digitsOnly(src: String, maxLen: Int): String =
        src.filter(Char::isDigit).take(maxLen)

    fun validateAndConfirm() {
        val y = year.toIntOrNull()
        val m = month.toIntOrNull()
        val d = day.toIntOrNull()
        if (y == null || m == null || d == null) {
            error = "Please enter numbers for all fields."
            return
        }
        if (year.length != 4) { error = "Year must be 4 digits."; return }
        if (m !in 1..12) { error = "Month must be 1–12."; return }
        if (d !in 1..31) { error = "Day must be 1–31."; return }

        val picked = try { LocalDate(y, m, d) } catch (_: Exception) {
            error = "That date isn’t valid."; return
        }

        if (picked < minInclusive || picked > maxInclusive) {
            error = "Pick a date within 1 year of today."
            return
        }

        error = null
        onConfirmOffset(dateToOffset(picked, today))
    }

    val fieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = White,
        focusedBorderColor = Cyan,
        unfocusedBorderColor = Silver,
        cursorColor = White,
        focusedLabelColor = Silver,
        unfocusedLabelColor = Silver,
        placeholderColor = Silver
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter date", color = White) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = digitsOnly(it, 4) },
                        label = { Text("YYYY") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = digitsOnly(it, 2) },
                        label = { Text("MM") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = digitsOnly(it, 2) },
                        label = { Text("DD") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(Modifier.height(6.dp))
                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(error!!, color = White, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { validateAndConfirm() }) { Text("Go", color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = White) } },
        backgroundColor = BgBlack,
        shape = MaterialTheme.shapes.large
    )
}

//helpers
//vm offset/label
private fun dayLabel(offset: Int): String =
    when (offset) {
        0 -> "Yesterday"
        1 -> "Today"
        2 -> "Tomorrow"
        else -> if (offset > 2) "+${offset - 1} days" else "${offset - 1} days"
    }

// 0 today, -1 yesterday, +1 tomorrow
private fun dateToOffset(
    date: LocalDate,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): Int {
    val delta = today.daysUntil(date)
    return 1 + delta
}

//convert offset back to a LocalDate
private fun offsetToDate(
    offset: Int,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): LocalDate {
    val delta = offset - 1
    return today.plus(delta, DateTimeUnit.DAY)
}

//format for LocalDate
private fun LocalDate.formatYmd(): String {
    val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$year $monthName $dayOfMonth"
}

