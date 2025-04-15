package com.teamnotfound.airise.home.accountSettings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.*

@Composable
fun AiPersonalityScreen(user: UserData, navController: NavController) {
    var personalityOption by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                backgroundColor = BgBlack,
                contentColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Orange
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Customize Ai Personality",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            listOf("Strong & Energetic", "Tough & No-Nonsense", "Supportive & Compassionate", "Science-Based & Analytical").forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { personalityOption = option }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = personalityOption == option,
                        onClick = { personalityOption = option },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Orange,
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = option, color = Color.White, fontSize = 18.sp)
                }
                Divider(color = DeepBlue, thickness = 1.dp)
            }
        }

        // Continue button
        Button(
            onClick = { navController.popBackStack() },
            enabled = personalityOption != null,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = DeepBlue,
                disabledBackgroundColor = DeepBlue
            ),
            border = BorderStroke(1.dp, Orange),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("Continue", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
