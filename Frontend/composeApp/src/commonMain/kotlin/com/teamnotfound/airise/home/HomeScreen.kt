package com.teamnotfound.airise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(email: String, onAccountSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF062022)),
        contentAlignment = Alignment.Center
    ) {
        val username = email.substringBefore("@")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome $username!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You have successfully signed in.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onAccountSettingsClick) {
                Text(text = "Go to Account Settings")
            }
        }
    }
}
