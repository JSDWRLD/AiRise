package com.teamnotfound.airise.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.teamnotfound.airise.data.serializable.UserData
import androidx.compose.ui.layout.ContentScale
import com.preat.peekaboo.image.picker.toImageBitmap

@Composable
fun AccountSettingScreen(user: UserData, navController: NavController) {
    val scope = rememberCoroutineScope()
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                user.profilePicture.value = listOf(it.toImageBitmap())
            }
        }
    )
    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
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
                IconButton(onClick = {navController.popBackStack()}) { // onclick not working
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
                if (user.profilePicture.value.isEmpty()) {
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
                        items(user.profilePicture.value) { image ->
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
                        navController.navigate(AccountSettingScreens.DOBSelect.route)
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
            // height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.HeightSelect.route)
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
            // weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.WeightSelect.route)
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
            // ai personality
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(AccountSettingScreens.AiPersonality.route)
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
        }
    }
}
