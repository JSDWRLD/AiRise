package com.teamnotfound.airise.community.challenges


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.Icon
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
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



@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
    navController: NavHostController,
    onAddClick: () -> Unit, //call to go to create challenge
    onEditClick: () -> Unit, //call to go to details of challenge
    communityNavBarViewModel: CommunityNavBarViewModel
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        topBar = { CommunityNavBar(navController = navController, currentPage = CommunityPage.Challenges, communityNavBarViewModel) },
        bottomBar = { BottomNavigationBar(
            navController = bottomNavController,
            appNavController = navController,
            onCommunityClick = {
                navController.navigate(AppScreen.CHALLENGES.name) {
                    launchSingleTop = true
                }
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
        ) },
        //floating add button
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick,
                backgroundColor = DeepBlue,
                contentColor = White) {
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
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    //show challenge card
                    items(state.items, key = { it.id }) { c ->
                        ChallengeCard(
                            name = c.name,
                            description = c.description,
                            label = if (c.name.isNotBlank()) c.name else "Challenge ${c.id}",
                            imageUrl = c.imageUrl,
                            isStarted = c.isStarted,
                            //onStart = { viewModel.startChallenge(c.id) },
                            isCompleted = c.isCompleted,
                            //onComplete = { viewModel.onChallengeCompleted(c.id) },
                            onClick = {
                                viewModel.onChallengeClick(c.id)
                                onEditClick()
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
    name: String,
    description: String,
    label: String,
    imageUrl: String?,
    onClick: () -> Unit,
    isStarted: Boolean,
    //onStart: () -> Unit,
    isCompleted: Boolean,
    //onComplete: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp) // so glow peeks out evenly
    ) {
        // Glow layer (cyan halo)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.35f), Transparent)
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
                .height(160.dp)
                .border(2.dp, Orange.copy(alpha = 0.6f), shape)
                .clip(shape)
                .clickable(onClick = onClick)
        ) {
            Box(Modifier.fillMaxSize()) {
                // Challenge image (Coil v3)
                AsyncImage(
                    model = imageUrl,
                    contentDescription = if (name.isNotBlank()) name else label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Label overlay with gradient
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Silver.copy(alpha = 0.9f), Transparent)
                            )
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
            }
        }
    }
}


