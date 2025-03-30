package com.teamnotfound.airise.navigationBar



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamnotfound.airise.navigationBar.Screen

@Composable
fun TopNavBar(navController: NavController) {
    TopAppBar(
        backgroundColor = Color(0xFF0D1F20), // Dark background color
        contentColor = Color.White,
        elevation = 0.dp,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder circle avatar
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFF8B9D9E) // Grayish color for the placeholder
                ) {
                    // If you want to add initials, you can add text here
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Greeting text
                Column {
                    Text(
                        text = "Good morning,",
                        style = MaterialTheme.typography.body2
                    )
                    Text(
                        text = "[User]",
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        },
        actions = {
            // Notifications icon
            IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.LightGray
                )
            }

            // Account icon
            IconButton(onClick = { navController.navigate(Screen.Account.route) }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account",
                    tint = Color.LightGray
                )
            }
        }
    )
}
/*
package com.teamnotfound.airise.navigationBar


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.teamnotfound.airise.navigationBar.Screen

@Composable
fun TopNavBar(navController: NavController) {
    TopAppBar(
        title = { Text(text = "Good Morning, User") },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        actions = {
            IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = { navController.navigate(Screen.Account.route) }) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Account")
            }
        }
    )
}


 */