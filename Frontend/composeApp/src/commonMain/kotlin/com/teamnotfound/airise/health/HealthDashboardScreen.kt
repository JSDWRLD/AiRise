package com.teamnotfound.airise.health

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.khealth.KHealth
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.NeonGreen
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val SHOWCASE_MODE = false // flip for demos

@Composable
fun HealthDashboardScreen(
    kHealth: KHealth,
    onBackClick: () -> Unit
) {
    val provider = remember { HealthDataProvider(kHealth) }
    val viewModel = remember { HealthDashboardViewModel(provider) }

    val healthData by viewModel.healthData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(Unit) { viewModel.loadData() }


    val allZero by remember(healthData) {
        mutableStateOf(
            healthData?.let {
                it.caloriesBurned == 0 && it.steps == 0 && it.hydration == 0.0 && it.sleep == 0.0
            } != false
        )
    }

    // Check if we need permissions based on whether we could read health data
    // If healthData is null, the read failed (likely no permissions)
    // If healthData exists (even with 0 values), the read succeeded (we have permissions)
    val needsPermissions by remember(healthData, isLoading) {
        mutableStateOf(!isLoading && healthData == null)
    }

    LaunchedEffect(error) {
        error?.let {
            scaffoldState.snackbarHostState.showSnackbar(
                message = "Error: $it",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                HealthTopBar(
                    title = "Health Dashboard",
                    subtitle = if (isLoading) "Syncing your latest data…" else if (needsPermissions) "Permissions needed to show metrics" else "All caught up",
                    connected = !needsPermissions && !allZero,
                    isLoading = isLoading,
                    onBackClick = onBackClick,
                    onPrimaryAction = {
                        if (needsPermissions) viewModel.requestAndLoadData() else viewModel.requestAndLoadData()
                    },
                    showPrimaryAction = !needsPermissions // keep header minimal when banner is visible
                )
                ConnectionBar(
                    connected = !needsPermissions && !allZero,
                    isLoading = isLoading,
                    onSyncClick = { viewModel.requestAndLoadData() },
                    onPermissionsClick = { viewModel.requestAndLoadData() },
                    showActionButton = !needsPermissions // hide any extra CTA if banner is visible
                )
            }
        },
        backgroundColor = BgBlack,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    backgroundColor = Color(0xFF122224),
                    contentColor = White,
                    shape = RoundedCornerShape(12.dp),
                    snackbarData = data
                )
            }
        }
    ) { padding ->
        // Backdrop gradient + blobs behind content
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(BgBlack, BgBlack.copy(alpha = 0.6f))))
        ) {
            FloatingBlobs(Modifier.matchParentSize())

            val gridState = rememberLazyGridState()

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                when {
                    isLoading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) { LoadingSection() }
                    }
                    isBlocked -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            PermissionsBanner(
                                isBlocked = true,
                                onGrant = { /* no-op when blocked */ }
                            )
                        }
                    }
                    needsPermissions -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            PermissionsBanner(
                                isBlocked = false,
                                onGrant = { viewModel.requestAndLoadData() }
                            )
                        }
                    }
                    else -> {
                        // Metrics as grid cells
                        item {
                            MetricCard(
                                title = "Steps",
                                value = (healthData?.steps ?: 0).formatWithCommas(),
                                subtitle = "Goal 10k",
                                icon = { Icon(Icons.Default.DirectionsWalk, null, tint = White) },
                                accent = Cyan,
                                progress = min(1f, (healthData?.steps ?: 0) / 10_000f)
                            )
                        }
                        item {
                            MetricCard(
                                title = "Calories",
                                value = (healthData?.caloriesBurned ?: 0).formatWithCommas(),
                                subtitle = "Active kcal",
                                icon = { Icon(Icons.Default.LocalFireDepartment, null, tint = White) },
                                accent = Orange,
                                progress = min(1f, (healthData?.caloriesBurned ?: 0) / 500f)
                            )
                        }
                        item {
                            MetricCard(
                                title = "Hydration",
                                value = "${((healthData?.hydration ?: 0.0).coerceAtLeast(0.0)).toOneDecimal()} L",
                                subtitle = "Goal 2.5 L",
                                icon = { Icon(Icons.Default.Opacity, null, tint = White) },
                                accent = DeepBlue,
                                progress = min(1f, (((healthData?.hydration ?: 0.0) / 2.5f).toFloat()))
                            )
                        }
                        item {
                            MetricCard(
                                title = "Sleep",
                                value = "${((healthData?.sleep ?: 0.0).coerceAtLeast(0.0)).toOneDecimal()} h",
                                subtitle = "Last night",
                                icon = { Icon(Icons.Default.Hotel, null, tint = White) },
                                accent = NeonGreen,
                                progress = min(1f, (((healthData?.sleep ?: 0.0) / 8f).toFloat()))
                            )
                        }

                        if (SHOWCASE_MODE) {
                            // Optional: keep your demo writer visible even without permissions,
                            // or remove this block if undesired.
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PrimaryButton(
                                        label = "Write Sample",
                                        onClick = {
                                            scope.launch {
                                                val ok = viewModel.writeHealthData()
                                                if (!ok) {
                                                    scaffoldState.snackbarHostState.showSnackbar(
                                                        "Failed to write sample data.",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                } else {
                                                    viewModel.requestAndLoadData()
                                                }
                                            }
                                        },
                                        background = Orange,
                                        contentColor = BgBlack,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

/* ----------------------------- UI Sections ----------------------------- */

@Composable
private fun HeaderStatusRow(connected: Boolean, isLoading: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val statusText = if (connected) "Device synced" else "Device not available"
        StatusChip(
            text = statusText,
            color = if (connected) NeonGreen else Orange,
            textColor = if (connected) BgBlack else White
        )

        AnimatedVisibility(visible = isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Cyan
                )
                Spacer(Modifier.width(8.dp))
                Text("Syncing…", color = White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Orange)
        Spacer(Modifier.height(12.dp))
        Text("Fetching your latest health data…", color = White.copy(alpha = 0.9f))
    }
}

/* ----------------------------- Single CTA Banner ----------------------------- */

@Composable
private fun PermissionsBanner(
    isBlocked: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isBlocked) Color(0xFF1A1212) else Color(0xFF0E1D1E)
    val accent = if (isBlocked) Orange else DeepBlue
    val title = if (isBlocked) "Health Access Disabled" else "Enable Health Permissions"
    val subtitle = if (isBlocked) {
        "Access to steps, calories, hydration and sleep is turned off in system settings."
    } else {
        "To show steps, calories, hydration and sleep, grant access to your health provider."
    }

    Card(
        backgroundColor = bg,
        shape = RoundedCornerShape(20.dp),
        elevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, color = White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = White.copy(alpha = 0.75f), fontSize = 14.sp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isBlocked) {
                    // Subtle, non-clickable status chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Orange.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Orange)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Blocked in Settings",
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Bullet(text = "We only read recent activity")
                    Bullet(text = "You can revoke anytime")
                }
            }

            if (!isBlocked) {
                PrimaryButton(
                    label = "Grant Permissions",
                    onClick = onGrant,
                    background = accent,
                    contentColor = White,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Small footer hint (no CTA button)
                Text(
                    "Re-enable permissions in Settings to continue.",
                    color = White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Cyan)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = White.copy(alpha = 0.8f), fontSize = 13.sp)
    }
}

/**
 * Responsive grid helper kept for clarity; metrics are built inline above.
 */
@Composable
private fun MetricsGrid(
    steps: Int,
    calories: Int,
    hydrationLiters: Double,
    sleepHours: Double
) {
    val gridState = rememberLazyGridState()
    val items = listOf(
        MetricItem(
            title = "Steps",
            value = steps.formatWithCommas(),
            subtitle = "Goal 10k",
            icon = { Icon(Icons.Default.DirectionsWalk, null, tint = White) },
            accent = Cyan,
            progress = min(1f, steps / 10_000f)
        ),
        MetricItem(
            title = "Calories",
            value = calories.formatWithCommas(),
            subtitle = "Active kcal",
            icon = { Icon(Icons.Default.LocalFireDepartment, null, tint = White) },
            accent = Orange,
            progress = min(1f, calories / 500f)
        ),
        MetricItem(
            title = "Hydration",
            value = "${hydrationLiters.toOneDecimal()} L",
            subtitle = "Goal 2.5 L",
            icon = { Icon(Icons.Default.Opacity, null, tint = White) },
            accent = DeepBlue,
            progress = min(1f, (hydrationLiters / 2.5f).toFloat())
        ),
        MetricItem(
            title = "Sleep",
            value = "${sleepHours.toOneDecimal()} h",
            subtitle = "Last night",
            icon = { Icon(Icons.Default.Hotel, null, tint = White) },
            accent = NeonGreen,
            progress = min(1f, (sleepHours / 8f).toFloat())
        )
    )

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 160.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 0.dp)
    ) {
        items(items) { item ->
            MetricCard(
                title = item.title,
                value = item.value,
                subtitle = item.subtitle,
                icon = item.icon,
                accent = item.accent,
                progress = item.progress
            )
        }
    }
}

private data class MetricItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: @Composable () -> Unit,
    val accent: Color,
    val progress: Float
)

/* ----------------------------- Building blocks ----------------------------- */

@Composable
private fun StatusChip(text: String, color: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    background: Color,
    contentColor: Color,
    icon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = background, contentColor = contentColor),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.height(48.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(label)
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    accent: Color,
    progress: Float
) {
    Card(
        backgroundColor = Color(0xFF0F1F20),
        shape = RoundedCornerShape(20.dp),
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, color = White.copy(alpha = 0.75f), fontSize = 12.sp)
                    Text(value, color = White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            ProgressLine(
                progress = progress,
                trackColor = Color(0xFF102426),
                fillColor = accent
            )

            Text(subtitle, color = White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProgressLine(
    progress: Float,
    trackColor: Color,
    fillColor: Color,
    height: Dp = 6.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = LinearEasing)
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .background(fillColor)
        )
    }
}

/* ----------------------------- Background Blobs (centered) ----------------------------- */

@Composable
fun FloatingBlobs(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val angle1 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing))
    )
    val angle2 by infinite.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing))
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val cx = w / 2f
        val cy = h / 2f

        val degToRad = (PI / 180.0)
        val r1 = angle1 * degToRad
        val r2 = angle2 * degToRad

        val orbitX = min(w, h) * 0.18f
        val orbitY = min(w, h) * 0.14f

        val c1x = cx + (sin(r1) * orbitX).toFloat()
        val c1y = cy + (kotlin.math.cos(r1) * orbitY).toFloat()
        val c2x = cx + (sin(r2) * (orbitX * 1.15)).toFloat()
        val c2y = cy + (kotlin.math.cos(r2) * (orbitY * 1.15)).toFloat()

        val base = min(w, h)
        val rBig = base * 0.22f
        val rMid = base * 0.19f
        val rSmall = base * 0.16f

        drawSoftCircle(Orange.copy(alpha = 0.10f), rBig, c1x, c1y)
        drawSoftCircle(DeepBlue.copy(alpha = 0.10f), rMid, c2x, c2y)
        drawSoftCircle(Cyan.copy(alpha = 0.08f), rSmall, (c1x + c2x) / 2f, (c1y + c2y) / 2f)
    }
}

/* ----------------------------- Top connection bar (dedup aware) ----------------------------- */

@Composable
private fun ConnectionBar(
    connected: Boolean,
    isLoading: Boolean,
    onSyncClick: () -> Unit,
    onPermissionsClick: () -> Unit = onSyncClick,
    showActionButton: Boolean = true
) {
    Surface(color = BgBlack, elevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background((if (connected) NeonGreen else Orange).copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (connected) NeonGreen else Orange)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (connected) "Device synced" else "Device not available",
                    color = if (connected) BgBlack else White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.weight(1f))

            when {
                isLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Cyan
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Syncing…", color = Silver, fontSize = 12.sp)
                    }
                }
                showActionButton && connected -> {
                    TextButton(onClick = onSyncClick, shape = RoundedCornerShape(999.dp)) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = White)
                        Spacer(Modifier.width(6.dp))
                        Text("Sync", color = White, fontSize = 14.sp)
                    }
                }
                showActionButton && !connected -> {
                    TextButton(onClick = onPermissionsClick, shape = RoundedCornerShape(999.dp)) {
                        Text("Permissions", color = White, fontSize = 14.sp)
                    }
                }
                else -> {
                    // intentionally empty: we show no extra CTA when the banner is visible
                }
            }
        }
    }
}

@Composable
private fun HealthTopBar(
    title: String,
    subtitle: String,
    connected: Boolean,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onPrimaryAction: () -> Unit,   // kept for binary compatibility (unused)
    showPrimaryAction: Boolean      // kept for binary compatibility (ignored)
) {
    Surface(color = Color.Transparent, elevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            DeepBlue,
                            DeepBlue.copy(alpha = .92f),
                            DeepBlue.copy(alpha = .86f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top row: Back + Title/Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back pill
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = White
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Title + subtitle with status dot
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isLoading -> Cyan
                                        connected -> NeonGreen
                                        else -> Orange
                                    }
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = subtitle,
                            color = White.copy(alpha = 0.82f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Subtle divider under the header for separation from ConnectionBar
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }
    }
}

private fun DrawScope.drawSoftCircle(color: Color, radius: Float, x: Float, y: Float) {
    drawCircle(color = color, radius = radius, center = androidx.compose.ui.geometry.Offset(x, y))
}

private fun Int.formatWithCommas(): String {
    val negative = this < 0
    val digits = kotlin.math.abs(this).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-$grouped" else grouped
}

private fun Double.toOneDecimal(): String {
    val rounded = (this * 10.0).roundToInt() / 10.0
    val s = rounded.toString()
    val dot = s.indexOf('.')
    return if (dot == -1) s + ".0" else {
        val frac = s.substring(dot + 1)
        if (frac.length >= 1) s.substring(0, dot + 2) else s + "0"
    }
}