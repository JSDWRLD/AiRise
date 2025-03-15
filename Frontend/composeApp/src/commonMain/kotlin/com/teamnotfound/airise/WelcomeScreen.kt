package com.teamnotfound.airise

import airise.composeapp.generated.resources.Res
import airise.composeapp.generated.resources.compose_multiplatform
import airise.composeapp.generated.resources.welcome_screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun WelcomeScreen(){
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val circleSize = maxWidth * 0.4f //Circle size is 40% of screen width
        val padding = 16.dp
        Image(
            painter = painterResource(Res.drawable.welcome_screen),
            contentDescription = "Welcome screen image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome To",
                color = Orange,
                modifier = Modifier.padding(bottom = padding),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier.size(circleSize)
                    .background(White, CircleShape)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = padding)
            ) {
                Text(
                    text = "Ai",
                    color = Orange,
                    fontSize = 55.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Rise",
                    color = Orange,
                    fontSize = 55.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        val buttonWidth = maxWidth * 0.9f
        val buttonHeight = maxHeight * 0.06f
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(
                onClick = {/* on click go to create account page */},
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(Orange)
            ){
                Text(
                    text = "Start",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = {/* on click go to login page*/},
                colors = ButtonDefaults.buttonColors(Transparent),
            ){
                Text(
                    text = "Already have an account?",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}