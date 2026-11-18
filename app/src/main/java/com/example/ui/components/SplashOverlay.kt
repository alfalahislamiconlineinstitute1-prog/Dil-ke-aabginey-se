package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SplashDeep
import com.example.ui.theme.SplashGlow
import com.example.ui.theme.SplashRingInner
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(durationMillis = 700)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "splash_spin")
        val ringRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ring_rotation"
        )

        var titleStarted by remember { mutableStateOf(false) }
        var subStarted by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(200L)
            titleStarted = true
            delay(300L)
            subStarted = true
        }

        val riseEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
        val titleProgress by animateFloatAsState(
            targetValue = if (titleStarted) 1f else 0f,
            animationSpec = tween(durationMillis = 1000, easing = riseEasing),
            label = "splash_title_rise"
        )
        val subProgress by animateFloatAsState(
            targetValue = if (subStarted) 1f else 0f,
            animationSpec = tween(durationMillis = 1000, easing = riseEasing),
            label = "splash_sub_rise"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_overlay")
                .background(SplashDeep),
            contentAlignment = Alignment.Center
        ) {
            // Radial glow centered at (50%, 40%) matching CSS radial-gradient(circle at 50% 40%,#0f7a58,#06241b 70%)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.4f)
                val radius = maxOf(size.width, size.height) * 0.7f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(SplashGlow, SplashDeep),
                        center = center,
                        radius = radius
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Conic spinning ring (150.dp) + counter-rotating ❖ symbol
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = ringRotation }
                    ) {
                        val sweepBrush = Brush.sweepGradient(
                            0.0f to GoldAccent,
                            0.30f to Color.Transparent,
                            0.55f to GoldAccent,
                            0.80f to Color.Transparent,
                            1.0f to GoldAccent
                        )
                        drawCircle(brush = sweepBrush)
                    }

                    Box(
                        modifier = Modifier
                            .size(142.dp) // inset 4dp on each side
                            .clip(CircleShape)
                            .background(SplashRingInner),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❖",
                            color = GoldAccent,
                            fontSize = 56.sp,
                            lineHeight = 56.sp,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = -ringRotation
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "دل کے آبگینے سے",
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    lineHeight = 72.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(titleProgress)
                        .graphicsLayer {
                            translationY = (1f - titleProgress) * 20.dp.toPx()
                        }
                )

                Text(
                    text = "حمد · نعت · نظمیں",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 32.sp,
                    color = GoldAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(subProgress)
                        .graphicsLayer {
                            translationY = (1f - subProgress) * 20.dp.toPx()
                        }
                )
            }
        }
    }
}
