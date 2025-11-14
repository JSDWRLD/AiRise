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
import kotlin.math.floor
import kotlin.math.roundToInt

// Displays the daily progress section
@Composable
fun DailyProgressSection(
    dailyProgressData: DailyProgressData,
    isLoaded: Boolean,
    lastNightSleepHours: Double? = null,
    isHealthSyncAvailable: Boolean
) {

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
            if (!isLoaded) {
                CircularProgressIndicator(color = DeepBlue)
            } else {
                Box(modifier = Modifier.size(140.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 30f  // px
                        val gap = 8f          // px

                        // Draw background circles based on health sync availability
                        if (isHealthSyncAvailable) {
                            // Show all three rings when health sync is available
                            listOf(0, 1, 2).forEach { index ->
                                val radius = (size.minDimension / 2f) -
                                        (strokeWidth * (index + 1)) -
                                        (gap * index)

                                if (radius > 0f) {
                                    drawCircle(
                                        color = Silver,
                                        radius = radius,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt) // caps don't matter on closed circle
                                    )
                                }
                            }

                            // Draw progress arcs
                            drawProgressArc(NeonGreen, dailyProgressData.sleepProgress.safePercent(), 0, strokeWidth, gap)
                            drawProgressArc(Orange, dailyProgressData.caloriesProgress.safePercent(), 1, strokeWidth, gap)
                            drawProgressArc(Cyan, dailyProgressData.hydrationProgress.safePercent(), 2, strokeWidth, gap)
                        } else {
                            // Hide sleep ring when health sync not available - only show 2 rings
                            listOf(0, 1).forEach { index ->
                                val radius = (size.minDimension / 2f) -
                                        (strokeWidth * (index + 1)) -
                                        (gap * index)

                                if (radius > 0f) {
                                    drawCircle(
                                        color = Silver,
                                        radius = radius,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                    )
                                }
                            }

                            // Draw only calorie intake and hydration arcs
                            drawProgressArc(Orange, dailyProgressData.caloriesProgress.safePercent(), 0, strokeWidth, gap)
                            drawProgressArc(Cyan, dailyProgressData.hydrationProgress.safePercent(), 1, strokeWidth, gap)
                        }
                    }

                    Text(
                        text = "${dailyProgressData.totalProgress.safePercent().toInt()}%",
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
                // Only show sleep legend if health sync is available
                if (isHealthSyncAvailable) {
                    Legend(NeonGreen, "Sleep: ", dailyProgressData.sleepProgress.safePercent().toInt())
                    if (isLoaded && lastNightSleepHours != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Last night: ${formatHours(lastNightSleepHours)}",
                            fontSize = 11.sp,
                            color = Silver
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Legend(Orange, "Calorie Intake: ", dailyProgressData.caloriesProgress.safePercent().toInt())
                Spacer(modifier = Modifier.height(6.dp))
                Legend(Cyan, "Hydration: ", dailyProgressData.hydrationProgress.safePercent().toInt())
            }
        }
    }
}

// Displays the legend for the progress
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

// Helper function to draw the progress arcs (ORIGINAL geometry, just safer)
fun DrawScope.drawProgressArc(
    color: Color,
    percentage: Float,
    index: Int,
    strokeWidth: Float,
    gap: Float
) {
    val pct = percentage.safePercent()

    val radiusOffset = (strokeWidth * (index + 1)) + (gap * index)
    val w = size.width - radiusOffset * 2f
    val h = size.height - radiusOffset * 2f
    if (w <= 0f || h <= 0f || pct <= 0f) return

    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = (pct / 100f) * 360f,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        topLeft = Offset(radiusOffset, radiusOffset),
        size = Size(w, h)
    )
}

// Helper for formatting sleep hours
private fun formatHours(hours: Double): String {
    val h = floor(hours).toInt()
    val m = ((hours - h) * 60).roundToInt().coerceIn(0, 59)
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

// Clamp+clean: avoid NaN/Infinity/overflows that can render as full 360°
private fun Float.safePercent(): Float {
    if (!isFinite()) return 0f
    val v = if (this in 0f..1f) this * 100f else this
    return v.coerceIn(0f, 100f)
}