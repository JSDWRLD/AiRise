package com.teamnotfound.airise.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import com.teamnotfound.airise.auth.general.BackChip
import com.teamnotfound.airise.util.*

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
                            listOf(
                                DeepBlue.copy(alpha = 0.98f),
                                DeepBlue.copy(alpha = 0.78f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BackChip(onClick = onBackClick, showLabel = false)
                    Spacer(Modifier.width(10.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stepTitle,
                            color = White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (onSkipClick != null) {
                        TextButton(
                            onClick = onSkipClick,
                            modifier = Modifier.padding(start = 8.dp)
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
            }
        }

        Spacer(Modifier.height(16.dp))
        content()
    }
}
