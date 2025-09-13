package com.teamnotfound.airise.community.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBar
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBarViewModel
import com.teamnotfound.airise.community.communityNavBar.CommunityPage
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.*

@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    communityNavBarViewModel: CommunityNavBarViewModel,
    viewModel: LeaderboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        topBar = {
            CommunityNavBar(
                navController = navController,
                currentPage = CommunityPage.Leaderboard,
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
        }
    ) { innerPadding ->
        // IMPORTANT: Put the BgBlack background OUTSIDE the innerPadding,
        // so the area behind the bottom bar is also dark.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            LeaderboardContent(
                uiState = uiState,
                onTabSelected = viewModel::onTabSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardUiState,
    onTabSelected: (LeaderboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LeaderboardTabs(
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = White)
            }
            uiState.error != null -> Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Failed to load leaderboard", color = White)
                Spacer(Modifier.height(6.dp))
                Text(uiState.error!!, color = Silver)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { /* trigger refresh if you expose it */ }) {
                    Text("Retry", color = White)
                }
            }
            else -> {
                val users = when (uiState.selectedTab) {
                    LeaderboardTab.GLOBAL -> uiState.globalUsers
                    LeaderboardTab.FRIENDS -> uiState.friendsUsers
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(users, key = { it.name }) { user ->
                        LeaderboardUserItem(user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardTabs(
    selectedTab: LeaderboardTab,
    onTabSelected: (LeaderboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(top = 8.dp, bottom = 6.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LeaderboardTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val label = when (tab) {
                LeaderboardTab.GLOBAL -> "Global"
                LeaderboardTab.FRIENDS -> "Friends"
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Subtle cyan halo behind the selected tab (matches Challenges glow vibe)
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .fillMaxWidth()
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Cyan.copy(alpha = 0.35f), Transparent)
                                    )
                                )
                                .blur(18.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        )
                    }
                    OutlinedButton(
                        onClick = { onTabSelected(tab) },
                        border = ButtonDefaults.outlinedBorder.copy(
                            width = if (isSelected) 2.dp else 1.dp
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = BgBlack,
                            contentColor = if (isSelected) White else Silver
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
                // Orange underline for selected tab (like your card borders)
                if (isSelected) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth(0.6f)
                            .background(Orange)
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LeaderboardUserItem(
    user: LeaderboardUser,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp) // so glow peeks out evenly (same trick as Challenges)
    ) {
        // Cyan glow halo (matches ChallengeCard)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.35f), Transparent)
                    )
                )
                .blur(28.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Card(
            backgroundColor = BgBlack,
            contentColor = White,
            elevation = 0.dp,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .border(2.dp, Orange.copy(alpha = 0.6f), shape)
                .clip(shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, DeepBlue, CircleShape)
                            .padding(2.dp) // keeps the inner avatar from covering the border
                    ) {
                        val initials = remember(user.name) { initialsFrom(user.name) }
                        AvatarWithFallback(
                            imageUrl = user.imageUrl,
                            initials = initials,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = user.name,
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Orange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${user.streak} day streak",
                                color = Silver,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Rank pill (DeepBlue to match accent)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepBlue)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "#${user.rank}",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun initialsFrom(fullName: String): String {
    val parts = fullName
        .split(" ")
        .filter { it.isNotBlank() }
    return parts
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "U" }
}

@Composable
private fun AvatarWithFallback(
    imageUrl: String?,
    initials: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(BgBlack)
    ) {
        // cyan glow ring (subtle)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.35f), Transparent)
                    )
                )
        )

        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Silver.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}