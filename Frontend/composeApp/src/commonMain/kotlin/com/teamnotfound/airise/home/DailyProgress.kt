package com.teamnotfound.airise.home

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Size
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver

//Displays the daily progress section
@Composable
fun DailyProgressSection() {
    val sleepPercentage = 80f
    val workoutPercentage = 75f
    val hydrationPercentage = 60f
    val totalProgress = (sleepPercentage + workoutPercentage + hydrationPercentage) / 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(12.dp))
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
            Box(
                modifier = Modifier.size(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 30f
                    val gap = 8f

                    listOf(0, 1, 2).forEach { index ->
                        drawCircle(
                            color = Color.LightGray,
                            radius = (size.minDimension / 2) - (strokeWidth * (index + 1)) - (gap * index),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    drawProgressArc(Color.Green, sleepPercentage, 0, strokeWidth, gap)
                    drawProgressArc(color = Orange, workoutPercentage, 1, strokeWidth, gap)
                    drawProgressArc(Color.Blue, hydrationPercentage, 2, strokeWidth, gap)
                }

                Text(
                    text = "${totalProgress.toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Silver,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Legend(Color.Green, "Sleep: ", sleepPercentage.toInt())
                Spacer(modifier = Modifier.height(6.dp))
                Legend(color = Orange, "Workouts: ", workoutPercentage.toInt())
                Spacer(modifier = Modifier.height(6.dp))
                Legend(Color.Blue, "Hydration: ", hydrationPercentage.toInt())
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