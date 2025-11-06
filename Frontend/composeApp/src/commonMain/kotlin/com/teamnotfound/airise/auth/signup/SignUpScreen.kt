package com.teamnotfound.airise.auth.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.teamnotfound.airise.BuildKonfig
import com.teamnotfound.airise.auth.general.AuthCard
import com.teamnotfound.airise.auth.general.AuthErrorBanner
import com.teamnotfound.airise.auth.general.AuthField
import com.teamnotfound.airise.auth.general.AuthHeader
import com.teamnotfound.airise.auth.general.OrDivider
import com.teamnotfound.airise.auth.general.PrimaryButton
import com.teamnotfound.airise.auth.general.friendlyAuthError
import com.teamnotfound.airise.data.DTOs.RegisterUserDTO
import com.teamnotfound.airise.util.*

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onLoginClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    onSignUpSuccessWithUser: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var attemptedSubmit by remember { mutableStateOf(false) }
    var authReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(serverId = BuildKonfig.GOOGLE_OAUTH_WEB_CLIENT_ID)
        )
        authReady = true
    }

    if (uiState.isSuccess) {
        LaunchedEffect(uiState) { onSignUpSuccessWithUser() }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    password == confirmPassword

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(Modifier.fillMaxSize()) {
            AuthHeader(
                title = "Create your account",
                subtitle = "It only takes a minute.",
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
                    if (attemptedSubmit && uiState.passwordErrors.isEmpty()) {
                        val friendlyError = friendlyAuthError(uiState.errorMessage)
                        if (friendlyError != null) {
                            AuthErrorBanner(message = friendlyError)
                        }
                    }

                    AuthField(
                        value = email,
                        onValueChange = { email = it },
                        hint = "Email Address",
                        leading = { Icon(Icons.Outlined.Email, null, tint = Silver) }
                    )

                    AuthField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (attemptedSubmit) viewModel.validatePassword(password, confirmPassword)
                        },
                        hint = "Password",
                        leading = { Icon(Icons.Outlined.Lock, null, tint = Silver) },
                        isPassword = true
                    )

                    AuthField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (attemptedSubmit) viewModel.validatePassword(password, confirmPassword)
                        },
                        hint = "Confirm Password",
                        leading = { Icon(Icons.Outlined.Lock, null, tint = Silver) },
                        isPassword = true
                    )

                    if (attemptedSubmit && uiState.passwordErrors.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            uiState.passwordErrors.forEach { err ->
                                Text(err, color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }

                    PrimaryButton(
                        text = "Create Account",
                        onClick = {
                            attemptedSubmit = true
                            viewModel.validatePassword(password, confirmPassword)
                            viewModel.register(RegisterUserDTO(email = email, password = password))
                        }
                    )

                    TextButton(onClick = onForgotPasswordClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Forgot password?", color = White, fontSize = 12.sp)
                    }

                    OrDivider()

                    if (authReady) {
                        GoogleButtonUiContainer(
                            onGoogleSignInResult = { googleUser ->
                                googleUser?.idToken?.let { viewModel.authenticateWithGoogle(it) }
                            }
                        ) {
                            PrimaryButton(
                                text = "Continue with Google",
                                onClick = { this.onClick() }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text("By registering, you agree to our:", color = White, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = onTermsClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Terms & Conditions", color = Orange, fontSize = 12.sp)
                    }
                    TextButton(onClick = onPrivacyPolicyClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Privacy Policy", color = Orange, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already have an account?", color = White, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Log in", color = Orange, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

