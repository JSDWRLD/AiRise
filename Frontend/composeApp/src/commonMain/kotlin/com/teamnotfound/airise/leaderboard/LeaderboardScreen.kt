package com.teamnotfound.airise.leaderboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBar
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBarViewModel
import com.teamnotfound.airise.community.communityNavBar.CommunityPage
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack

@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    communityNavBarViewModel: CommunityNavBarViewModel,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        topBar = { CommunityNavBar(navController = navController, currentPage = CommunityPage.Leaderboard, communityNavBarViewModel) },
        // bottom nav bar
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
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .padding(innerPadding)
        ) {
            // Leaderboard content
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
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = Color(0xFF2E2E2E)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Tab Row
            LeaderboardTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            
            // Error state
            else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }
            
            // Leaderboard list
            else {
                val users = when (uiState.selectedTab) {
                    LeaderboardTab.GLOBAL -> uiState.globalUsers
                    LeaderboardTab.FRIENDS -> uiState.friendsUsers
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
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
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LeaderboardTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val tabName = when (tab) {
                LeaderboardTab.GLOBAL -> "Global"
                LeaderboardTab.FRIENDS -> "Friends"
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                TextButton(
                    onClick = { onTabSelected(tab) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) Color.White else Color.Gray
                    )
                ) {
                    Text(
                        text = tabName,
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                // Underline for selected tab
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.White)
                    )
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
    Card(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF3E3E3E),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
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
                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    // User name
                    Text(
                        text = user.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.streak.toString(),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Rank
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF4CAF50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "#${user.rank}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}