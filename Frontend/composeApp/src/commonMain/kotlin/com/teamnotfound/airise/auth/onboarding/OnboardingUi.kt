package com.teamnotfound.airise.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.auth.general.AuthHeader
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Orange

@Composable
fun OnboardingScaffold(
    stepTitle: String,                // e.g. "Fitness Goal (3/13)"
    onBackClick: () -> Unit,
    onSkipClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        // Header box lets us overlay the Skip button inside the same visual area
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AuthHeader(
                title = stepTitle,
                subtitle = "",
                onBackClick = onBackClick
            )

            // Skip sits on the right INSIDE the header
            if (onSkipClick != null) {
                TextButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp) // slightly tucked in for balance
                ) {
                    Text(
                        text = "Skip",
                        color = Orange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        content()
    }
}
