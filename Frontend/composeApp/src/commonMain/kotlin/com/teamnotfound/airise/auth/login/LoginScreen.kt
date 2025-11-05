package com.teamnotfound.airise.auth.login

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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Email
import com.teamnotfound.airise.util.*
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLoginSuccess: (email: String) -> Unit,
    onBackClick: () -> Unit,
) {
    var authReady by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.value.isLoggedIn) {
        if (uiState.value.isLoggedIn) {
            onLoginSuccess(uiState.value.email)
        }
    }

    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(serverId = BuildKonfig.GOOGLE_OAUTH_WEB_CLIENT_ID)
        )
        authReady = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(Modifier.fillMaxSize()) {
            AuthHeader(
                title = "Welcome back!",
                subtitle = "Log in to continue your journey.",
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
                    AuthField(
                        value = uiState.value.email,
                        onValueChange = { viewModel.onEvent(LoginUiEvent.EmailChanged(it)) },
                        hint = "Email Address",
                        leading = { Icon(Icons.Outlined.Email, null, tint = Silver) }
                    )

                    AuthField(
                        value = uiState.value.password,
                        onValueChange = { viewModel.onEvent(LoginUiEvent.PasswordChanged(it)) },
                        hint = "Password",
                        leading = { Icon(Icons.Outlined.Lock, null, tint = Silver) },
                        isPassword = true
                    )

                    val friendlyError = friendlyAuthError(uiState.value.errorMessage)
                    if (friendlyError != null) {
                        AuthErrorBanner(message = friendlyError)
                    }

                    PrimaryButton(
                        text = "Login",
                        loading = uiState.value.isLoading,
                        onClick = { if (!uiState.value.isLoading) viewModel.onEvent(LoginUiEvent.Login) }
                    )

                    TextButton(onClick = onForgotPasswordClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Forgot password?", color = White, fontSize = 12.sp)
                    }

                    OrDivider()

                    if (authReady) {
                        GoogleButtonUiContainer(
                            onGoogleSignInResult = { user ->
                                user?.idToken?.let { viewModel.onEvent(LoginUiEvent.GoogleSignInSuccess(it)) }
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

                Text("By logging in, you agree to our:", color = White, fontSize = 12.sp)
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
                    Text("Don't have an account?", color = White, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onSignUpClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Sign up", color = Orange, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

