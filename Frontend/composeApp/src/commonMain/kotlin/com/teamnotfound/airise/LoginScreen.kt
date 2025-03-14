package com.teamnotfound.airise

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
import androidx.compose.material.icons.outlined.Email


@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = Color.Gray) },
                singleLine = true,
                // email icon
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = "Email Icon", tint = Color.Gray) },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .background(Color(0xFF1B263B), RoundedCornerShape(8.dp)),
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
                    .background(Color(0xFF1B263B), RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // login button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B))
            ) {
                Text("Login", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))


            // forgot password button
            TextButton(onClick = { /* onForgotPasswordClick() */ }) {
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

            // google sign in button and apple removed for android ui
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
                TextButton(onClick = { /* Policy to be added */ }, contentPadding = PaddingValues(0.dp)) {
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
                    "Don’t have an account?",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(3.dp))


                TextButton(onClick = { onSignUpClick() }, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign up", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
