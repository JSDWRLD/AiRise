package com.teamnotfound.airise.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import com.teamnotfound.airise.util.BgBlack

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        // Back Arrow
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFFA500)
                )
            }
        }

        // Center the screen
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .padding(24.dp)
        ) {

            Text("AiRise Terms of Use", fontSize = 24.sp, color = Color.White)

            Spacer(modifier = Modifier.height(12.dp))

            // Terms of use test
            Text(
                "Effective as of Match 24, 2025\n" +
                        "1. Introduction\n" +
                        "2. The AiRise Service\n" +
                        "3. Your Use of the AiRise Service\n" +
                        "4. Content and Intellectual Property Rights\n" +
                        "5. Customer Support Information, Questions and Complaints\n" +
                        "6. Problems and Disputes\n" +
                        "7. About these terms\n",
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                lineHeight = 18.sp
            )
        }
    }
}