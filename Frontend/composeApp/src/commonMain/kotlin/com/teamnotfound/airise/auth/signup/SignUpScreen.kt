package com.teamnotfound.airise.auth.signup

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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.teamnotfound.airise.data.DTOs.RegisterUserDTO
import com.teamnotfound.airise.util.*

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignUpClick: () -> Unit,
    onBackClick: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    // Observe the UI state from the view model
    val uiState by viewModel.uiState.collectAsState()
    var attemptedSubmit by remember { mutableStateOf(false) }
    val showErrors = attemptedSubmit


    // If sign up is successful, trigger navigation or any other success action.
    if (uiState.isSuccess) {
        // Using LaunchedEffect to perform a side-effect (navigation)
        LaunchedEffect(uiState) {
            // Continue to onboard screen
            onSignUpSuccess()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val passwordsMatch = password == confirmPassword //password validation

    //screen placement
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        //back arrow in the left top corner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Orange
                )
            }
        }

        // sign up column
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .padding(24.dp)
        ) {
            // Display error message from UI state, if any
            uiState.errorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //only after clicking
            if (showErrors && uiState.passwordErrors.isEmpty() && uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //title of the screen of create your account
            Text(
                "Create your account",
                fontSize = 24.sp,
                color = White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text("Email Address", color = Silver)
                },                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email Icon", tint = Silver) },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(White, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver,
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // password input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (attemptedSubmit) {
                        viewModel.validatePassword(password, confirmPassword)
                    }
                },
                placeholder = {
                    Text("Password", color = Silver)
                },                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon", tint = Silver) },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(White, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver,
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // confirm password input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (attemptedSubmit) {
                        viewModel.validatePassword(password, confirmPassword)
                    }
                },
                placeholder = {
                    Text("Confirm Password", color = Silver)
                },                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon", tint = Silver) },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(White, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver,
                )
            )

            //shows message of error for password
            if (attemptedSubmit && uiState.passwordErrors.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    uiState.passwordErrors.forEach { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // create account button
            Button(
                onClick = {
                    attemptedSubmit = true
                    viewModel.validatePassword(password, confirmPassword)

                    if (passwordsMatch && uiState.passwordErrors.isEmpty()) {
                        val userModel = RegisterUserDTO(
                            email = email,
                            password = password
                        )
                        viewModel.register(userModel) // make a register
                    }
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                enabled = passwordsMatch && uiState.passwordErrors.isEmpty()
            ) {
                Text("Create Account", color = White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // forgot password button
            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot password?", color = White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // or divider on screen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = Silver, thickness = 1.dp)
                Text("  or  ", color = Color.Gray)
                Divider(modifier = Modifier.weight(1f), color = Silver, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // google sign in button
            Button(
                onClick = onGoogleSignUpClick,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue)
            ) {
                Text("Continue with Google", color = White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // terms and conditions button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "By registering, you agree to our:",
                    color = White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { /* Terms */ }, contentPadding = PaddingValues(0.dp)) {
                    Text("Terms & Conditions", color = Orange, fontSize = 12.sp)
                }
                TextButton(onClick = { /* Policy */ }, contentPadding = PaddingValues(0.dp)) {
                    Text("Privacy Policy", color = Orange, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // sign up button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account?",
                    color = White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Log in", color = White, fontSize = 12.sp)
                }
            }
        }
    }
}