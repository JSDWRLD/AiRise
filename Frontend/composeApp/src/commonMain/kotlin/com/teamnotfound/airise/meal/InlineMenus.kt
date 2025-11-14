package com.teamnotfound.airise.meal

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Cyan
import com.teamnotfound.airise.util.White
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

/**
 * Keeps a reference to an anchor's LayoutCoordinates.
 * Use rememberInlineMenuAnchor() and Modifier.inlineMenuAnchor(anchor) on the clickable caller.
 */
class InlineMenuAnchor {
    var coords by mutableStateOf<LayoutCoordinates?>(null)
}

@Composable
fun rememberInlineMenuAnchor(): InlineMenuAnchor = remember { InlineMenuAnchor() }

fun Modifier.inlineMenuAnchor(anchor: InlineMenuAnchor): Modifier =
    this.onGloballyPositioned { anchor.coords = it }

/** Controller to open/close a single shared inline menu. */
class InlineMenuController {
    private val _state = mutableStateOf<InlineMenuState?>(null)
    val state: State<InlineMenuState?> get() = _state

    fun show(
        anchor: InlineMenuAnchor,
        matchAnchorWidth: Boolean = false,
        content: @Composable ColumnScope.() -> Unit
    ) {
        val coords = anchor.coords ?: return
        _state.value = InlineMenuState(coords, matchAnchorWidth, content)
    }

    fun hide() { _state.value = null }
}

data class InlineMenuState(
    val anchorCoords: LayoutCoordinates,
    val matchAnchorWidth: Boolean,
    val content: @Composable ColumnScope.() -> Unit
)

/**
 * A full-screen, in-tree overlay that positions the dropdown **under** the anchor.
 * - Dismisses by tapping anywhere outside
 * - Works on iOS/Android (no Popup/DropdownMenu/LocalContext)
 */
@Composable
fun InlineMenuHost(
    controller: InlineMenuController,
    screenPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state = controller.state.value ?: return
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding)
            .zIndex(9999f)
            .background(Color.Black.copy(alpha = 0.001f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { controller.hide() },
        contentAlignment = Alignment.TopStart
    ) {
        val anchorPos = state.anchorCoords.localToRoot(Offset.Zero)
        val anchorSize = state.anchorCoords.size

        val xDp = with(density) { anchorPos.x.toDp() }
        val yDp = with(density) { (anchorPos.y + anchorSize.height).toDp() }
        val anchorWidthDp = with(density) { anchorSize.width.toDp() }

        // If we’re matching width, clamp left so the full menu fits inside screen.
        val left = if (state.matchAnchorWidth) {
            val maxLeft = this.maxWidth - anchorWidthDp - 8.dp
            xDp.coerceIn(8.dp, maxLeft.coerceAtLeast(8.dp))
        } else {
            xDp.coerceIn(8.dp, this.maxWidth - 8.dp)
        }
        val top  = yDp.coerceIn(0.dp, this.maxHeight - 8.dp)

        Column(
            modifier = Modifier
                .offset(x = left, y = top)
                .then(
                    if (state.matchAnchorWidth)
                        Modifier.width(anchorWidthDp)
                    else
                        Modifier // wrap content
                )
                .background(BgBlack, RoundedCornerShape(12.dp))
                .border(1.dp, Cyan.copy(alpha = .35f), RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume taps inside */ }
        ) {
            state.content(this)
        }
    }
}

/** Simple reusable row for menu items (kept from your original look). */
@Composable
fun InlineMenuItem(
    text: String,
    tint: Color = White,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(Color.Transparent)
            .then(Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text, color = tint)
    }
}
