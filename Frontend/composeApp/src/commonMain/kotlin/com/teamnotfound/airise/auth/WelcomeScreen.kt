package com.teamnotfound.airise.auth

import airise.composeapp.generated.resources.AiRise_Logo
import airise.composeapp.generated.resources.Res
import airise.composeapp.generated.resources.welcome_account
import airise.composeapp.generated.resources.welcome_screen
import airise.composeapp.generated.resources.welcome_start
import airise.composeapp.generated.resources.welcome_to
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Transparent
import com.teamnotfound.airise.util.White
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onAlreadyHaveAnAccountClick: () -> Unit
){
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val circleSize = maxWidth * 0.4f //Circle size is 40% of screen width
        val logoWidth = maxWidth * 0.5f
        val logoHeight = maxHeight * 0.3f
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

            Box(
                modifier = Modifier
                    .width(logoWidth)
                    .height(logoHeight)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.welcome_to),
                    color = Orange,
                    modifier = Modifier.padding(bottom = padding).align(Alignment.TopCenter),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Image(
                    painter = painterResource(Res.drawable.AiRise_Logo),
                    contentDescription = "AiRise Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
//            Row(
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(top = padding)
//            ) {
//                Text(
//                    text = "Ai",
//                    color = Orange,
//                    fontSize = 55.sp,
//                    fontWeight = FontWeight.ExtraBold
//                )
//                Text(
//                    text = "Rise",
//                    color = Orange,
//                    fontSize = 55.sp,
//                    fontStyle = FontStyle.Italic
//                )
//            }
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
                onClick = {onStartClick()},
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(Orange)
            ){
                Text(
                    text = stringResource(Res.string.welcome_start),
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = {onAlreadyHaveAnAccountClick()},
                colors = ButtonDefaults.buttonColors(Transparent),
            ){
                Text(
                    text = stringResource(Res.string.welcome_account),
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}