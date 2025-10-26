package com.teamnotfound.airise.community.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.teamnotfound.airise.auth.admin.AdminVerifyViewModel
import com.teamnotfound.airise.auth.admin.AdminVerifyDialog
import com.teamnotfound.airise.auth.admin.AdminVerifyUiState
import com.teamnotfound.airise.community.challenges.challengeEditor.ChallengeEditorDialog
import com.teamnotfound.airise.community.challenges.challengeEditor.ChallengeEditorUiEvent
import com.teamnotfound.airise.community.challenges.challengeEditor.ChallengeEditorViewModel


@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModelImpl,
    adminVerifyViewModel: AdminVerifyViewModel,
    editorViewModel: ChallengeEditorViewModel,
    navController: NavHostController,
    communityNavBarViewModel: CommunityNavBarViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val editorState by editorViewModel.uiState.collectAsState()
    val adminState by adminVerifyViewModel.uiState.collectAsState()
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
            Row(
                modifier = Modifier
                    .padding(end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(adminState.isAdmin){
                    // Add Challenge FAB (Admin only)
                    ExtendedFloatingActionButton(
                        text = { Text("Add Challenge") },
                        onClick = {
                            if (adminState.isVerified) {
                                editorViewModel.onEvent(ChallengeEditorUiEvent.OpenEditor(
                                    ChallengeUI()
                                ))
                            } else {
                                // Not verified, show password dialog
                                adminVerifyViewModel.showPasswordPrompt()
                            }
                        },
                        backgroundColor = Orange,
                        contentColor = BgBlack
                    )
                }

                // Existing circular FAB (chat or add action)
                FloatingActionButton(
                    onClick = { /* Existing action */ },
                    backgroundColor = DeepBlue,
                    contentColor = White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
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
                                challengeUI = c,
                                label = if (c.name.value.isNotBlank()) c.name.value else "Challenge ${c.id}",
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
                                },
                                adminState = adminState,
                                adminVerifyViewModel = adminVerifyViewModel,
                                editorViewModel = editorViewModel
                            )
                        }
                    }
                    if (adminState.showAdminPasswordPrompt) {
                        AdminVerifyDialog(
                            onDismiss = { adminVerifyViewModel.dismissPasswordPrompt() },
                            onEvent = { adminVerifyViewModel.onEvent(it) },
                            uiState = adminState
                        )
                    }
                    if (editorState.isEditing) {
                        ChallengeEditorDialog(
                            viewModel = editorViewModel,
                            onAuthorizationError = {
                                adminVerifyViewModel.resetVerification()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challengeUI: ChallengeUI,
    label: String,
    isActive: Boolean,
    completedToday: Boolean,
    completedAnyToday: Boolean,
    hasActive: Boolean,
    onStartToday: () -> Unit,
    onCompleteToday: () -> Unit,
    adminState: AdminVerifyUiState,
    adminVerifyViewModel : AdminVerifyViewModel,
    editorViewModel: ChallengeEditorViewModel
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
                        model = challengeUI.imageUrl.value,
                        contentDescription = challengeUI.name.value.ifBlank { label },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if(adminState.isAdmin){
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
                        ) {
                            IconButton(
                                onClick = {
                                    if (adminState.isVerified) {
                                        editorViewModel.delete(challengeUI.id)
                                    } else {
                                        // Not verified, show password dialog
                                        adminVerifyViewModel.showPasswordPrompt()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .background(color = DeepBlue, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Challenge",
                                    tint = White,
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (adminState.isVerified) {
                                        editorViewModel.onEvent(
                                            ChallengeEditorUiEvent.OpenEditor(
                                                challengeUI
                                            )
                                        )
                                    } else {
                                        // Not verified, show password dialog
                                        adminVerifyViewModel.showPasswordPrompt()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .background(color = DeepBlue, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Challenge",
                                    tint = White,
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                        }
                    }

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

                if (challengeUI.description.value.isNotBlank()) {
                    Text(
                        text = challengeUI.description.value,
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
