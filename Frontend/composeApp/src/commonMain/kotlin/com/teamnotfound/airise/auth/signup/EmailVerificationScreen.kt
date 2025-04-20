package com.teamnotfound.airise.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    viewModel: SignUpViewModel,
    onVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val isVerified by viewModel.isEmailVerified.collectAsState()
    var timer by remember { mutableStateOf(0) }
    val firebaseUser = Firebase.auth.currentUser ?: return

    LaunchedEffect(Unit) {
        viewModel.sendEmailVerification(firebaseUser)

        // Start polling every 3 seconds
        while (!isVerified && timer < 300) { // 300 = ~15 minutes
            delay(3000)
            viewModel.checkEmailVerified(firebaseUser)
            timer += 3
        }

        if (isVerified) onVerified()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Verify your Email",
                color = White,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "A verification email has been sent to:\n${firebaseUser.email}",
                color = Silver,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Orange)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Waiting for email verification...",
                color = White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Back to Login", color = Orange)
            }
        }
    }
}
