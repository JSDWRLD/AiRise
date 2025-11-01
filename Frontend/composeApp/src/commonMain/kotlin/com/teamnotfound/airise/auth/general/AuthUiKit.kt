package com.teamnotfound.airise.auth.general

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import androidx.compose.material.Surface
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.teamnotfound.airise.util.White

@Composable
fun AuthHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    Surface(
        color = Color.Transparent,
        elevation = 6.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(DeepBlue.copy(alpha = 0.98f), DeepBlue.copy(alpha = 0.78f))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onBackClick != null) {
                    BackChip(onClick = onBackClick, showLabel = false)
                    Spacer(Modifier.width(10.dp))
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(text = subtitle, color = Silver, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = BgBlack,
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp,
        border = BorderStroke(1.dp, DeepBlue.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue, contentColor = White),
        modifier = modifier
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    leading: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var hidden by remember { mutableStateOf(isPassword) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = leading,
        trailingIcon = if (isPassword) {
            {
                TextButton(onClick = { hidden = !hidden }, contentPadding = PaddingValues(0.dp)) {
                    Text(if (hidden) "Show" else "Hide", color = Orange, fontSize = 12.sp)
                }
            }
        } else null,
        placeholder = { Text(hint, color = Silver) },
        visualTransformation = if (isPassword && hidden) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = Orange,
            unfocusedBorderColor = Silver,
            textColor = Color.Black,
            placeholderColor = Silver,
            leadingIconColor = Silver,
            trailingIconColor = Orange,
            cursorColor = DeepBlue
        ),
        modifier = modifier.height(56.dp)
    )
}

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Divider(Modifier.weight(1f), color = Silver.copy(alpha = 0.6f), thickness = 1.dp)
        Text("  OR  ", color = Silver.copy(alpha = 0.8f), fontSize = 12.sp)
        Divider(Modifier.weight(1f), color = Silver.copy(alpha = 0.6f), thickness = 1.dp)
    }
}

@Composable
 fun TopLeftBack(onBack: () -> Unit) {
    IconButton(
        onClick = onBack,
        modifier = Modifier
            .padding(16.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Orange)
    }
}

@Composable
fun BackChip(
    onClick: () -> Unit,
    showLabel: Boolean = false,
    label: String = "Back",
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DeepBlue.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, DeepBlue),
        modifier = Modifier
            .height(40.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = White,
                modifier = Modifier.size(18.dp)
            )
            if (showLabel) {
                Spacer(Modifier.width(6.dp))
                Text(text = label, color = White, fontSize = 14.sp)
            }
        }
    }
}
