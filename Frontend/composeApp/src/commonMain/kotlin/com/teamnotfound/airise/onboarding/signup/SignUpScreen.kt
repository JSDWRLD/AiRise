package com.teamnotfound.airise.onboarding.signup

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.teamnotfound.airise.data.DTOs.RegisterUserDTO

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
            .background(Color(0xFF062022))
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
                    tint = Color(0xFFFFA500)
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

            //title of the screen of create your account
            Text(
                "Create your account",
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = Color.Gray) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email Icon", tint = Color.Gray) },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(Color(0xFF1B263B), RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(Color(0xFF1B263B), RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // confirm password input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(Color(0xFF1B263B), RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray,
                )
            )

            //shows message if password does not match
            if(!passwordsMatch){
                Text(
                    text = "Passwords do not match",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // create account button
            Button(
                onClick = {
                    val userModel = RegisterUserDTO(
                        email = email,
                        password = password
                    )
                    viewModel.register(userModel) // make a register
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B)),
                enabled = passwordsMatch //disable button when passwords do not match
            ) {
                Text("Create Account", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // forgot password button
            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot password?", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // or divider on screen
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
                onClick = onGoogleSignUpClick,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B))
            ) {
                Text("Continue with Google", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // terms and conditions button
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
                TextButton(onClick = { /* Terms */ }, contentPadding = PaddingValues(0.dp)) {
                    Text("Terms & Conditions", color = Color(0xFFFFA500), fontSize = 12.sp)
                }
                TextButton(onClick = { /* Policy */ }, contentPadding = PaddingValues(0.dp)) {
                    Text("Privacy Policy", color = Color(0xFFFFA500), fontSize = 12.sp)
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
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Log in", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}