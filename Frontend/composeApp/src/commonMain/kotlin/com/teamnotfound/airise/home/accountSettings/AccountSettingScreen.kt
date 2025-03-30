package com.teamnotfound.airise.home.accountSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.teamnotfound.airise.data.serializable.UserData
import androidx.compose.ui.layout.ContentScale
import com.preat.peekaboo.image.picker.toImageBitmap
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue


@Composable
fun AccountSettingScreen(
    user: UserData,
    navController: NavController,
    localNavController: NavHostController,
    accountSettingViewModel: AccountSettingsViewModel
) {
    val uiState by accountSettingViewModel.uiState.collectAsState()

    // If user is signed out successfully we route to welcome screen
    if (uiState.isSignedOut) {
        // Using LaunchedEffect to perform a side-effect (navigation)
        LaunchedEffect(uiState) {
            navController.navigate(AppScreen.WELCOME.name)
        }
    }

    val scope = rememberCoroutineScope()
    var images by remember { mutableStateOf(listOf<ImageBitmap>()) }
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                images = listOf(it.toImageBitmap())
            }
        }
    )
    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // back button
                IconButton(onClick = { navController.navigate(AppScreen.HOMESCREEN.name) { popUpTo("home") { inclusive = true } } }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Settings Icon",
                        tint = Color.White
                    )
                }
                // title
                Text(
                    text = "Account Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                    // modifier = Modifier.padding(top = 24.dp)
                )
                // save button
                IconButton(onClick = {}) { // call backend api to update to local values
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Settings Icon",
                        tint = Color.White
                    )
                }
            }
            // profile image
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (images.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "No Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                            .padding(16.dp),
                        tint = Color.Gray
                    )
                } else { // image selected
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        items(images) { image ->
                            Image(
                                bitmap = image,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                // overlay edit button
                IconButton(
                    onClick = { singleImagePicker.launch() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White
                    )
                }
            }
            // profile name
            TextField(
                value = "${user.firstName.value} ${user.middleName.value} ${user.lastName.value}",
                onValueChange = {
                    //name = it
                    val splitName = it.split(" ")
                    user.firstName.value = splitName.getOrNull(0) ?: ""
                    user.middleName.value = splitName.getOrNull(1) ?: ""
                    user.lastName.value = splitName.getOrNull(2) ?: ""
                },
                label = { Text(
                    text = "Profile Name",
                    color = Color.White
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                singleLine = true,
                textStyle = MaterialTheme.typography.body1.copy(color = Color.White),
            )

            // dob
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        localNavController.navigate(AccountSettingScreens.DOBSelect.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Date of Birth",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            Divider(color = DeepBlue, thickness = 1.dp)
            // height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        localNavController.navigate(AccountSettingScreens.HeightSelect.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Height",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            Divider(color = DeepBlue, thickness = 1.dp)
            // weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        localNavController.navigate(AccountSettingScreens.WeightSelect.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Weight",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            Divider(color = DeepBlue, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        localNavController.navigate(AccountSettingScreens.Notifications.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Notifications",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            Divider(color = DeepBlue, thickness = 1.dp)
            // connect device
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        // connect to KHealth
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connect a New Smart Device",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            Divider(color = DeepBlue, thickness = 1.dp)
            // ai personality
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        localNavController.navigate(AccountSettingScreens.AiPersonality.route)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize Ai Personality",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = Color.White
                )
            }
            Divider(color = DeepBlue, thickness = 1.dp)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    accountSettingViewModel.signout()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = DeepBlue)
            ) {
                Text(text = "Sign Out", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
