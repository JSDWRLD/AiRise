package com.teamnotfound.airise.meal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete

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

    // edit/delete targets
    var editTarget by remember { mutableStateOf<FoodEntry?>(null) }
    var deleteTarget by remember { mutableStateOf<FoodEntry?>(null) }

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
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            )
        },
        isFloatingActionButtonDocked = false
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(paddingValues)
        ) {
            // Header / date + summary
            Spacer(Modifier.height(8.dp))
            SummaryBanner(
                goal = s.goal,
                food = vm.totalFood,
                exercise = s.exercise,
                remaining = vm.remaining,
                onEditGoal = { showGoalEdit = true },
                onOpenDate = { showDateInput = true }
            )

            // Meals list
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val meals = s.day.meals
                item {
                    MealSection(
                        title = "Breakfast",
                        items = meals.breakfast,
                        onAddFood = { quickAddMeal = MealType.Breakfast; showQuickAdd = true },
                        onEditItem = { editTarget = it },
                        onDeleteItem = { if (it.id != null) deleteTarget = it }
                    )
                }
                item {
                    MealSection(
                        title = "Lunch",
                        items = meals.lunch,
                        onAddFood = { quickAddMeal = MealType.Lunch; showQuickAdd = true },
                        onEditItem = { editTarget = it },
                        onDeleteItem = { if (it.id != null) deleteTarget = it }
                    )
                }
                item {
                    MealSection(
                        title = "Dinner",
                        items = meals.dinner,
                        onAddFood = { quickAddMeal = MealType.Dinner; showQuickAdd = true },
                        onEditItem = { editTarget = it },
                        onDeleteItem = { if (it.id != null) deleteTarget = it }
                    )
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

    // Confirm Delete dialog
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete entry?", color = White) },
            text = { Text(deleteTarget!!.name, color = Silver) },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget?.id?.let { vm.deleteEntry(it) }
                    deleteTarget = null
                }) { Text("Delete", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = White) }
            },
            backgroundColor = BgBlack,
            shape = MaterialTheme.shapes.large
        )
    }

    // Quick Edit dialog
    if (editTarget != null) {
        var name by remember { mutableStateOf(editTarget!!.name) }
        var calories by remember { mutableStateOf(editTarget!!.calories.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Edit food", color = White) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Calories") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updated = editTarget!!.copy(
                        name = name,
                        calories = calories.toDoubleOrNull() ?: editTarget!!.calories
                    )
                    editTarget!!.id?.let { vm.editEntry(it, updated) }
                    editTarget = null
                }) { Text("Save", color = White) }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("Cancel", color = White) } },
            backgroundColor = BgBlack,
            shape = MaterialTheme.shapes.large
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
    onEditGoal: () -> Unit,
    onOpenDate: () -> Unit
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
                Column(Modifier.weight(1f)) {
                    Text("Calories", color = White, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$remaining", color = White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(6.dp))
                        Text("remaining", color = Silver, fontSize = 12.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Daily goal", color = Silver, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$goal", color = White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = onEditGoal, border = BorderStroke(1.dp, Cyan.copy(alpha = .45f))) { Text("Edit", color=White) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatChip("Food", food.toString())
                StatChip("Exercise", exercise.toString())
                DateChip(onOpen = onOpenDate) { Text("${Clock.System.todayIn(TimeZone.currentSystemDefault())}", color = White) }
            }
        }
    }
}

@Composable
private fun StatChip(title: String, value: String, onValueClick: (() -> Unit)? = null, valueText: @Composable () -> Unit = { Text(value, color = White, fontSize = 18.sp, fontWeight = FontWeight.Medium) }) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    onAddFood: () -> Unit,
    onEditItem: (FoodEntry) -> Unit,
    onDeleteItem: (FoodEntry) -> Unit
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
                    onEdit = { onEditItem(item) },
                    onDelete = { onDeleteItem(item) }
                )
            }

            OutlinedButton(
                onClick = onAddFood,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                border = BorderStroke(1.dp, Cyan.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Cyan,
                    backgroundColor = Color.Transparent
                )
            ) {
                Text("Add food", color=White)
            }
        }
    }
}

// ----------------------
// Row for a single entry
// ----------------------
@Composable
private fun FoodRow(
    item: FoodEntry,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.calories.toInt().toString(), color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = White
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = White
                        )
                    }
                }
            }
        }
    }
}

// ----------------------
// Dialogs and inputs
// ----------------------
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
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = { text.toIntOrNull()?.let(onConfirm) }) { Text("Save", color = White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = White) }
        },
        backgroundColor = BgBlack
    )
}

@Composable
private fun DateChip(onOpen: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = DeepBlue,
        contentColor = White
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(24.dp))
                .clickable { onOpen() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { content() }
    }
}

@Composable
private fun MealPickerDialog(onDismiss: () -> Unit, onPick: (MealType) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log to which meal?", color = White) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { onPick(MealType.Breakfast) }) { Text("Breakfast") }
                    DropdownMenuItem(onClick = { onPick(MealType.Lunch) }) { Text("Lunch") }
                    DropdownMenuItem(onClick = { onPick(MealType.Dinner) }) { Text("Dinner") }
                }
            }
        },
        confirmButton = {},
        backgroundColor = BgBlack
    )
}

@Composable
private fun QuickAddDialog(
    meal: MealType,
    onDismiss: () -> Unit,
    onAdd: (calories: Int, name: String, serving: String, fats: Double, carbs: Double, proteins: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var serving by remember { mutableStateOf("1") }
    var fats by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }

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
        title = { Text("Quick add to ${'$'}meal", color = White) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, colors = fieldColors)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = calories, onValueChange = { v -> calories = v.filter { it.isDigit() } }, label = { Text("Calories") }, colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = serving, onValueChange = { serving = it }, label = { Text("Serving") }, colors = fieldColors)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fats, onValueChange = { fats = it }, label = { Text("Fat g") }, colors = fieldColors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs g") }, colors = fieldColors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = proteins, onValueChange = { proteins = it }, label = { Text("Protein g") }, colors = fieldColors, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(
                    calories.toIntOrNull() ?: 0,
                    name.ifBlank { "Quick Add" },
                    serving,
                    fats.toDoubleOrNull() ?: 0.0,
                    carbs.toDoubleOrNull() ?: 0.0,
                    proteins.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Add", color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = White) } },
        backgroundColor = BgBlack
    )
}

@Composable
private fun DateInputDialog(
    currentOffset: Int,
    onDismiss: () -> Unit,
    onConfirmOffset: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentOffset.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to day offset", color = White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v.filter { it.isDigit() }.take(4) },
                label = { Text("Offset") }
            )
        },
        confirmButton = {
            TextButton(onClick = { text.toIntOrNull()?.let(onConfirmOffset) }) { Text("Go", color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = White) } },
        backgroundColor = BgBlack
    )
}
