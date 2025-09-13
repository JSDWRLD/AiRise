package com.teamnotfound.airise.community.communityNavBar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.NeonGreen
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun CommunityNavBar(
    navController: NavController,
    currentPage: CommunityPage,
    viewModel: CommunityNavBarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = currentPage) {
        viewModel.updatePage(currentPage)
    }

    // Simple helpers for display
    val userData = uiState.userData  // <-- local immutable copy enables smart cast

    val displayName = if (userData != null) {
        listOf(userData.firstName, userData.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "User" }
    } else {
        "Loading..."
    }

    fun initialsOf(first: String?, last: String?): String {
        val parts = listOfNotNull(first, last)
            .flatMap { it.split(" ") }
            .filter { it.isNotBlank() }
        return parts.take(2).joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }

    val initials = initialsOf(
        uiState.userData?.firstName,
        uiState.userData?.lastName
    )

    // App-level background for the bar section
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Card container with soft shadow and gradient fill
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.Transparent,
            border = BorderStroke(1.dp, Silver.copy(alpha = 0.22f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(0.dp, RoundedCornerShape(20.dp)) // keep crisp edges
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DeepBlue.copy(alpha = 0.85f),
                                Silver.copy(alpha = 0.75f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Header row: avatar + name + stat pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with neon/cyan ring
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BgBlack)
                        ) {
                            // ring
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Cyan.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            if (!uiState.userProfilePicture.isNullOrBlank()) {
                                // Show profile picture
                                AsyncImage(
                                    model = uiState.userProfilePicture,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback: initials circle
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Silver.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val nameColor = when {
                                uiState.errorMessage != null -> Orange
                                else -> White
                            }
                            Text(
                                text = if (uiState.errorMessage != null) "Unable to load user" else displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = nameColor
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatPill(
                                    background = BgBlack.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, Silver.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak",
                                            tint = Orange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "${uiState.streak} day streak",
                                            color = White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                StatPill(
                                    background = Orange.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Orange.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "#${uiState.rank}",
                                        color = Orange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Section title and action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Community",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = White
                            )
                            Text(
                                text = uiState.page.name,
                                color = Silver,
                                fontSize = 12.sp,
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            when (uiState.page) {
                                CommunityPage.Leaderboard -> {
                                    PrimaryCTA(
                                        text = "Friends",
                                        onClick = { navController.navigate(AppScreen.FRIENDS.name) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    SecondaryCTA(
                                        text = "Challenges",
                                        onClick = { navController.navigate(AppScreen.CHALLENGES.name) }
                                    )
                                }
                                CommunityPage.Friends -> {
                                    PrimaryCTA(
                                        text = "Leaderboard",
                                        onClick = { /* navController.navigate(AppScreen.LEADERBOARD.name) */ }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    SecondaryCTA(
                                        text = "Challenges",
                                        onClick = { navController.navigate(AppScreen.CHALLENGES.name) }
                                    )
                                }
                                CommunityPage.Challenges -> {
                                    PrimaryCTA(
                                        text = "Leaderboard",
                                        onClick = { /* navController.navigate(AppScreen.LEADERBOARD.name) */ }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    SecondaryCTA(
                                        text = "Friends",
                                        onClick = { navController.navigate(AppScreen.FRIENDS.name) }
                                    )
                                }
                            }
                        }
                    }

                    // Optional: loading bar under the header
                    if (uiState.isLoading) {
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = Cyan,
                            backgroundColor = Silver.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

/* --- Reusable bits --- */

@Composable
private fun StatPill(
    background: Color,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = background,
        elevation = 0.dp,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun PrimaryCTA(
    text: String,
    onClick: () -> Unit
) {
    // Filled button with cyan glow + orange accent border
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = DeepBlue.copy(alpha = 0.9f),
            contentColor = White
        ),
        border = BorderStroke(1.dp, Orange.copy(alpha = 0.6f)),
        modifier = Modifier
            .widthIn(min = 160.dp)
            .height(44.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Cyan.copy(alpha = 0.25f), Color.Transparent)
                ),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryCTA(
    text: String,
    onClick: () -> Unit
) {
    // Ghost/outline button for lower emphasis
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Silver.copy(alpha = 0.45f)),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = BgBlack.copy(alpha = 0.4f),
            contentColor = White
        ),
        modifier = Modifier
            .widthIn(min = 160.dp)
            .height(44.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
