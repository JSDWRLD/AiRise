package com.teamnotfound.airise.login

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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Email
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onPrivacyPolicyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Login(
        uiState = uiState.value,
        onEvent = { viewModel.onEvent(it) },
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onSignUpClick = onSignUpClick,
        onGoogleSignInClick = onGoogleSignInClick
    )
}

@Composable
fun Login(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    // Removed redundant local state variables since we're using uiState

    //screen arrangement
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF062022)) // background coloring
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 90.dp)
        ) {
            // title of screen
            Text(
                "Welcome back!",
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // email input
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { onEvent(LoginUiEvent.EmailChanged(it)) },
                label = { Text("Email Address", color = Color.Gray) },
                singleLine = true,
                // email icon
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = "Email Icon", tint = Color.Gray)
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // password input
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onEvent(LoginUiEvent.PasswordChanged(it)) },
                label = { Text("Password", color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = "Password Icon", tint = Color.Gray)
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Login button with loading state
            Button(
                onClick = { onEvent(LoginUiEvent.Login) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // forgot password button
            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot password?", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // divider (or)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.Gray, thickness = 1.dp)
                Text("  or  ", color = Color.Gray)
                Divider(modifier = Modifier.weight(1f), color = Color.Gray, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // google sign in button
            Button(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B))
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            //terms and conditions button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "By registering, you agree to our:",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { /* Terms to be added */ }, contentPadding = PaddingValues(0.dp)) {
                    Text("Terms & Conditions", color = Color(0xFFFFA500), fontSize = 12.sp)
                }
                TextButton(onClick = onPrivacyPolicyClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Privacy Policy", color = Color(0xFFFFA500), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            //sign up buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account?",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(3.dp))

                TextButton(onClick = onSignUpClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign up", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}