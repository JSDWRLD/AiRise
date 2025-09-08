package com.teamnotfound.airise.friends.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamnotfound.airise.friends.models.FriendsViewModel

/**
 * Friends list screen concept (not the activity feed).
 * Uses FriendsViewModel (network-backed) to load/add/remove friends.
 */
@Composable
fun FriendsScreen(vm: FriendsViewModel) {

    val state by vm.ui.collectAsState(initial = FriendsViewModel.UiState())

    LaunchedEffect(Unit) { vm.load() }

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(state.error!!, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
            Button(onClick = vm::refresh) { Text("Retry") }
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            items(state.friends, key = { it.firebaseUid }) { f ->
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add an AsyncImage here
                    Column(Modifier.weight(1f)) {
                        Text(f.displayName, fontWeight = FontWeight.Bold)
                        Text("🔥 Streak: ${f.streak}")
                    }
                    OutlinedButton(onClick = { vm.removeFriend(f.firebaseUid) }) { Text("Remove") }
                }
                Divider()
            }
        }
    }
}
