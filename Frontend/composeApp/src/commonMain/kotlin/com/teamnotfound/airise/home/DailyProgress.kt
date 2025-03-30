package com.teamnotfound.airise.home

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Size
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.NeonGreen
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver

//Displays the daily progress section
@Composable
fun DailyProgressSection(dailyProgressData: DailyProgressData, isLoading: Boolean) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepBlue.copy(alpha = 0.4f),
                        Orange.copy(alpha = 0.4f)
                    )
                )
            )
            .padding(10.dp)
    ) {
        Text(
            text = "Daily Progress",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(70.dp)
        ) {
            if(isLoading){
                CircularProgressIndicator(
                    color = DeepBlue
                )
            }else{
                Box(
                    modifier = Modifier.size(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 30f
                        val gap = 8f

                        listOf(0, 1, 2).forEach { index ->
                            drawCircle(
                                color = Silver,
                                radius = (size.minDimension / 2) - (strokeWidth * (index + 1)) - (gap * index),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        drawProgressArc(NeonGreen, dailyProgressData.sleepProgress, 0, strokeWidth, gap)
                        drawProgressArc(Orange, dailyProgressData.workoutProgress, 1, strokeWidth, gap)
                        drawProgressArc(Cyan, dailyProgressData.hydrationProgress, 2, strokeWidth, gap)
                    }

                    Text(
                        text = "${dailyProgressData.totalProgress.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Silver,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Legend(NeonGreen, "Sleep: ", dailyProgressData.sleepProgress.toInt())
                Spacer(modifier = Modifier.height(6.dp))
                Legend(Orange, "Workouts: ", dailyProgressData.workoutProgress.toInt())
                Spacer(modifier = Modifier.height(6.dp))
                Legend(Cyan, "Hydration: ", dailyProgressData.hydrationProgress.toInt())
            }
        }
    }
}

//Displays the legend for the progress
@Composable
fun Legend(color: Color, label: String, percentage: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = label, fontSize = 12.sp, color = Color.White)
        Text(text = "$percentage%", fontSize = 12.sp, color = Silver)
    }
}

//Helper function to draw the progress arcs
fun DrawScope.drawProgressArc(
    color: Color,
    percentage: Float,
    index: Int,
    strokeWidth: Float,
    gap: Float
) {
    val radiusOffset = (strokeWidth * (index + 1)) + (gap * index)
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = (percentage / 100f) * 360f,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        topLeft = Offset(radiusOffset, radiusOffset),
        size = Size(size.width - radiusOffset * 2, size.height - radiusOffset * 2)
    )
}