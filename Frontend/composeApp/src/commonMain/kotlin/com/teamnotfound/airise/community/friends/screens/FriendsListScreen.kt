package com.teamnotfound.airise.community.friends.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBar
import com.teamnotfound.airise.community.communityNavBar.CommunityNavBarViewModel
import com.teamnotfound.airise.community.communityNavBar.CommunityPage
import com.teamnotfound.airise.data.DTOs.UserProfile
import com.teamnotfound.airise.community.friends.models.FriendsViewModel
import com.teamnotfound.airise.data.DTOs.toDomain
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.White
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.Transparent

/**
 * Friends list screen concept (not the activity feed).
 * Uses FriendsViewModel (network-backed) to load/add/remove friends.
 */
@Composable
fun FriendsListScreen(
    navController: NavHostController,
    communityNavBarViewModel: CommunityNavBarViewModel,
    viewModel: FriendsViewModel
) {
    val uiState by viewModel.ui.collectAsState()
    val bottomNavController = rememberNavController()

    var searchQuery by remember { mutableStateOf("") }
    var testFriendUid by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            UserSearchField(
                searchQuery = searchQuery,
                onQueryChanged = {
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                searchResults = uiState.search.results,
                onUserSelected = {viewModel.addFriend(it.firebaseUid)},
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Friends List Section
            Text("Friends", color = White, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            // Filter friends based on search query
            val filteredFriends = uiState.friends.filter { friend ->
                friend.displayName.contains(searchQuery, ignoreCase = true) ||
                        friend.firebaseUid.contains(searchQuery, ignoreCase = true)
            }

            FriendListContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh Button
            Button(
                onClick = { viewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Friends List")
            }
        }
    }
}

@Composable
private fun FriendListContent(
    uiState: FriendsListUiState,
    viewModel: FriendsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = White)
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Failed to load friend list: ${uiState.error}", color = Silver)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.friends, key = { it.displayName }) { friend ->
                        FriendListItem(
                            friend = friend,
                            onRemove = {viewModel.removeFriend(friend.firebaseUid)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendListItem(
    friend: UserProfile,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
)  {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp)// so glow peeks out evenly (same trick as Challenges)

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
                        val initials = remember(friend.displayName) { initialsFrom(friend.displayName) }
                        AvatarWithFallback(
                            imageUrl = friend.profilePicUrl,
                            initials = initials,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = friend.displayName,
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
                                text = "${friend.streak} day streak",
                                color = Silver,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Button(
                    onClick = onRemove,
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Silver)
                ) {
                    Text("Remove", color = White)
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

@Composable
fun UserSearchField(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    searchResults: List<UserProfile>,
    onUserSelected: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChanged, // just update the text/trigger search
            label = { Text("Search by Name", color = Silver) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = White,
                cursorColor = White,
                focusedBorderColor = White,
                unfocusedBorderColor = Silver,
                placeholderColor = Silver
            ),
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = White)
            },
            singleLine = true
        )

        // Show suggestion/list BELOW the field when there's text
        if (searchQuery.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgBlack)
                    .border(1.dp, Silver, RoundedCornerShape(8.dp)),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(4.dp).background(color = BgBlack)) {
                    if (searchResults.isEmpty()) {
                        Text(
                            "No users found",
                            color = Silver,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        searchResults.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AvatarWithFallback(
                                    imageUrl = user.profilePicUrl,
                                    initials = initialsFrom(user.displayName),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = user.displayName,
                                    color = White,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        onUserSelected(user)
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                                    shape = RoundedCornerShape(size = 15.dp),
                                ) {
                                    Text(text = "Add", color = White)
                                }
                            }
                            Divider(Modifier.padding(start = 44.dp))
                        }
                    }
                }
            }
        }
    }
}
