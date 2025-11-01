package com.teamnotfound.airise.community.challenges.challengeEditor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.Transparent
import com.teamnotfound.airise.util.White

@Composable
fun ChallengeEditorDialog(
    viewModel: IChallengeEditorViewModel,
    onAuthorizationError: () -> Unit,
    onSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentImageUrl = uiState.challengeUI.imageUrl

    var showImagePickerDialog by remember { mutableStateOf(false) }
    var challengeImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var challengeImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val scope = rememberCoroutineScope()
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                challengeImage = it.toImageBitmap()
                challengeImageBytes = it
            }
        }
    )

    Dialog(
        onDismissRequest = { viewModel.onEvent(ChallengeEditorUiEvent.CloseEditor) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(BgBlack, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Cyan.copy(alpha = 0.18f), Transparent)
                        )
                    )
                    .blur(36.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Challenge",
                    color = White,
                    style = MaterialTheme.typography.h6
                )

                // --- Image section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(BgBlack),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentImageUrl.value.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "No Challenge Picture",
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = uiState.challengeUI.imageUrl.value,
                            contentDescription = uiState.challengeUI.name.value,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { showImagePickerDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(color = BgBlack)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = White,
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }
                }

                // --- Text fields
                TextField(
                    value = uiState.challengeUI.name.value,
                    onValueChange = { viewModel.onEvent(ChallengeEditorUiEvent.NameChanged(it)) },
                    label = { Text("Name", color = White) },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Silver,
                        backgroundColor = BgBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = uiState.challengeUI.description.value,
                    onValueChange = { viewModel.onEvent(ChallengeEditorUiEvent.DescriptionChanged(it)) },
                    label = { Text("Description", color = White) },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Silver,
                        backgroundColor = BgBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center
                    )
                }

                // --- Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.onEvent(ChallengeEditorUiEvent.CloseEditor) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Silver),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = White)
                    }

                    Button(
                        onClick = { viewModel.upsert(onSuccess) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm", color = White)
                    }
                }
            }

            // --- Nested image picker dialog
            if (showImagePickerDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showImagePickerDialog = false
                        challengeImage = null
                    },
                    title = { Text("Change Challenge Image", color = White) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pick a new image and preview it below:", color = White)
                            Spacer(Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                challengeImage?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } ?: Icon(
                                    Icons.Default.BrokenImage,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Silver
                                )
                            }

                            Spacer(Modifier.height(16.dp))

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
                        TextButton(onClick = {
                            challengeImageBytes?.let {
                                viewModel.onEvent(ChallengeEditorUiEvent.ImageChanged(it))
                            }
                            showImagePickerDialog = false
                            challengeImage = null
                            challengeImageBytes = null
                        }) {
                            Text("Save", color = Orange)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImagePickerDialog = false
                            challengeImage = null
                        }) {
                            Text("Cancel", color = Silver)
                        }
                    },
                    backgroundColor = BgBlack
                )
            }
        }
    }

    // Resets the verification process and requires ADMIN to enter password to retry
    if (uiState.authFailed) onAuthorizationError()
}