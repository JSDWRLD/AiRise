package com.teamnotfound.airise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.teamnotfound.airise.data.serializable.UserOnboarding

@Composable
fun AccountSettingScreen(user: UserOnboarding, navController: NavController) {
    var name by remember { mutableStateOf("${user.firstName.value} ${user.lastName.value}") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // back button
                IconButton(onClick = {navController.popBackStack()}) { // onclick not working
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Settings Icon",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Account Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                    // modifier = Modifier.padding(top = 24.dp)
                )
                // save button
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Settings Icon",
                        tint = Color.White
                    )
                }
            }
            // profile name
            TextField(
                value = name,
                onValueChange = {
                    name = it
                    val splitName = it.split(" ")
                    user.firstName.value = splitName.getOrNull(0) ?: ""
                    user.lastName.value = splitName.getOrNull(1) ?: ""
                },
                label = { Text(
                    text = "Profile Name",
                    color = Color.White
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                singleLine = true,
                textStyle = MaterialTheme.typography.body1.copy(color = Color.White),
            )
            // dob
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.DOBSelect.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Date of Birth",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            // height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.HeightSelect.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Height",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            // weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.WeightSelect.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Weight",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            // connect device
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        connectToHealth()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connect a New Smart Device",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            // ai personality
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.AiPersonality.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize Ai Personality",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
        }
    }
}

fun connectToHealth() {}
