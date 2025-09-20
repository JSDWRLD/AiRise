package com.teamnotfound.airise.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModelContract) {
    val state by viewModel.uiState.collectAsState()
    val bottomNav = rememberNavController()

    // fixed 7 day expand/collapse
    val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply { days.forEach { put(it, true) } }
    }

    // track rows are editable
    val editingKeys = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = { BottomNavigationBar(navController = bottomNav) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Log") },
                onClick = { viewModel.logAll() },
                backgroundColor = DeepBlue,
                contentColor = White
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(inner)
                .padding(horizontal = 12.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                text = "Workout",
                color = White,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = White)
                }
                state.error != null -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Couldn't load workouts", color = White)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = viewModel::refresh) { Text("Retry") }
                }
                else -> {
                    // ex show all current items on Monday, others None
                    val schedule: Map<String, List<WorkoutRow>> = remember(state.items) {
                        mapOf(
                            "Mon" to state.items,
                            "Tue" to emptyList(),
                            "Wed" to emptyList(),
                            "Thu" to emptyList(),
                            "Fri" to emptyList(),
                            "Sat" to emptyList(),
                            "Sun" to emptyList(),
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(days) { day ->
                            DaySection(
                                day = day,
                                expanded = expanded[day] == true,
                                onToggle = { expanded[day] = !(expanded[day] ?: true) },
                                items = schedule[day].orEmpty(),
                                isEditing = { workoutId, setIndex ->
                                    editingKeys["$workoutId#$setIndex"] == true
                                },
                                onToggleEdit = { workoutId, setIndex ->
                                    val key = "$workoutId#$setIndex"
                                    editingKeys[key] = !(editingKeys[key] ?: false)
                                },
                                onChange = { workoutId, setIndex, reps, weight ->
                                    viewModel.changeSet(workoutId, setIndex, reps, weight)
                                },
                                onExerciseNotes = { workoutId, notes ->
                                    viewModel.changeExerciseNotes(workoutId, notes)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySection(
    day: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    items: List<WorkoutRow>,
    isEditing: (workoutId: String, index: Int) -> Boolean,
    onToggleEdit: (workoutId: String, index: Int) -> Unit,
    onChange: (workoutId: String, index: Int, reps: Int?, weight: Double?) -> Unit,
    onExerciseNotes: (workoutId: String, notes: String) -> Unit
) {
    val headerShape = RoundedCornerShape(12.dp)

    // day header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, White, headerShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(day, color = White, style = MaterialTheme.typography.subtitle1)
        Text(if (expanded) "▲" else "▼", color = White)
    }

    if (expanded) {
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Silver, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("None", color = Silver)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { row ->
                    WorkoutCard(
                        row = row,
                        isEditing = { setIndex -> isEditing(row.id, setIndex) },
                        onToggleEdit = { setIndex -> onToggleEdit(row.id, setIndex) },
                        onChange = { setIndex, reps, weight -> onChange(row.id, setIndex, reps, weight) },
                        onExerciseNotes = { notes -> onExerciseNotes(row.id, notes) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    row: WorkoutRow,
    isEditing: (index: Int) -> Boolean,
    onToggleEdit: (index: Int) -> Unit,
    onChange: (index: Int, reps: Int?, weight: Double?) -> Unit,
    onExerciseNotes: (notes: String) -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val focus = LocalFocusManager.current

    Card(
        backgroundColor = BgBlack,
        contentColor = White,
        elevation = 0.dp,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, White, shape)
            .padding(14.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(row.name, style = MaterialTheme.typography.h6, color = White)

            // planned line
            val plannedBits = buildList {
                row.plannedReps?.let { add("$it reps") }
                row.plannedWeightLbs?.let { add("@ ${it} lbs") }
            }.joinToString(" ")
            if (plannedBits.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Planned: ${row.sets.size} sets • $plannedBits", color = Silver)
            }

            Spacer(Modifier.height(8.dp))

            row.sets.forEachIndexed { i, set ->
                val editing = isEditing(i)

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberField(
                        label = "Reps done",
                        value = set.repsCompleted,
                        enabled = editing,
                        imeAction = ImeAction.Next,
                        onImeAction = { focus.moveFocus(FocusDirection.Right) },
                        onValue = { v -> onChange(i, v, null) },
                        modifier = Modifier.weight(1f)
                    )
                    DecimalNumberField(
                        label = "Weight used (lbs)",
                        value = set.weightUsedLbs,
                        enabled = editing,
                        imeAction = ImeAction.Done,
                        onImeAction = { focus.clearFocus() },
                        onValue = { v -> onChange(i, null, v) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onToggleEdit(i) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (editing) "Done editing" else "Edit",
                            tint = White
                        )
                    }
                }

                if (i != row.sets.lastIndex) Spacer(Modifier.height(8.dp))
            }

            // notes box per exercise
            Spacer(Modifier.height(12.dp))
            var notes by remember(row.exerciseNotes) { mutableStateOf(row.exerciseNotes) }
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    onExerciseNotes(it)
                },
                label = { Text("Notes") },
                singleLine = false,
                textStyle = LocalTextStyle.current.copy(color = White),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = White,
                    focusedBorderColor = White,
                    unfocusedBorderColor = Silver,
                    cursorColor = White,
                    focusedLabelColor = Silver,
                    unfocusedLabelColor = Silver
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//inputs
@Composable
private fun NumberField(
    label: String,
    value: Int,
    enabled: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    onValue: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(if (value == 0) "" else value.toString()) }

    if (enabled) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it.filter(Char::isDigit).take(4)
                onValue(text.toIntOrNull() ?: 0)
            },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            textStyle = LocalTextStyle.current.copy(color = White),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = White,
                focusedBorderColor = White,
                unfocusedBorderColor = Silver,
                cursorColor = White,
                focusedLabelColor = Silver,
                unfocusedLabelColor = Silver
            ),
            modifier = modifier
        )
    } else {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = modifier
                .border(2.dp, Silver, shape)
                .padding(12.dp)
        ) {
            Text(label, color = Silver, style = MaterialTheme.typography.caption)
            Spacer(Modifier.height(4.dp))
            Text(if (value == 0) "-" else value.toString(), color = White)
        }
    }
}

@Composable
private fun DecimalNumberField(
    label: String,
    value: Double?,
    enabled: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    onValue: (Double?) -> Unit, // null means empty
    modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(value?.toString().orEmpty()) }

    if (enabled) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                // allow digits and a single dot
                var s = it.filter { ch -> ch.isDigit() || ch == '.' }
                val firstDot = s.indexOf('.')
                if (firstDot != -1) {
                    s = s.substring(0, firstDot + 1) + s.substring(firstDot + 1).replace(".", "")
                }
                text = s
                onValue(s.toDoubleOrNull())
            },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            textStyle = LocalTextStyle.current.copy(color = White),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = White,
                focusedBorderColor = White,
                unfocusedBorderColor = Silver,
                cursorColor = White,
                focusedLabelColor = Silver,
                unfocusedLabelColor = Silver
            ),
            modifier = modifier
        )
    } else {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = modifier
                .border(2.dp, Silver, shape)
                .padding(12.dp)
        ) {
            Text(label, color = Silver, style = MaterialTheme.typography.caption)
            Spacer(Modifier.height(4.dp))
            Text(value?.toString() ?: "-", color = White)
        }
    }
}
