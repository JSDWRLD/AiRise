package com.teamnotfound.airise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    // height ranges
    val heightRange = if (newUser.heightMetric.value) {
        (140..210 step 5)
    } else {
        (50..80 step 1)
    }
    //
    val showDialog = remember { mutableStateOf(false) }
    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.Center
            ) {
                // inches
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.heightMetric.value = false
                            newUser.heightValue.value = 0
                        }
                        .background(if (!newUser.heightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IN",
                        color = if (!newUser.heightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp
                    )
                }
                // centimeters
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.heightMetric.value = true
                            newUser.heightValue.value = 0
                        }
                        .background(if (newUser.heightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CM",
                        color = if (newUser.heightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp
                    )
                }
            }
            // height scroll
            ScrollableColumnSelection(
                label = null,
                items = heightRange.toList(),
                selectedItem = newUser.heightValue.value,
                onItemSelected = { newUser.heightValue.value = it }
            )
        }
        // continue button
        Button(
            onClick = { showDialog.value = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            enabled = newUser.heightValue.value != 0,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF21565C))
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
