package com.teamnotfound.airise.home.accountSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@Composable
fun AccountSettingScreen(
    user: UserDataUiState,
    navController: NavController,
    localNavController: NavHostController,
    accountSettingViewModel: AccountSettingsViewModel
) {
    val firebaseUser = Firebase.auth.currentUser
    LaunchedEffect(Unit) {
        firebaseUser?.let { accountSettingViewModel.getUserSettings(it) }
    }

    val uiState by accountSettingViewModel.uiState.collectAsState()
    val currentImageUrl = uiState.userSettings?.profilePictureUrl

    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }
    var candidateImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var candidateImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // If user is signed out successfully, route to welcome screen
    if (uiState.isSignedOut) {
        LaunchedEffect(uiState) {
            navController.navigate(AppScreen.WELCOME.name)
        }
    }

    val scope = rememberCoroutineScope()
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                candidateImage = it.toImageBitmap()
                candidateImageBytes = it
            }
        }
    )

    val dobSubtitle = formatDob(user, uiState.userData)
    val heightSubtitle = formatHeight(user, uiState.userData)
    val weightSubtitle = formatWeight(user, uiState.userData)


    // Settings items (Notifications REMOVED)
    val settings = listOf(
        SettingItem(
            title = "Name",
            subtitle = "${user.firstName.value} ${user.middleName.value} ${user.lastName.value}".replace(Regex("\\s+"), " ").trim(),
            onClick = { localNavController.navigate(AccountSettingScreens.NameEdit.route) }
        ),
        SettingItem(
            title = "Date of Birth",
            subtitle = dobSubtitle ?: "Add your birth date",
            onClick = { localNavController.navigate(AccountSettingScreens.DOBSelect.route) }
        ),
            SettingItem(
            title = "Height",
            subtitle = heightSubtitle ?: "Add your height",
            onClick = { localNavController.navigate(AccountSettingScreens.HeightSelect.route) }
        ),
        SettingItem(
            title = "Weight",
            subtitle = weightSubtitle ?: "Add your weight",
            onClick = { localNavController.navigate(AccountSettingScreens.WeightSelect.route) }
        ),
        SettingItem(
            title = "Connect a New Smart Device",
            subtitle = "Link wearables and health apps",
            onClick = { localNavController.navigate(AccountSettingScreens.HealthDashboard.route) }
        ),
        SettingItem(
            title = "Customize AI Personality",
            subtitle = "Tone, style, and behavior",
            onClick = { localNavController.navigate(AccountSettingScreens.AiPersonality.route) }
        )
    )

    // Body
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        AccountSettingsTopBar(
            title = "Account Settings",
            subtitle = "Manage your profile",
            onBackClick = {
                navController.navigate(AppScreen.HOMESCREEN.name) {
                    popUpTo("home") { inclusive = true }
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {

            // Profile Card
            item {
                Surface(
                    color = DeepBlue.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(listOf(Orange, Silver)),
                                    shape = CircleShape
                                )
                                .clickable { showImagePickerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentImageUrl.isNullOrBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "No Profile Picture",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(42.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = currentImageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Small floating edit chip
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepBlue)
                                    .clickable { showImagePickerDialog = true }
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${user.firstName.value} ${user.middleName.value} ${user.lastName.value}"
                                    .replace(Regex("\\s+"), " ").trim()
                                    .ifBlank { "Your name" },
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = Firebase.auth.currentUser?.email ?: "Add an email",
                                color = White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tap photo to update",
                                color = Orange,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Settings list
            items(settings) { item ->
                SettingsRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    onClick = item.onClick
                )
                Divider(color = DeepBlue.copy(alpha = 0.6f), thickness = 1.dp)
            }

            // Sign out button
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { accountSettingViewModel.signout() },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue)
                ) {
                    Text(text = "Sign Out", color = Color.White, fontSize = 18.sp)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // Image picker dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = {
                showImagePickerDialog = false
                candidateImage = null
                candidateImageBytes = null
            },
            title = { Text("Change Profile Picture", color = White) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pick a new image and preview it below:", color = White)
                    Spacer(Modifier.size(12.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        candidateImage?.let {
                            Image(
                                bitmap = it,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Silver
                        )
                    }

                    Spacer(Modifier.size(16.dp))

                    Button(
                        onClick = { singleImagePicker.launch() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Orange)
                    ) {
                        Text("Pick Image", color = White)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        candidateImageBytes?.let {
                            accountSettingViewModel.uploadProfilePicture(it, firebaseUser)
                        }
                        showImagePickerDialog = false
                        candidateImage = null
                        candidateImageBytes = null
                    }
                ) {
                    Text("Save", color = Orange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImagePickerDialog = false
                        candidateImage = null
                        candidateImageBytes = null
                    }
                ) {
                    Text("Cancel", color = Silver)
                }
            },
            backgroundColor = BgBlack
        )
    }
}

/** Compact top bar with gradient and back button (unchanged baseline, lightly cleaned) */
@Composable
private fun AccountSettingsTopBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit
) {
    Surface(color = Color.Transparent, elevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            DeepBlue,
                            DeepBlue.copy(alpha = .92f),
                            DeepBlue.copy(alpha = .86f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = White.copy(alpha = 0.82f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }
    }
}

/** Single-line settings row with title + optional subtitle + chevron */
@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun formatDob(user: UserDataUiState, fallback: UserData?): String? {
    val d = user.dobDay.value.takeIf { it > 0 } ?: fallback?.dobDay ?: 0
    val m = user.dobMonth.value.takeIf { it > 0 } ?: fallback?.dobMonth ?: 0
    val y = user.dobYear.value.takeIf { it > 0 } ?: fallback?.dobYear ?: 0
    if (d == 0 || m == 0 || y == 0) return null

    // zero-pad day/month for consistency, e.g., 03/09/1998
    val mm = m.toString().padStart(2, '0')
    val dd = d.toString().padStart(2, '0')
    return "$mm/$dd/$y"
}

private fun formatHeight(user: UserDataUiState, fallback: UserData?): String? {
    val metric = user.heightMetric.value.takeIf { user.heightValue.value != 0 } ?: fallback?.heightMetric
    val value  = user.heightValue.value.takeIf { it > 0 } ?: fallback?.heightValue ?: 0
    if (value == 0 || metric == null) return null

    return if (metric) {
        // metric stored as centimeters
        "$value cm"
    } else {
        // imperial stored as inches -> show as 5'11"
        val feet = value / 12
        val inches = value % 12
        "${feet}\'${inches}\""
    }
}

private fun formatWeight(user: UserDataUiState, fallback: UserData?): String? {
    val metric = user.weightMetric.value.takeIf { user.weightValue.value != 0 } ?: fallback?.weightMetric
    val value  = user.weightValue.value.takeIf { it > 0 } ?: fallback?.weightValue ?: 0
    if (value == 0 || metric == null) return null

    return if (metric) {
        "$value kg"
    } else {
        "$value lb"
    }
}

private data class SettingItem(
    val title: String,
    val subtitle: String? = null,
    val onClick: () -> Unit
)
