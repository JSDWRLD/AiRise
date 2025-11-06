package com.teamnotfound.airise.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Transparent
import com.teamnotfound.airise.util.White
import com.teamnotfound.airise.util.Orange
import kotlinx.coroutines.launch
import com.teamnotfound.airise.generativeAi.GeminiApi
import com.teamnotfound.airise.generativeAi.AiMessage
import com.teamnotfound.airise.generativeAi.GeminiSessionManager


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiChat(
    navController: NavHostController,
    workoutGoal: String? = null,
    dietaryGoal: String? = null,
    activityLevel: String? = null,
    fitnessLevel: String? = null,
    workoutLength: Int? = null,
    workoutRestrictions: String? = null,
    healthData: HealthData? = null,
    dailyProgressData: DailyProgressData? = null,
    onPickImageBytes: (suspend () -> ByteArray?) = { null }
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val api = remember { GeminiApi() }
    val session = remember { GeminiSessionManager(api, debounceMs = 500, maxChars = 4000) }

    val messageHistory = remember { mutableStateListOf<Message>() }

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messageHistory.size) {
        if (messageHistory.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messageHistory.size - 1)
            } catch (e: Exception) {
                listState.scrollToItem(messageHistory.size - 1)
            }
        }
    }

    LaunchedEffect(Unit) {
        messageHistory += Message("Hello! How can I help you?", ai = true)
    }

    val messageSuggested = remember {
        mutableStateListOf(
            SuggestedMessage("What should I eat before exercising?", "I can't offer nutrition advice right now, but a good general tip is to have a light snack with carbs and protein 30-60 minutes before your workout."),
            SuggestedMessage("How do I stay motivated?", "Finding motivation can be tough! While I can't give you a personalized plan right now, common strategies include setting small, achievable goals and finding a workout buddy."),
            SuggestedMessage("What is the best pizza topping?", "While I'm not a food critic, many people enjoy pepperoni. Maybe try asking a chef!")
        )
    }

    DisposableEffect(Unit) {
        onDispose { messageHistory.clear() }
    }

    val maxTurns: Int = 24 // Can be adjusted based on how limited our token size is

    // Map UI bubbles to chat history (no preamble here)
    fun mapUiHistoryToAiMessages(): List<AiMessage> {
        val turns = messageHistory.map { m ->
            AiMessage(
                aiModel = if (m.ai) "model" else "user",
                message = m.text
            )
        }
        return turns.takeLast(maxTurns)
    }

    fun send(text: String, fallbackMessage: String? = null) {
        if (text.isBlank()) return

        val prior = mapUiHistoryToAiMessages()
        messageHistory += Message(text, ai = false)

        scope.launch {
            val reply = try {
                session.sendPrompt(
                    prompt = text,
                    priorTurns = prior,
                    workoutGoal = workoutGoal,
                    dietaryGoal = dietaryGoal,
                    activityLevel = activityLevel,
                    fitnessLevel = fitnessLevel,
                    workoutLength = workoutLength,
                    workoutRestrictions = workoutRestrictions,
                    healthData = healthData,
                    dailyProgressData = dailyProgressData
                )
            } catch (e: Exception) {
                fallbackMessage ?: "Sorry, I couldn't reach the coach right now. Please try again in a moment."
            }
            messageHistory += Message(reply, ai = true)
        }

    }

    // body
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(bottom = 8.dp)
    ) {
        // back button bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Back",
                    tint = Silver
                )
            }
        }

        // message history list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(messageHistory, key = { it.hashCode() }) { message ->
                AnimatedMessageBubble(
                    message = message,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        // message suggestion list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messageSuggested, key = { it.text }) { suggestion ->
                SimpleSuggestionChip(
                    text = suggestion.text,
                    onClick = {
                        if (messageHistory.none { it.text == suggestion.text }) {
                            send(suggestion.text, suggestion.fallback)
                        }
                    }
                )
            }
        }

        // message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                // add image logic
                scope.launch {
                    val bytes = onPickImageBytes() ?: return@launch
                    messageHistory += Message("📷 Image attached", ai = false)
                    mapUiHistoryToAiMessages()
                    val reply = try {
                        api.visionReplyWithContext(
                            userMsg = messageText.text,
                            imageData = bytes,
                            workoutGoal = workoutGoal,
                            dietaryGoal = dietaryGoal,
                            activityLevel = activityLevel,
                            fitnessLevel = fitnessLevel,
                            workoutLength = workoutLength,
                            workoutRestrictions = workoutRestrictions,
                            healthData = healthData,
                            dailyProgressData = dailyProgressData
                        )
                    } catch (_: Exception) {
                        "Sorry, I couldn't analyze that image right now."
                    }
                    messageHistory += Message(reply, ai = true)
                    messageText = TextFieldValue("")
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Image",
                    tint = Silver
                )
            }

            BasicTextField(
                value = messageText.text,
                onValueChange = { messageText = TextFieldValue(it) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                decorationBox = { placeHolder ->
                    if (messageText.text.isEmpty()) {
                        Text(
                            text = "Message...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    placeHolder()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.text.isNotBlank()) {
                        send(messageText.text)
                        messageText = TextFieldValue("")
                    }
                },
                enabled = messageText.text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (messageText.text.isNotBlank()) Orange else Silver
                )
            }
        }
    }
}

@Composable
fun AnimatedMessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    // Trigger the animation when the composable enters composition
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val enterAnimation = remember {
        fadeIn(
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        ) + slideInHorizontally(
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            initialOffsetX = { if (message.ai) -300 else 300 }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = enterAnimation,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.ai) Arrangement.Start else Arrangement.End
        ) {
            Column {
                // Simple sender label for AI only
                if (message.ai) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TagFaces,
                            contentDescription = "AI",
                            tint = Silver,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Coach Rise",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Silver
                        )
                    }
                }

                // Message bubble with subtle styling
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (message.ai) DeepBlue else Orange
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .widthIn(max = 280.dp)
                ) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = White,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleSuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isClicked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isClicked) Orange else Silver,
                shape = RoundedCornerShape(16.dp)
            )
            .background(Transparent)
            .clickable {
                if (!isClicked) {
                    isClicked = true
                    onClick()
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isClicked) Orange else Silver,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

data class Message(val text: String, val ai: Boolean)
data class SuggestedMessage(val text: String, val fallback: String)