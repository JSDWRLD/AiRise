package com.teamnotfound.airise.community.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBar
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBarViewModel
import com.teamnotfound.airise.community.communityNavBar.CommunityPage
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.Transparent
import com.teamnotfound.airise.util.White
import com.teamnotfound.airise.AppScreen
import kotlinx.coroutines.flow.collect

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModelImpl,
    navController: NavHostController,
    communityNavBarViewModel: CommunityNavBarViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val bottomNavController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.events.collect { evt ->
            when (evt) {
                is ChallengesEvent.CompletedToday -> communityNavBarViewModel.refresh()
                is ChallengesEvent.Error -> {}
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndAutoResetOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        backgroundColor = BgBlack,
        topBar = {
            CommunityNavBar(
                navController = navController,
                currentPage = CommunityPage.Challenges,
                viewModel = communityNavBarViewModel
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }, backgroundColor = DeepBlue, contentColor = White) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = White)
                }

                state.error != null -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Failed to load challenges", color = White)
                    Spacer(Modifier.height(6.dp))
                    Text(state.error!!, color = Silver)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                }

                state.items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No challenges yet.", color = Silver)
                }

                else -> {
                    val completedAnyToday = state.progress.completedToday
                    val activeId = state.progress.activeChallengeId
                    val hasActive = activeId != null

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.items, key = { it.id }) { c ->
                            val isActive = activeId == c.id
                            ChallengeCard(
                                name = c.name,
                                description = c.description,
                                label = if (c.name.isNotBlank()) c.name else "Challenge ${c.id}",
                                imageUrl = c.imageUrl,
                                isActive = isActive,
                                completedToday = completedAnyToday && isActive,
                                completedAnyToday = completedAnyToday,
                                hasActive = hasActive,
                                onStartToday = {
                                    if (!hasActive && !completedAnyToday) {
                                        viewModel.enrollInChallenge(c.id)
                                    }
                                },
                                onCompleteToday = {
                                    if (isActive && !completedAnyToday) {
                                        viewModel.completeToday()
                                    }
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
private fun ChallengeCard(
    name: String,
    description: String,
    label: String,
    imageUrl: String?,
    isActive: Boolean,
    completedToday: Boolean,
    completedAnyToday: Boolean,
    hasActive: Boolean,
    onStartToday: () -> Unit,
    onCompleteToday: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        // Glow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.30f), Transparent)
                    )
                )
                .blur(radius = 28.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Card(
            backgroundColor = BgBlack,
            contentColor = White,
            elevation = 0.dp,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Silver.copy(alpha = 0.35f), shape)
                .clip(shape)
        ) {
            Column {
                // Header image + label
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .background(BgBlack)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = if (name.isNotBlank()) name else label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    0f to Transparent,
                                    0.6f to BgBlack.copy(alpha = 0.2f),
                                    1f to BgBlack.copy(alpha = 0.75f)
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    }
                }

                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        color = Silver,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                } else {
                    Spacer(Modifier.height(10.dp))
                }

                // CTA area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        completedAnyToday -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Silver,
                                    contentColor = BgBlack
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Come back tomorrow")
                            }
                        }
                        isActive -> {
                            Button(
                                onClick = onCompleteToday,
                                enabled = !completedToday,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (!completedToday) Orange else Silver,
                                    contentColor = BgBlack
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (completedToday) "Done today" else "Complete today")
                            }
                        }
                        hasActive -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Silver,
                                    contentColor = BgBlack
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Another challenge in progress")
                            }
                        }
                        else -> {
                            Button(
                                onClick = onStartToday,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = DeepBlue,
                                    contentColor = White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Start today")
                            }
                        }
                    }
                }
            }
        }
    }
}
