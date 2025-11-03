package com.teamnotfound.airise.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PlaylistAddCheck
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import notifications.WorkoutReminderUseCase
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.util.*

@Composable
fun WorkoutScreen(userRepository: IUserRepository, navController: NavHostController, reminder: WorkoutReminderUseCase) {
    val viewModel: WorkoutViewModel = remember { WorkoutViewModel(userRepository, reminder) }
    val state by viewModel.uiState.collectAsState()
    val bottomNav = rememberNavController()

    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        viewModel.refresh(force = false)
    }

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
                    navController.navigate(AppScreen.HOMESCREEN.name) {
                        launchSingleTop = true
                        navController.graph.startDestinationRoute?.let { startRoute ->
                            popUpTo(startRoute) { saveState = true }
                        }
                        restoreState = true
                    }
                },
                onWorkoutClick = {
                    navController.navigate(AppScreen.WORKOUT.name) { launchSingleTop = true }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ){
                ExtendedFloatingActionButton(
                    text = { Text("Log") },
                    onClick = {
                        viewModel.logAll()
                        viewModel.onWorkoutLogged()
                    },
                    backgroundColor = DeepBlue,
                    contentColor = White,
                    icon = { Icon(Icons.Rounded.PlaylistAddCheck, contentDescription = null) },
                    modifier = Modifier
                        .testTag("log_button")
                        .alpha(0.75f)
                )

                Spacer(Modifier.width(16.dp))

                FloatingActionButton(
                    onClick = {
                        navController.navigate(AppScreen.AI_CHAT.name)
                    },
                    backgroundColor = DeepBlue,
                    contentColor = White,
                    modifier = Modifier
                        .testTag("chat_button")
                        .alpha(0.75f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Open Ai Chat"
                    )
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(inner)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .testTag("workout_screen")
        ) {
            when (val s = state) {
                is WorkoutUiState.Success -> {
                    val program = s.programDoc.program

                    HeroHeader(
                        title = program.templateName.ifBlank { "Workout" },
                        metaLeft = "${program.days} day",
                        metaRight = program.type.toString()
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(program.schedule, key = { it.dayIndex }) { day ->
                            DaySection(
                                dayIndex = day.dayIndex,
                                day = day.dayName,
                                focus = day.focus,
                                count = day.exercises.size,
                                expanded = expanded[day.dayName] ?: false,
                                onToggle = { expanded[day.dayName] = !(expanded[day.dayName] ?: false) },
                                items = day.exercises,
                                onChange = { exerciseName, reps, weight ->
                                    viewModel.changeSet(day.dayIndex, exerciseName, reps, weight)
                                }
                            )
                        }
                    }
                }
                is WorkoutUiState.Error -> CenteredMessagePrimary("Something went wrong.")
                WorkoutUiState.Loading -> LoadingState()
            }
        }
    }
}

@Composable
private fun LoadingState() {
    // Subtle loading shimmer without external libs – animated blurred gradients
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.18f), Transparent)
                    )
                )
                .blur(36.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(color = White)
            Spacer(Modifier.height(12.dp))
            Text("Loading program…", color = Silver)
        }
    }
}

@Composable
private fun HeroHeader(title: String, metaLeft: String, metaRight: String) {
    val headShape = RoundedCornerShape(20.dp)

    Box(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp)) {
        // Ambient glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.28f), Transparent)
                    )
                )
                .blur(28.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        Card(
            backgroundColor = BgBlack,
            contentColor = White,
            elevation = 0.dp,
            shape = headShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(Cyan.copy(alpha = 0.55f), Silver.copy(alpha = 0.25f)))
                    ),
                    headShape
                )
                .clip(headShape)
        ) {
            // Subtle diagonal sheen layer
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.05f), Transparent)
                        )
                    )
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold),
                        color = White,
                        modifier = Modifier.testTag("title_workout")
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Pill(text = metaLeft, border = NeonGreen)
                        Spacer(Modifier.width(8.dp))
                        Pill(text = metaRight, border = Cyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessagePrimary(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, color = White) }
}

@Composable
internal fun DaySection(
    dayIndex: Int,
    day: String,
    focus: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    items: List<UserExerciseEntry>,
    onChange: (exerciseName: String, reps: Int?, weight: Double?) -> Unit
) {
    val headerShape = RoundedCornerShape(18.dp)
    val rotate by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing)
    )

    Box(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
        // Cyan halo
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.radialGradient(colors = listOf(Cyan.copy(alpha = 0.18f), Transparent)))
                .blur(22.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        Column(
            Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.5.dp, Brush.linearGradient(listOf(Silver.copy(.35f), Cyan.copy(.25f)))) ,
                    headerShape
                )
                .clip(headerShape)
                .background(BgBlack)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onToggle() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("day_header_${day}")
                .animateContentSize(tween(220, easing = FastOutSlowInEasing))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AccentBar()
                        Spacer(Modifier.width(8.dp))
                        Text(day, color = White, style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold))
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val f = focus.ifBlank { "Workout" }
                        Text(f, color = Silver, style = MaterialTheme.typography.caption)
                        Spacer(Modifier.width(8.dp))
                        DividerDot()
                        Spacer(Modifier.width(8.dp))
                        Text("$count exercises", color = Silver, style = MaterialTheme.typography.caption)
                    }
                }
                Surface(color = Transparent, shape = CircleShape, border = BorderStroke(1.dp, Silver.copy(alpha = 0.35f))) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(26.dp).rotate(rotate).padding(2.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(10.dp))
                    if (items.isEmpty()) {
                        EmptyDayCard(day)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items.forEach { exercise ->
                                WorkoutCard(
                                    exercise = exercise,
                                    onChange = { reps, weight -> onChange(exercise.name, reps, weight) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun DividerDot() {
    Box(Modifier.size(4.dp).clip(CircleShape).background(Silver.copy(alpha = 0.7f)))
}

@Composable private fun AccentBar() {
    Box(
        Modifier
            .height(16.dp)
            .width(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Brush.verticalGradient(listOf(Cyan, NeonGreen)))
    )
}

@Composable
private fun EmptyDayCard(day: String) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Silver.copy(.5f), Silver.copy(.15f)))), shape)
            .clip(shape)
            .background(BgBlack)
            .padding(16.dp)
            .testTag("none_${day}"),
        contentAlignment = Alignment.CenterStart
    ) { Text("Rest day", color = Silver) }
}

@Composable
private fun WorkoutCard(
    exercise: UserExerciseEntry,
    onChange: (reps: Int?, weight: Double?) -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val focus = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
        // Ambient glow layers
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.radialGradient(colors = listOf(Cyan.copy(alpha = 0.16f), Transparent)))
                .blur(20.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.radialGradient(colors = listOf(Orange.copy(alpha = 0.10f), Transparent)))
                .blur(24.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Card(
            backgroundColor = BgBlack,
            contentColor = White,
            elevation = 0.dp,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(Silver.copy(alpha = 0.35f), Cyan.copy(alpha = 0.25f)))
                    ),
                    shape
                )
                .clip(shape)
                .testTag("card_${exercise.name}")
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.03f), Transparent)))
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        exercise.name,
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold),
                        color = White
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("${exercise.sets} sets", NeonGreen)
                        Pill("${exercise.targetReps} reps", Cyan)
                        Pill("${exercise.weight.value} ${exercise.weight.unit}", Orange)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberField(
                            label = "Reps done",
                            value = exercise.repsCompleted,
                            enabled = true,
                            imeAction = ImeAction.Next,
                            onImeAction = { focus.moveFocus(FocusDirection.Right) },
                            onValue = { v -> onChange(v, null) },
                            modifier = Modifier.weight(1f).testTag("reps_${exercise.name}")
                        )
                        DecimalNumberField(
                            label = "Weight used (${exercise.weight.unit})",
                            value = exercise.weight.value.toDouble(),
                            enabled = true,
                            imeAction = ImeAction.Done,
                            onImeAction = { focus.clearFocus() },
                            onValue = { v -> onChange(null, v) },
                            modifier = Modifier.weight(1f).testTag("weight_${exercise.name}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, border: Color) {
    val shape = RoundedCornerShape(999.dp)
    Surface(
        color = Transparent,
        contentColor = White,
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(border.copy(alpha = .95f), border.copy(alpha = .45f)))),
        shape = shape,
        elevation = 0.dp
    ) {
        Text(text = text, color = White, style = MaterialTheme.typography.caption, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun AnimatedExpand(visible: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))) { if (visible) content() }
}

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
    val shape = RoundedCornerShape(12.dp)

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it.filter(Char::isDigit).take(4)
            onValue(text.toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        textStyle = LocalTextStyle.current.copy(color = White),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = White,
            focusedBorderColor = Cyan,
            unfocusedBorderColor = Silver,
            cursorColor = White,
            focusedLabelColor = Silver,
            unfocusedLabelColor = Silver
        ),
        shape = shape,
        modifier = modifier.background(Transparent, shape)
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
    val shape = RoundedCornerShape(12.dp)

    OutlinedTextField(
        value = text,
        onValueChange = {
            var s = it.filter { ch -> ch.isDigit() || ch == '.' }
            val firstDot = s.indexOf('.')
            if (firstDot != -1) s = s.substring(0, firstDot + 1) + s.substring(firstDot + 1).replace(".", "")
            text = s
            onValue(s.toDoubleOrNull())
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        textStyle = LocalTextStyle.current.copy(color = White),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = White,
            focusedBorderColor = Cyan,
            unfocusedBorderColor = Silver,
            cursorColor = White,
            focusedLabelColor = Silver,
            unfocusedLabelColor = Silver
        ),
        shape = shape,
        modifier = modifier.background(Transparent, shape)
    )
}
