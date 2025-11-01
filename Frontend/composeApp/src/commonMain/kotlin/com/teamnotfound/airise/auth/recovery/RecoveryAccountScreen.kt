package com.teamnotfound.airise.auth.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.ui.graphics.Color
import com.teamnotfound.airise.auth.general.AuthCard
import com.teamnotfound.airise.auth.general.AuthField
import com.teamnotfound.airise.auth.general.AuthHeader
import com.teamnotfound.airise.auth.general.PrimaryButton
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun RecoverAccountScreen(
    viewModel: RecoveryViewModel,
    onBackClick: () -> Unit,
    onSendEmailClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    val isValid = remember(email) { email.contains("@") && email.contains(".") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(Modifier.fillMaxSize()) {
            AuthHeader(
                title = "Reset your password",
                subtitle = "We'll email you a reset link.",
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
                    // Intro
                    Text(
                        "Enter the email associated with your account.",
                        color = Silver,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    // Email field (matches app style)
                    AuthField(
                        value = email,
                        onValueChange = { email = it },
                        hint = "Email Address",
                        leading = { androidx.compose.material.Icon(Icons.Outlined.Email, null, tint = Silver) }
                    )

                    // Inline validation (optional)
                    if (attempted && !isValid) {
                        Text(
                            "Please enter a valid email address.",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    // Send button (matches PrimaryButton style)
                    PrimaryButton(
                        text = "Send Reset Link",
                        onClick = {
                            attempted = true
                            if (isValid) {
                                viewModel.sendEmail(email)
                                onSendEmailClick()
                            }
                        }
                    )

                    // Helper text
                    Text(
                        "Check your inbox (and spam folder) for our message.",
                        color = Silver,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
