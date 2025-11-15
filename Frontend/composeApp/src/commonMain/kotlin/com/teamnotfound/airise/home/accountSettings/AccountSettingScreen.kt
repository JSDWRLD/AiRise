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
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import io.ktor.util.date.getTimeMillis
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.ButtonDefaults as M3ButtonDefaults

@Composable
fun AccountSettingScreen(
    user: UserDataUiState,
    navController: NavController,
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

    LaunchedEffect(uiState.userSettings?.profilePictureUrl) {
        val newUrl = uiState.userSettings?.profilePictureUrl
        if (!newUrl.isNullOrBlank()) {
            navController.getBackStackEntry(AppScreen.HOMESCREEN.name)
                .savedStateHandle
                .set<Long>("profile_picture_updated", getTimeMillis())
        }
    }

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
            onClick = { navController.navigate(AppScreen.ACCOUNT_SETTINGS_NAME_EDIT.name) }
        ),
        SettingItem(
            title = "Date of Birth",
            subtitle = dobSubtitle ?: "Add your birth date",
            onClick = { navController.navigate(AppScreen.ACCOUNT_SETTINGS_DOB.name) }
        ),
            SettingItem(
            title = "Height",
            subtitle = heightSubtitle ?: "Add your height",
            onClick = { navController.navigate(AppScreen.ACCOUNT_SETTINGS_HEIGHT.name) }
        ),
        SettingItem(
            title = "Weight",
            subtitle = weightSubtitle ?: "Add your weight",
            onClick = { navController.navigate(AppScreen.ACCOUNT_SETTINGS_WEIGHT.name) }
        ),
        SettingItem(
            title = "Connect a New Smart Device",
            subtitle = "Link wearables and health apps",
            onClick = { navController.navigate(AppScreen.HEALTH_DASHBOARD.name) }
        ),
        SettingItem(
            title = "Customize AI Personality",
            subtitle = "Tone, style, and behavior",
            onClick = { navController.navigate(AppScreen.ACCOUNT_SETTINGS_AI_PERSONALITY.name) }
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
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .clickable { showImagePickerDialog = true },
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
        ProfilePictureDialog(
            candidateImage = candidateImage,
            onPick = { singleImagePicker.launch() },
            onSave = {
                candidateImageBytes?.let { accountSettingViewModel.uploadProfilePicture(it, firebaseUser) }
                showImagePickerDialog = false
                candidateImage = null
                candidateImageBytes = null
            },
            onCancel = {
                showImagePickerDialog = false
                candidateImage = null
                candidateImageBytes = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePictureDialog(
    candidateImage: ImageBitmap?,
    onPick: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Full-screen overlay inside your current layout tree
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // scrim that truly covers the screen
            .clickable(
                // dismiss when tapping outside the card
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        // The dialog card
        M3Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(min = 280.dp, max = 420.dp)
                .clickable( // consume taps so the scrim’s clickable doesn’t fire
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { /* no-op */ }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(BgBlack)
                    .padding(20.dp)
            ) {
                M3Text("Change Profile Picture", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(12.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (candidateImage != null) {
                        Image(
                            bitmap = candidateImage,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Silver)
                    }
                }

                Spacer(Modifier.size(16.dp))
                M3Button(
                    onClick = onPick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = M3ButtonDefaults.buttonColors(
                        containerColor = Orange,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    M3Text("Pick Image")
                }
                Spacer(Modifier.size(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    M3TextButton(onClick = onCancel) { M3Text("Cancel", color = Silver) }
                    M3TextButton(onClick = onSave)   { M3Text("Save",   color = Orange) }
                }
            }
        }
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
