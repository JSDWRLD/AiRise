package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.util.*


/*
 * Generic scrollable colum
 * Used for Age, Height, and Weight selection
 */
@Composable
fun ScrollableColumnSelection(
    label: String?,
    items: List<Int>,
    selectedItem: Int?,
    onItemSelected: (Int) -> Unit
) {
    // center selected item
    val listState = rememberLazyListState()
    LaunchedEffect(selectedItem) {
        selectedItem?.let {
            val index = items.indexOf(it)
            if (index >= 0) {
                val centerPosition = maxOf(index - 2, 0)
                listState.scrollToItem(centerPosition)
            }
        }
    }
    // scroll
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // label check
        if (label != null) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = White,
                fontWeight = FontWeight.SemiBold
            )
        }
        // list wheel
        Box(
            modifier = Modifier
                .height(300.dp)
                .width(100.dp)
                .background(DeepBlue, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Text(
                        text = item.toString(),
                        fontSize = if (isSelected) 32.sp else 24.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Orange else White,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onItemSelected(item) }
                    )
                }
            }
        }
    }
}
