package com.teamnotfound.airise.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.navigationBar.TopNavBar
import com.teamnotfound.airise.navigationBar.BottomNavigationBar


@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = Color(0xFF0D1F20),
        topBar = {
            TopNavBar(navController = navController)
        },
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Edit Notifications header with back and check buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1F20))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color(0xFFFF7043) // Orange color
                    )
                }

                Text(
                    text = "Edit Notifications",
                    style = MaterialTheme.typography.h6,
                    color = Color.White
                )

                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color(0xFFFF7043) // Orange color
                    )
                }
            }

            // Notification toggle items
            NotificationToggleItem("Challenge Notifications")
            Divider(color = Color(0xFF1E3A3A), thickness = 1.dp)

            NotificationToggleItem("Friend Request Notifications")
            Divider(color = Color(0xFF1E3A3A), thickness = 1.dp)

            NotificationToggleItem("Streak Notifications")
            Divider(color = Color(0xFF1E3A3A), thickness = 1.dp)

            NotificationToggleItem("Mealtime Notifications")
        }
    }
}

@Composable
fun NotificationToggleItem(title: String) {
    var isEnabled by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            color = Color.White
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = { isEnabled = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF8B9D9E),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF505A5B)
            )
        )
    }
}