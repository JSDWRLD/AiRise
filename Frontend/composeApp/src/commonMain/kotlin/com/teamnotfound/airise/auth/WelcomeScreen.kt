package com.teamnotfound.airise.auth

import airise.composeapp.generated.resources.AiRise_Logo
import airise.composeapp.generated.resources.Res
import airise.composeapp.generated.resources.welcome_account
import airise.composeapp.generated.resources.welcome_screen
import airise.composeapp.generated.resources.welcome_start
import airise.composeapp.generated.resources.welcome_to
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.auth.general.PrimaryButton
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.White
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onAlreadyHaveAnAccountClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        // Background image
        Image(
            painter = painterResource(Res.drawable.welcome_screen),
            contentDescription = "Welcome background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay gradient for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BgBlack.copy(alpha = 0.25f),
                            BgBlack.copy(alpha = 0.45f),
                            BgBlack.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Lifted logo + title
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-70).dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.welcome_to),
                color = Orange,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(2.0f), // logo shape ratio
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.AiRise_Logo),
                    contentDescription = "AiRise Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Bottom buttons (same orange button)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = DeepBlue,
                    contentColor = White
                )
            ) {
                Text(
                    text = stringResource(Res.string.welcome_start),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(14.dp))

            TextButton(onClick = onAlreadyHaveAnAccountClick, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = stringResource(Res.string.welcome_account),
                    color = White,
                    fontSize = 15.sp
                )
            }
        }
    }
}
