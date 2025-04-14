package com.teamnotfound.airise.home

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
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.util.BgBlack

@Composable
fun TopNavBar(username: String, navController: NavController, isLoading: Boolean) {
    TopAppBar(
        modifier = Modifier.padding(top = 25.dp),
        backgroundColor = BgBlack, // Dark background color
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
                    if(isLoading){
                        CircularProgressIndicator()
                    }else {
                        Text(
                            text = "Good morning,",
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = username,
                            style = MaterialTheme.typography.h6
                        )
                    }
                }
            }
        },
        actions = {
            // Notifications icon
            IconButton(onClick = { }) { // TODO: make notifications screen to show curr notifs
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.LightGray
                )
            }

            // Account icon
            IconButton(onClick = { navController.navigate(AppScreen.ACCOUNT_SETTINGS.name) }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account",
                    tint = Color.LightGray
                )
            }
        }
    )
}