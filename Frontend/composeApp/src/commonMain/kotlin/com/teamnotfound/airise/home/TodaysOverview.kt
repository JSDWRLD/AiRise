package com.teamnotfound.airise.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun TodaysOverview (overview: String, isLoading: Boolean) {
    Column {
        Text(
            text = "Today's Overview",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = White
        )
        if(isLoading){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = DeepBlue
            )
        }else{
            Text(
                modifier = Modifier.padding(horizontal = 0.015.dp, vertical = 0.025.dp),
                text = overview,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Silver
            )
        }
    }
}