package com.teamnotfound.airise.challenges

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
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.navigationBar.BottomNavigationBar
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
    onAddClick: () -> Unit, //call to go to create challenge
    onEditClick: () -> Unit //call to go to details of challenge
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = BgBlack,
        bottomBar = { BottomNavigationBar(navController = bottomNavController) },
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
                .padding(top = 300.dp)       // reserve space for the top header
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
                            onClick = {
                                //notifies vm of selection and nav to details
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
    name: String,                 // unused for now
    description: String,          // unused for now
    label: String,                // challenge label
    onClick: () -> Unit           // nav to details
) {
    val shape = RoundedCornerShape(18.dp)

    Card(
        backgroundColor = BgBlack,
        contentColor = White,
        elevation = 0.dp,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(2.dp, White, shape)
            .clickable(onClick = onClick)
    ) {
        //label placement
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}