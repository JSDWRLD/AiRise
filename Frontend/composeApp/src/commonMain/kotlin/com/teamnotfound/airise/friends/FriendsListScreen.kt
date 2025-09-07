package com.teamnotfound.airise.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.communityNavBar.CommunityNavBar
import com.teamnotfound.airise.communityNavBar.UserProfile
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

//screen of activity feed
//currently uses viewmodel state and bottom nav bar created previously
@Composable
fun FriendsListScreen(viewModel: FriendsListViewModel) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = { BottomNavigationBar(navController = bottomNavController) } // bottom nav bar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(innerPadding)                // space for bottom bar
                .padding(horizontal = 12.dp)
        ) {
            //
            CommunityNavBar()
            when {
                state.isLoading -> FeedLoading()
                state.error != null -> FeedError(message = state.error!!, onRetry = { viewModel.refresh() })
                state.items.isEmpty() -> FeedEmpty(onRefresh = { viewModel.refresh() })
                else -> ActivityFeedList(items = state.items)
            }
        }
    }
}

//list of friends activity info card
@Composable
private fun ActivityFeedList(items: List<FriendActivity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item -> FeedCard(item) }
    }
}

//one friend activity card which contains avatar, name, challenge, and status
@Composable
private fun FeedCard(item: FriendActivity) {
    Card(
        backgroundColor = BgBlack,
        contentColor = White,
        elevation = 0.dp,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, White, RoundedCornerShape(18.dp))
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //place holder for profile/avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Silver)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                //displays name, challenge, and status
                Text(
                    text = "${item.friendName} completed challenge: ${item.challengeTitle}",
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(Modifier.height(4.dp))
                Text(text = item.status, color = White)

                Spacer(Modifier.height(4.dp))
                Text("Had an awesome workout!", color = Silver)

            }
        }
    }
}

//loading state for activity feed
@Composable
private fun FeedLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = White)
    }
}

//error state for feed
@Composable
private fun FeedError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Couldn’t load feed.", color = Color.Red)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

//empty state when there are no act.
@Composable
private fun FeedEmpty(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No recent activity.", color = Silver)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRefresh) {
            Text("Refresh")
        }
    }
}
