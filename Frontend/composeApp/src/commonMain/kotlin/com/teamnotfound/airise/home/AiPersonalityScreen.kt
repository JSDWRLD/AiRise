package com.teamnotfound.airise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserOnboarding

@Composable
fun AiPersonalityScreen(user: UserOnboarding, navController: NavController){
    // temp until placed in UserOnboarding
    var personalityOption by remember { mutableStateOf<String?>(null) }
    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF062022))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // title
            Text(
                text = "Customize Ai Personality",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // list
            listOf("Strong & Energetic", "Tough & No-Nonsense", "Supportive & Compassionate", "Science-Based & Analytical").forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { personalityOption = option }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = personalityOption == option,
                        onClick = { personalityOption = option },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFFA500),
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = option, color = Color.White)
                }
            }
        }
        // continue button
        Button(
            onClick = { navController.popBackStack() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            enabled = personalityOption != null,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF21565C))
        ) {
            Text("Continue", fontSize = 18.sp, color = Color.White)
        }
    }
}
