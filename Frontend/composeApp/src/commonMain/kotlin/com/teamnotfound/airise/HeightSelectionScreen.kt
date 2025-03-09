package com.teamnotfound.airise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Page to select user height
 */
@Composable
fun HeightSelectionScreen(newUser: UserProfile) {
    val heightRange = if (newUser.heightMetric.value) (140..210 step 5) else (50..80 step 1)
    val showDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // title
        Text(
            text = "What Is Your Height?",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp)
        )

        // metric select
        Row(
            modifier = Modifier.padding(16.dp).background(Color.Gray, RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .clickable { newUser.heightMetric.value = false }
                    .background(if (!newUser.heightMetric.value) Color.White else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IN",
                    color = if (!newUser.heightMetric.value) Color.Black else Color.White,
                    fontSize = 18.sp
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .clickable { newUser.heightMetric.value = true }
                    .background(if (newUser.heightMetric.value) Color.White else Color.Transparent),
                contentAlignment = Alignment.Center // Center the content inside the box
            ) {
                Text(
                    text = "CM",
                    color = if (newUser.heightMetric.value) Color.Black else Color.White,
                    fontSize = 18.sp
                )
            }
        }
        
        // height scroll
        Box(modifier = Modifier.height(200.dp).padding(16.dp)) {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(heightRange.toList()) { height ->
                    Text(
                        text = height.toString(),
                        fontSize = if (height == newUser.heightValue.value) 32.sp else 24.sp,
                        fontWeight = if (height == newUser.heightValue.value) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White,
                        modifier = Modifier.padding(4.dp).clickable { newUser.heightValue.value = height }
                    )
                }
            }
        }

        // continue
        Button(
            onClick = { showDialog.value = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.8f),
            enabled = newUser.heightValue.value != 0
        ) {
            Text("Continue", fontSize = 18.sp, color = Color.White)
        }
    }

    // temp display to show values actually saves to newUser
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Your Height Selection") },
            text = {
                Text("Height: ${newUser.heightValue.value} ${if (newUser.heightMetric.value) "CM" else "IN"}")
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
}
