package com.teamnotfound.airise.friends.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.teamnotfound.airise.data.auth.IAuthService
import com.teamnotfound.airise.data.DTOs.UserProfile
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.friends.models.FriendsViewModel
import com.teamnotfound.airise.friends.repos.FriendsNetworkRepository
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.White
import com.teamnotfound.airise.util.Silver

/**
 * Friends list screen concept (not the activity feed).
 * Uses FriendsViewModel (network-backed) to load/add/remove friends.
 */
@Composable
fun FriendsListScreen(
    authService: IAuthService,
    friendsRepository: FriendsNetworkRepository,
    userRepository: UserRepository,
    navController: NavHostController
) {
    val friendsViewModel = remember { FriendsViewModel(authService, friendsRepository, userRepository) }
    val state by friendsViewModel.ui.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var testFriendUid by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        friendsViewModel.load()
    }

    Scaffold(
        backgroundColor = BgBlack,
        topBar = {
            TopAppBar(
                title = { Text("Friends Test Screen", color = White) },
                backgroundColor = BgBlack,
                contentColor = White,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Section
            Text("Search Friends", color = White, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by UID or Name", color = Silver) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = White,
                    cursorColor = White,
                    focusedBorderColor = White,
                    unfocusedBorderColor = Silver
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = White)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add Friend Test Section
            Text("Add Friend Test", color = White, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = testFriendUid,
                onValueChange = { testFriendUid = it },
                label = { Text("Friend UID to Add", color = Silver) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = White,
                    cursorColor = White,
                    focusedBorderColor = White,
                    unfocusedBorderColor = Silver
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (testFriendUid.isNotBlank()) {
                        friendsViewModel.addFriend(testFriendUid)
                        testFriendUid = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Test Friend")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Friends List Section
            Text("Current Friends List", color = White, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            // Filter friends based on search query
            val filteredFriends = state.friends.filter { friend ->
                friend.displayName.contains(searchQuery, ignoreCase = true) ||
                        friend.firebaseUid.contains(searchQuery, ignoreCase = true)
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = White)
                    }
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}", color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { friendsViewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
                filteredFriends.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isNotBlank()) "No friends match your search"
                            else "No friends yet",
                            color = Silver
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFriends, key = { it.firebaseUid }) { friend ->
                            FriendListItem(
                                friend = friend,
                                onRemove = { friendsViewModel.removeFriend(friend.firebaseUid) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh Button
            Button(
                onClick = { friendsViewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Friends List")
            }
        }
    }
}

@Composable
fun FriendListItem(friend: UserProfile, onRemove: () -> Unit) {
    Card(
        backgroundColor = BgBlack,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, White, MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.displayName, color = White, fontWeight = FontWeight.Bold)
                Text("UID: ${friend.firebaseUid}", color = Silver, style = MaterialTheme.typography.caption)
                Text("Streak: ${friend.streak} days", color = Silver)
            }

            Button(
                onClick = onRemove,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Remove", color = White)
            }
        }
    }
}