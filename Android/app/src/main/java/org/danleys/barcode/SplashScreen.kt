package org.danleys.barcode

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val missionStatement = "Providing nutritious food and resources for our neighbors with dignity, equity and convenience."
    var timeLeft by remember {
        mutableIntStateOf(if (BuildConfig.DEBUG) 2 else 4)
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000.milliseconds)
            timeLeft--
        } // while (timeLeft > 0)
        onTimeout()
    } // LaunchedEffect

    val configuration = LocalConfiguration.current
    val backgroundColor = Color(0xFF1B5E20) // Dark Green

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo at the top
            Image(
                painter = painterResource(id = R.drawable.neighborsempowered),
                contentDescription = "NIFB Logo",
                modifier = Modifier
                    .width(375.dp)
                    .height(163.dp),
                contentScale = ContentScale.Fit
            ) // end Image

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = missionStatement,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) // Text (Mission statement)

        } // Column (Content)

        // Prominent, non-subtle animated countdown timer
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        val timerFontSize = (configuration.screenHeightDp * 0.10).sp

        Text(
            text = timeLeft.toString(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            style = TextStyle(
                fontSize = timerFontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Red, Color(0xFFFFa500), Color.Yellow), // Red, Orange, Yellow
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                ),
                shadow = Shadow(
                    color = Color.Red.copy(alpha = 0.8f),
                    offset = Offset(0f, 0f),
                    blurRadius = 30f
                )
            )
        )
    } // Box
} // SplashScreen
