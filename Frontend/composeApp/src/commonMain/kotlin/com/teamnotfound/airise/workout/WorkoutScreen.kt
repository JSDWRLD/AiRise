package com.teamnotfound.airise.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavHostController
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import notifications.WorkoutReminderUseCase
import androidx.compose.ui.platform.testTag

@Composable
fun WorkoutScreen(userRepository: UserRepository, navController: NavHostController, reminder: WorkoutReminderUseCase) {
    val viewModel: WorkoutViewModel = remember {
        WorkoutViewModel(userRepository, reminder)
    }
    val state by viewModel.uiState.collectAsState()
    val bottomNav = rememberNavController()

    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNav,
                appNavController = navController,
                onCommunityClick = {
                    navController.navigate(AppScreen.CHALLENGES.name) { launchSingleTop = true }
                },
                onOverviewClick = {
                    navController.navigate(AppScreen.HOMESCREEN.name) { launchSingleTop = true }
                },
                onWorkoutClick = {
                    navController.navigate(AppScreen.WORKOUT.name) { launchSingleTop = true }
                }
            )
        },

        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Log") },
                onClick = {
                    viewModel.logAll()
                    viewModel.onWorkoutLogged()
                          },
                backgroundColor = DeepBlue,
                contentColor = White,
                modifier = Modifier.testTag("log_button") // testing
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
                .testTag("workout_screen") //testing
        ) {
            Text(
                text = "Workout",
                color = White,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 12.dp)
                    .testTag("title_workout") //testing
            )

            when (val s = state) {
                is WorkoutUiState.Success -> {
                    val programDoc = s.programDoc
                    val schedule = programDoc.program.schedule

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(schedule) { day ->
                            DaySection(
                                day = day.dayName,
                                expanded = expanded[day.dayName] == true,
                                onToggle = {
                                    expanded[day.dayName] = !(expanded[day.dayName] ?: true)
                                    viewModel.setActiveDay(day.dayIndex, day.dayName, day.focus)
                                },
                                items = day.exercises,
                                onChange = { exerciseName, reps, weight ->
                                    viewModel.changeSet(day.dayIndex, exerciseName, reps, weight)
                                }
                            )
                        }
                    }
                }
                is WorkoutUiState.Error -> {
                    Text("Something went wrong.", color = White)
                }
                WorkoutUiState.Loading -> {
                    CircularProgressIndicator(color = White)
                }
            }
        }
    }
}

@Composable
internal fun DaySection(
    day: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    items: List<UserExerciseEntry>,
    onChange: (exerciseName: String, reps: Int?, weight: Double?) -> Unit
) {
    val headerShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, White, headerShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable { onToggle() }
            .testTag("day_header_$day"), //test
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
                    .padding(16.dp)
                    .testTag("none_$day"), //testing
                contentAlignment = Alignment.CenterStart
            ) {
                Text("None", color = Silver)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { exercise ->
                    WorkoutCard(
                        exercise = exercise,
                        onChange = { reps, weight ->
                            onChange(exercise.name, reps, weight)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    exercise: UserExerciseEntry,
    onChange: (reps: Int?, weight: Double?) -> Unit
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
            .testTag("card_${exercise.name}") //testing
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(exercise.name, style = MaterialTheme.typography.h6, color = White)

            val planned = "${exercise.sets} sets • ${exercise.targetReps} reps @ ${exercise.weight.value} ${exercise.weight.unit}"
            Spacer(Modifier.height(4.dp))
            Text("Planned: $planned", color = Silver)

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberField(
                    label = "Reps done",
                    value = exercise.repsCompleted,
                    enabled = true,
                    imeAction = ImeAction.Next,
                    onImeAction = { focus.moveFocus(FocusDirection.Right) },
                    onValue = { v -> onChange(v, null) },
                    modifier = Modifier.weight(1f).testTag("reps_${exercise.name}")//testing
                )
                DecimalNumberField(
                    label = "Weight used (${exercise.weight.unit})",
                    value = exercise.weight.value.toDouble(),
                    enabled = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { focus.clearFocus() },
                    onValue = { v -> onChange(null, v) },
                    modifier = Modifier.weight(1f).testTag("weight_${exercise.name}")//testing
                )
            }
        }
    }
}

// --- Inputs ---
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

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it.filter(Char::isDigit).take(4)
            onValue(text.toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
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
}

@Composable
private fun DecimalNumberField(
    label: String,
    value: Double?,
    enabled: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    onValue: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(value?.toString().orEmpty()) }

    OutlinedTextField(
        value = text,
        onValueChange = {
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
        enabled = enabled,
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
}
