package com.teamnotfound.airise.auth.email

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.auth.general.AuthCard
import com.teamnotfound.airise.auth.general.AuthHeader
import com.teamnotfound.airise.auth.general.PrimaryButton
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    viewModel: EmailVerificationViewModel,
    onVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var hasSentVerification by remember { mutableStateOf(false) }
    val firebaseUser = Firebase.auth.currentUser ?: return

    LaunchedEffect(firebaseUser) {
        if (!hasSentVerification) {
            viewModel.sendEmailVerification(firebaseUser)
            hasSentVerification = true
        }
        // Poll every 3s up to ~5 minutes, then rely on manual navigation.
        repeat(300 / 3) {
            delay(3000)
            viewModel.checkEmailVerified(firebaseUser)
            if (viewModel.isVerified.value) {
                onVerified()
                return@LaunchedEffect
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(Modifier.fillMaxSize()) {
            AuthHeader(
                title = "Verify your email",
                subtitle = "We just sent a verification link.",
                onBackClick = onBackToLogin
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
                        text = "A verification email has been sent to:\n${firebaseUser.email}",
                        color = Silver,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    /* TODO: Fix firebase error
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage.orEmpty(),
                            color = Orange,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Orange)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Waiting for email verification...",
                                color = White,
                                fontSize = 14.sp
                            )
                        }
                    }
                     */

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Orange)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Waiting for email verification...",
                            color = White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Back to Login",
                        onClick = onBackToLogin
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
