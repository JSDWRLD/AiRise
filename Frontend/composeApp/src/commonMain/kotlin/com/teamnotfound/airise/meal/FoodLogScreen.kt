package com.teamnotfound.airise.meal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.util.*
import kotlinx.datetime.*

@Composable
fun FoodLogScreen(
    appNavController: NavHostController,
    vm: MealViewModel
) {
    // Collect the UI state properly
    val uiState = vm.uiState
    val totalFood = vm.totalFood
    val remaining = vm.remaining

    var showQuickAdd by remember { mutableStateOf(false) }
    var quickAddMeal by remember { mutableStateOf(MealType.Breakfast) }
    var showGoalEdit by remember { mutableStateOf(false) }
    var showMealPicker by remember { mutableStateOf(false) }
    var showDateInput by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<FoodEntry?>(null) }
    val menuController = remember { InlineMenuController() }

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
            FloatingActionButton(
                onClick = {
                    appNavController.navigate(AppScreen.AI_CHAT.name)
                },
                backgroundColor = DeepBlue,
                contentColor = White,
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(0.75f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Open Ai Chat"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                DateSelectorDropdown(
                    currentOffset = uiState.dayOffset,
                    onPrev = vm::previousDay,
                    onNext = vm::nextDay,
                    onSelect = vm::setDayOffset,
                    onOpenManual = { showDateInput = true },
                    menuController = menuController
                )

                Spacer(Modifier.height(8.dp))

                SummaryBanner(
                    goal = uiState.goal,
                    food = totalFood,
                    exercise = uiState.exercise,
                    remaining = remaining,
                    onEditGoalClick = { showGoalEdit = true }
                )

                Spacer(Modifier.height(8.dp))

                // Show error message if present
                if (uiState.errorMessage != null) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = White,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                val meals = uiState.day.meals
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        MealSection(
                            title = "Breakfast",
                            items = meals.breakfast,
                            onAddFood = {
                                quickAddMeal = MealType.Breakfast
                                showQuickAdd = true
                            },
                            onEditFood = { entry ->
                                editingEntry = entry
                                showEditDialog = true
                            },
                            onDeleteFood = { entryId ->
                                vm.deleteEntry(entryId)
                            },
                            menuController = menuController
                        )
                    }
                    item {
                        MealSection(
                            title = "Lunch",
                            items = meals.lunch,
                            onAddFood = {
                                quickAddMeal = MealType.Lunch
                                showQuickAdd = true
                            },
                            onEditFood = { entry ->
                                editingEntry = entry
                                showEditDialog = true
                            },
                            onDeleteFood = { entryId ->
                                vm.deleteEntry(entryId)
                            },
                            menuController = menuController
                        )
                    }
                    item {
                        MealSection(
                            title = "Dinner",
                            items = meals.dinner,
                            onAddFood = {
                                quickAddMeal = MealType.Dinner
                                showQuickAdd = true
                            },
                            onEditFood = { entry ->
                                editingEntry = entry
                                showEditDialog = true
                            },
                            onDeleteFood = { entryId ->
                                vm.deleteEntry(entryId)
                            },
                            menuController = menuController
                        )
                    }
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Cyan)
                }
            }

            InlineMenuHost(
                controller = menuController,
                screenPadding = padding
            )
        }
    }

    // Dialogs (now in-tree, iOS-safe)
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
            initial = uiState.goal.toString(),
            onConfirm = { vm.setGoal(it); showGoalEdit = false },
            onDismiss = { showGoalEdit = false }
        )
    }

    if (showDateInput) {
        DateInputDialog(
            currentOffset = uiState.dayOffset,
            onDismiss = { showDateInput = false },
            onConfirmOffset = { newOffset ->
                vm.setDayOffset(newOffset)
                showDateInput = false
            }
        )
    }

    if (showEditDialog && editingEntry != null) {
        EditFoodDialog(
            entry = editingEntry!!,
            onDismiss = {
                showEditDialog = false
                editingEntry = null
            },
            onSave = { updated ->
                vm.editEntry(editingEntry!!.id, updated)
                showEditDialog = false
                editingEntry = null
            }
        )
    }
}

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

@Composable
private fun MealSection(
    title: String,
    items: List<FoodEntry>,
    onAddFood: () -> Unit,
    onEditFood: (FoodEntry) -> Unit,
    onDeleteFood: (String) -> Unit,
    menuController: InlineMenuController
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

            items.forEach { item ->
                FoodRow(
                    item = item,
                    onEdit = { onEditFood(item) },
                    onDelete = { onDeleteFood(item.id) },
                    menuController = menuController // ← pass down
                )
            }

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
private fun FoodRow(
    item: FoodEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    menuController: InlineMenuController
) {
    var showMenu by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    val anchor = rememberInlineMenuAnchor()

    Surface(
        color = DeepBlue.copy(alpha = 0.20f),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .inlineMenuAnchor(anchor)
            .clickable {
                menuController.show(anchor) {
                    InlineMenuItem("Edit") { menuController.hide(); onEdit() }
                    InlineMenuItem("Delete") { menuController.hide(); onDelete() }
                }
            },
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

    AppDialog(onDismissRequest = onDismiss) {
        Text(title, color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { v -> text = v.filter { it.isDigit() }.take(5) },
            label = { Text("Calories") },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel", color = White) }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { onConfirm(text.toIntOrNull() ?: 0) }) { Text("Save", color = White) }
        }
    }
}

@Composable
private fun MealPickerDialog(
    onDismiss: () -> Unit,
    onPick: (MealType) -> Unit
) {
    AppDialog(onDismissRequest = onDismiss) {
        Text("Log to", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MealPickRow("Breakfast") { onPick(MealType.Breakfast) }
            MealPickRow("Lunch") { onPick(MealType.Lunch) }
            MealPickRow("Dinner") { onPick(MealType.Dinner) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel", color = White) }
        }
    }
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
private fun AppDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    // Full-screen scrim that actually covers the screen on iOS (inside layout tree)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismissRequest() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = BgBlack,
            shape = shape,
            elevation = 0.dp,
            modifier = modifier
                .padding(24.dp)
                .widthIn(min = 280.dp, max = 520.dp)
                .clickable( // consume taps
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* no-op */ }
                .imePadding()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                content = content
            )
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

    AppDialog(onDismissRequest = onDismiss) {
        Text("Quick Add — ${meal.name}", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it.filter(Char::isDigit).take(5) },
            label = { Text("Calories") },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = serving,
            onValueChange = { serving = it },
            label = { Text("Serving") },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Divider(color = DeepBlue.copy(alpha = 0.35f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fats,
                onValueChange = { fats = numericDecimal(it) },
                label = { Text("Fat   (g)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = fieldColors,
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = numericDecimal(it) },
                label = { Text("Carbs (g)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = fieldColors
            )
            OutlinedTextField(
                value = proteins,
                onValueChange = { proteins = numericDecimal(it) },
                label = { Text("Protein (g)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = fieldColors
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel", color = White) }
            Spacer(Modifier.width(8.dp))
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
        }
    }
}

@Composable
private fun EditFoodDialog(
    entry: FoodEntry,
    onDismiss: () -> Unit,
    onSave: (FoodEntry) -> Unit
) {
    var calories by remember { mutableStateOf(entry.calories.toInt().toString()) }
    var name by remember { mutableStateOf(entry.name) }
    var fats by remember { mutableStateOf(entry.fats.toString()) }
    var carbs by remember { mutableStateOf(entry.carbs.toString()) }
    var proteins by remember { mutableStateOf(entry.proteins.toString()) }

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

    AppDialog(onDismissRequest = onDismiss) {
        Text("Edit Food", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
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
        Divider(color = DeepBlue.copy(alpha = 0.35f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fats,
                onValueChange = { fats = numericDecimal(it) },
                label = { Text("Fat   (g)") },
                singleLine = true,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = fieldColors
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = numericDecimal(it) },
                label = { Text("Carbs (g)") },
                singleLine = true,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = fieldColors
            )
            OutlinedTextField(
                value = proteins,
                onValueChange = { proteins = numericDecimal(it) },
                label = { Text("Protein (g)") },
                singleLine = true,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = fieldColors
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel", color = White) }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = {
                val updated = entry.copy(
                    calories = calories.toDoubleOrNull() ?: 0.0,
                    name = name,
                    fats = fats.toDoubleOrNull() ?: 0.0,
                    carbs = carbs.toDoubleOrNull() ?: 0.0,
                    proteins = proteins.toDoubleOrNull() ?: 0.0
                )
                onSave(updated)
            }) { Text("Save", color = White) }
        }
    }
}

@Composable
private fun DateSelectorDropdown(
    currentOffset: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: (Int) -> Unit,
    onOpenManual: () -> Unit,
    menuController: InlineMenuController
) {
    val shape = RoundedCornerShape(18.dp)

    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(tz) }
    val dateLabel = remember(currentOffset, today) {
        offsetToDate(currentOffset, today).formatYmd()
    }

    val minOffsetExclusive = 1 - 364
    val maxOffsetExclusive = 1 + 364
    val canPrev = currentOffset > minOffsetExclusive
    val canNext = currentOffset < maxOffsetExclusive

    val anchor = rememberInlineMenuAnchor()

    val arrowTouch = 40.dp
    val arrowFont  = 20.sp
    val dateFont   = 18.sp

    Surface(
        color = BgBlack,
        contentColor = White,
        elevation = 0.dp,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                BorderStroke(1.5.dp, Brush.linearGradient(listOf(Silver.copy(alpha = .35f), Cyan.copy(alpha = .25f)))),
                shape
            )
            .inlineMenuAnchor(anchor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left arrow
            Box(
                modifier = Modifier
                    .size(arrowTouch)
                    .let { if (canPrev) it.clickable { onPrev() } else it },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    color = if (canPrev) White else Silver,
                    fontSize = arrowFont,
                    fontWeight = FontWeight.Medium
                )
            }

            // Date label (center)
            Text(
                text = dateLabel,
                color = White,
                fontSize = dateFont,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable {
                        menuController.show(anchor, matchAnchorWidth = true) {
                            InlineMenuItem("Enter Date") {
                                menuController.hide()
                                onOpenManual()
                            }
                        }
                    }
            )

            // Right arrow
            Box(
                modifier = Modifier
                    .size(arrowTouch)
                    .let { if (canNext) it.clickable { onNext() } else it },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    color = if (canNext) White else Silver,
                    fontSize = arrowFont,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DateInputDialog(
    currentOffset: Int,
    onDismiss: () -> Unit,
    onConfirmOffset: (Int) -> Unit
) {
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(tz) }

    val minInclusive = remember { today.minus(DatePeriod(years = 1)).plus(1, DateTimeUnit.DAY) }
    val maxInclusive = remember { today.plus(DatePeriod(years = 1)).minus(1, DateTimeUnit.DAY) }

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
            error = "That date isn't valid."; return
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

    AppDialog(onDismissRequest = onDismiss) {
        Text("Enter date", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
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
                    modifier = Modifier.weight(1.2f).fillMaxWidth()
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = digitsOnly(it, 2) },
                    label = { Text("MM") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors,
                    modifier = Modifier.weight(0.9f).fillMaxWidth(),
                )
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = digitsOnly(it, 2) },
                    label = { Text("DD") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors,
                    modifier = Modifier.weight(0.9f).fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(6.dp))
            if (error != null) {
                Spacer(Modifier.height(4.dp))
                Text(error!!, color = White, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel", color = White) }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { validateAndConfirm() }) { Text("Go", color = White) }
        }
    }
}

private fun dateToOffset(
    date: LocalDate,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): Int {
    val delta = today.daysUntil(date)
    return 1 + delta
}

private fun offsetToDate(
    offset: Int,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): LocalDate {
    val delta = offset - 1
    return today.plus(delta, DateTimeUnit.DAY)
}

private fun LocalDate.formatYmd(): String {
    val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$year $monthName $dayOfMonth"
}
