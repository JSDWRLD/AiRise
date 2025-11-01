package com.teamnotfound.airise.auth.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.auth.general.AuthCard
import com.teamnotfound.airise.auth.general.AuthHeader
import com.teamnotfound.airise.auth.general.PrimaryButton
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun RecoverySentScreen(
    onBackToLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(Modifier.fillMaxSize()) {
            AuthHeader(
                title = "Recovery Email Sent",
                subtitle = "Follow the link in your inbox to reset your password.",
                onBackClick = onBackClick
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthCard {
                    Text(
                        "We've sent you an email.\nFollow the instructions to access your AiRise account.",
                        color = Silver,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    PrimaryButton(
                        text = "Back to Login",
                        onClick = onBackToLoginClick
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
