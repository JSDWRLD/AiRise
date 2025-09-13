package com.teamnotfound.airise.community.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.White

@Composable
fun ChallengeDetailsScreen(
    viewModel: ChallengesViewModelImpl,
    onBackClick: () -> Unit
) {
    //current selected challenge from vm
    val item by viewModel.selected.collectAsState()

    //title
    val title = item?.name?.takeIf { it.isNotBlank() }
        ?: item?.id?.let { "Challenge $it" }
        ?: "Challenge"

    // local edit for description
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var localDesc by remember(item) { mutableStateOf(item?.description ?: "") }

    Scaffold(
        backgroundColor = BgBlack,
        //back button
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Orange
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            // title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // description which is readable and editable
            if (isEditing) {
                OutlinedTextField(
                    value = localDesc,
                    onValueChange = { localDesc = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp),
                    textStyle = LocalTextStyle.current.copy(color = White),
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        focusedBorderColor = White,
                        unfocusedBorderColor = White,
                        cursorColor = White,
                        focusedLabelColor = White,
                        unfocusedLabelColor = White
                    ),
                    label = { Text("Challenge Description", color = White) }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .border(
                            width = 2.dp,
                            color = White,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (localDesc.isBlank()) "No description yet." else localDesc,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //buttons for save and edit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isEditing = true },
                    enabled = !isEditing,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DeepBlue,
                        contentColor = White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp)
                ) {
                    Text("Edit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.updateSelectedDescription(localDesc)
                        isEditing = false
                    },
                    enabled = isEditing,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DeepBlue,
                        contentColor = White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp)
                ) {
                    Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
