package com.teamnotfound.airise.auth.login

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Email
import com.teamnotfound.airise.util.*
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import com.teamnotfound.airise.BuildKonfig
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onPrivacyPolicyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLoginSuccess: (email: String) -> Unit,
    onBackClick: () -> Unit,
) {
    var authReady by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState.collectAsState()

    // Initialize Google Auth Provider
    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
              serverId = BuildKonfig.GOOGLE_OAUTH_WEB_CLIENT_ID
            )
        )
        authReady = true
    }

    if (uiState.value.isLoggedIn) {
        // Using LaunchedEffect to perform a side-effect (navigation)
        LaunchedEffect(uiState.value.email) {
            // Continue to onboard screen
            onLoginSuccess(uiState.value.email)
        }
    }

    Login(
        uiState = uiState.value,
        onEvent = { viewModel.onEvent(it) },
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onSignUpClick = onSignUpClick,
        authReady = authReady,
        onBackClick = onBackClick
    )
}

@Composable
fun Login(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    authReady: Boolean,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        // Back button at the top-left corner (consistent with SignUpScreen)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Orange
            )
        }

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp) // Match SignUpScreen top padding
                .padding(24.dp)
        ) {

            Text("Welcome back!", fontSize = 24.sp, color = White)
            Spacer(modifier = Modifier.height(24.dp))

            // Email Input
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { onEvent(LoginUiEvent.EmailChanged(it)) },
                placeholder = {
                    Text("Email Address", color = Silver)
                },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email Icon", tint = Silver) },
                modifier = Modifier.width(300.dp).height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Password Input
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onEvent(LoginUiEvent.PasswordChanged(it)) },
                placeholder = {
                    Text("Password", color = Silver)
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon", tint = Silver) },
                modifier = Modifier.width(300.dp).height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Display error message if login fails
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Login Button with loading indicator
            Button(
                onClick = {
                    if (!uiState.isLoading) {
                        onEvent(LoginUiEvent.Login)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.width(300.dp).height(50.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("Login", color = White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot Password Button
            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot password?", color = White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = Silver, thickness = 1.dp)
                Text("  or  ", color = Color.Gray)
                Divider(modifier = Modifier.weight(1f), color = Silver, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (authReady) {
                GoogleButtonUiContainer(
                    onGoogleSignInResult = { googleUser ->
                        val idToken = googleUser?.idToken


                        if (idToken != null) {
                            onEvent(LoginUiEvent.GoogleSignInSuccess(idToken))
                        }
                    }
                ) {

                    Button(
                        onClick = { this.onClick() },
                        modifier = Modifier.width(300.dp).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue)
                    ) {
                        Text("Continue with Google", color = White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Terms and Conditions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("By registering, you agree to our:", color = White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { /* TODO: Add Terms */ }, contentPadding = PaddingValues(0.dp)) {
                    Text("Terms & Conditions", color = Orange, fontSize = 12.sp)
                }
                TextButton(onClick = onPrivacyPolicyClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Privacy Policy", color = Orange, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Sign-up Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?", color = White, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(3.dp))

                TextButton(onClick = onSignUpClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign up", color = White, fontSize = 12.sp)
                }
            }
        }
    }
}