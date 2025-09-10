package com.teamnotfound.airise.communityNavBar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange

@Composable
fun CommunityNavBar(
    navController: NavController,
    currentPage: CommunityPage,
    viewModel: CommunityNavBarViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    LaunchedEffect(key1 = currentPage) {
        viewModel.updatePage(currentPage)
    }
    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(4.dp)
            .background(Color(0xFFE0E0E0)),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB5B0B3))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile section with picture, name, streak, and rank
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    // Name
                    Text(
                        text = userProfile.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Streak badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF212121))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color.Red, modifier = Modifier.size(16.dp))
                                Text(text = "${userProfile.streak}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Rank badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFA500))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "#${userProfile.rank}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Buttons for Community and Activity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Community",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    // alternating button names with routs
                    when (currentPage) {
                        CommunityPage.Leaderboard -> {
                            CommunityButton(
                                text = "Activity Feed",
                                color = ButtonDefaults.buttonColors(backgroundColor = Orange),
                                onClick = { navController.navigate(AppScreen.FRIENDS.name) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CommunityButton(
                                text = "Challenges",
                                color = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                                onClick = { navController.navigate(AppScreen.CHALLENGES.name) }
                            )
                        }
                        CommunityPage.ActivityFeed -> {
                            CommunityButton(
                                text = "Leaderboard",
                                color = ButtonDefaults.buttonColors(backgroundColor = Orange),
                                onClick = { /*navController.navigate(AppScreen.LEADERBOARD.name)*/ }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CommunityButton(
                                text = "Challenges",
                                color = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                                onClick = { navController.navigate(AppScreen.CHALLENGES.name) }
                            )
                        }
                        CommunityPage.Challenges -> {
                            CommunityButton(
                                text = "Leaderboard",
                                color = ButtonDefaults.buttonColors(backgroundColor = Orange),
                                onClick = { /*navController.navigate(AppScreen.LEADERBOARD.name)*/ }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CommunityButton(
                                text = "Activity Feed",
                                color = ButtonDefaults.buttonColors(backgroundColor = DeepBlue),
                                onClick = { navController.navigate(AppScreen.FRIENDS.name) }
                            )
                        }
                    }
                }
            }
            // page title
            Text(
                text = currentPage.name,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun CommunityButton(
    text: String,
    color: ButtonColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = color,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
