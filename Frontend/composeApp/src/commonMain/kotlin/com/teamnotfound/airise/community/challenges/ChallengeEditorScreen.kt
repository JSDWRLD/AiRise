package com.teamnotfound.airise.community.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.White
import com.teamnotfound.airise.util.DeepBlue
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import com.teamnotfound.airise.util.Orange


@Composable
fun ChallengeEditorScreen(
    navController: NavHostController,
    viewModel: ChallengesViewModelImpl,
    initialName: String? = null,
    initialDesc: String? = null,
    onBackClick: () -> Unit
) {
    //for text fields
    var name by remember { mutableStateOf(TextFieldValue(initialName ?: "")) }
    var desc by remember { mutableStateOf(TextFieldValue(initialDesc ?: "")) }
    val isEdit = initialName != null || initialDesc != null

    Scaffold(
        backgroundColor = BgBlack,
        //top bar for back arrow
        topBar = {
            TopAppBar(
                backgroundColor = BgBlack,
                contentColor = White,
                elevation = 0.dp,
                title = { },
                //returns to challenge list
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Orange
                        )
                    }
                }
            )
        },
        //for save button
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                //save is to update challenges in vm then navs back
                Button(
                    onClick = {
                        if (name.text.isNotBlank()) {
                            // viewModel.addChallenge(name.text, desc.text)
                            navController.popBackStack() // go back to the Challenges list
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DeepBlue,
                        contentColor = White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 150.dp),
        ) {
            //title change
            Text(
                text = if (isEdit) "Edit Challenge" else "Create New Challenge",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 70.dp)
            )

            // Challenge name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Challenge Name", color = White) },
                textStyle = LocalTextStyle.current.copy(color = White, fontSize = 20.sp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = White,
                    cursorColor = White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .border(2.dp, White, RoundedCornerShape(16.dp))
                    .padding(bottom = 28.dp)
            )

            Spacer(Modifier.height(30.dp))

            // Challenge description
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                placeholder = { Text("Challenge Description", color = White) },
                textStyle = LocalTextStyle.current.copy(color = White, fontSize = 18.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = White,
                    cursorColor = White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(2.dp, White, RoundedCornerShape(16.dp))
            )
        }
    }
}
