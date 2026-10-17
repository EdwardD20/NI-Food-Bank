package org.danleys.barcode

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun BottomBarLayout(
    modifier: Modifier = Modifier,
    websiteLabel: String,
    facebookLabel: String,
    onWebsiteTap: () -> Unit = {},
    onFacebookTap: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val barHeight = 56.dp
    val backgroundColor = Color(0xFF1B5E20) // Dark Green

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = barHeight)
        ) {
            content()
        } // Box

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(barHeight)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            BottomShadedBarGeometry(
                heightDp = barHeight,
                modifier = Modifier.matchParentSize()
            ) // BottomShadedBarGeometry

            BottomMessageSwitcher(
                heightDp = barHeight,
                websiteLabel = websiteLabel,
                facebookLabel = facebookLabel,
                onWebsiteTap = onWebsiteTap,
                onFacebookTap = onFacebookTap,
                modifier = Modifier.matchParentSize()
            ) // BottomMessageSwitcher
        } // Box
    } // Box
} // fun BottomBarLayout

@Composable
fun BottomShadedBarGeometry(
    heightDp: Dp,
    modifier: Modifier = Modifier
) {
    val base = Color(0xFF003300) // Even darker green for the bar
    val divider = Color.White.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .background(base)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        ) // Box

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(divider)
                .align(Alignment.TopCenter)
        ) // Box
    } // Box
} // fun BottomShadedBarGeometry

@Composable
fun BottomMessageSwitcher(
    heightDp: Dp,
    modifier: Modifier = Modifier,
    websiteLabel: String,
    facebookLabel: String,
    onWebsiteTap: () -> Unit,
    onFacebookTap: () -> Unit
) {
    val textColor = Color.White
    val bgColor = Color.Transparent

    var showFirst by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            showFirst = !showFirst
        } // while
    } // LaunchedEffect

    fun dynamicFontSize(text: String): Float {
        return when {
            text.length <= 20 -> 22f
            text.length <= 30 -> 18f
            else -> 14f
        } // when
    } // fun dynamicFontSize

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .background(bgColor)
            .clickable {
                if (showFirst) onWebsiteTap() else onFacebookTap()
            } // clickable
    ) {
        if (showFirst) {
            Text(
                text = websiteLabel,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = dynamicFontSize(websiteLabel).sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp)
            ) // Text
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                ) // Icon

                Text(
                    text = facebookLabel,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = dynamicFontSize(facebookLabel).sp,
                    maxLines = 1
                ) // Text
            } // Row
        } // if
    } // Box
} // fun BottomMessageSwitcher

@Composable
fun GraphicDialog(
    onClose: () -> Unit,
    @StringRes title: Int,
    @DrawableRes graphic: Int
) {
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            color = Color(0xFF1B5E20) // Match theme
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B5E20))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.close_button),
                            color = Color.White
                        ) // Text
                    } // TextButton

                    ZoomableImage(
                        imageRes = graphic,
                        contentDescription = stringResource(id = title),
                        modifier = if (isLandscape)
                            Modifier.fillMaxHeight()
                        else
                            Modifier.fillMaxWidth()
                    ) // ZoomableImage
                } // Column
            } // Box
        } // Surface
    } // Dialog
} // fun GraphicDialog

@Composable
fun ZoomableImage(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 4f

    val gestureModifier = Modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                val newOffset = if (newScale > minScale) offset + pan else Offset.Zero
                scale = newScale
                offset = newOffset
            } // detectTransformGestures
        } // pointerInput
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    if (scale > minScale + 0.01f) {
                        scale = minScale
                        offset = Offset.Zero
                    } else {
                        scale = 2f
                    } // if
                } // onDoubleTap
            ) // detectTapGestures
        } // pointerInput
        .graphicsLayer {
            translationX = offset.x
            translationY = offset.y
            scaleX = scale
            scaleY = scale
        } // graphicsLayer

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .then(gestureModifier)
            .fillMaxSize()
    ) // Image
} // fun ZoomableImage

@Composable
fun MediumWidthButton(
    text: String,
    onClick: () -> Unit,
    icon: Int? = null,
    enabled: Boolean = true
) {
    val screenDp = LocalConfiguration.current.screenWidthDp.dp
    val maxW = minOf(280.dp, screenDp * 0.6f)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .widthIn(min = 160.dp, max = maxW)
            .padding(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.9f), // Light button on dark green
            contentColor = Color(0xFF1B5E20) // Green text
        ),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_facebook),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 8.dp)
            ) // Icon
        } // if

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        ) // Text
    } // Button
} // fun MediumWidthButton
