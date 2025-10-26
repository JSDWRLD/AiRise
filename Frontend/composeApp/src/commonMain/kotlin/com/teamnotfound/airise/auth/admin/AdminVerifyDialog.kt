package com.teamnotfound.airise.auth.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun AdminVerifyDialog(
    onDismiss: () -> Unit,
    uiState: AdminVerifyUiState,
    onEvent: (AdminVerifyUiEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Admin Verification", color = White) },
        text = {
            Column {
                Text("Please re-enter your password to continue.", color = Silver)
                Spacer(Modifier.height(8.dp))
                // Password Input
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { onEvent(AdminVerifyUiEvent.PasswordChanged(it)) },
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
                if (uiState.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.errorMessage, color = Orange)
                }
            }
        },
        backgroundColor = BgBlack,
        confirmButton = {
            TextButton(onClick = {
                if (!uiState.isLoading) {
                    onEvent(AdminVerifyUiEvent.Verify)
                }
            }) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirm", color = White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Silver)
            }
        }
    )
    if (uiState.isVerified) onDismiss()
}