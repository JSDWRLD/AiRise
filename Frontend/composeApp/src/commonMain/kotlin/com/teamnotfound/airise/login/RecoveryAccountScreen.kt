
package com.teamnotfound.airise.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecoverAccountScreen(
    onBackClick: () -> Unit,
    onSendEmailClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    //screen arrangement
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1E22))
    ) {
        // back arrow icon
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // space from edges
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

        // password recovery placement
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .padding(24.dp)
        ) {

            // title of screen
            Text(
                "Reset your password",
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                //email icon
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email",
                        tint = Color.Gray
                    )
                },
                label = { Text("Email Address", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // send email button
            Button(
                onClick = onSendEmailClick,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B))
            ) {
                Text("Send Email", color = Color.White)
            }
        }
    }
}
