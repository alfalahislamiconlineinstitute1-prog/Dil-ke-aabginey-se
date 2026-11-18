package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentDayGold
import com.example.ui.theme.AccentDayGreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.BgDay
import com.example.ui.theme.BottomNavBg
import com.example.ui.theme.BottomNavDayBg
import com.example.ui.theme.Card2Dark
import com.example.ui.theme.Card2Day
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDay
import com.example.ui.theme.CoverDark
import com.example.ui.theme.CoverDescMint
import com.example.ui.theme.CoverLight
import com.example.ui.theme.CoverNoteMint
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldTextOnButton
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.LineDark
import com.example.ui.theme.LineDay
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TextDayMuted
import com.example.ui.theme.TextDayPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    hasSavedProgress: Boolean,
    latestBookmarkTitle: String?,
    isFehristOpen: Boolean,
    isDayMode: Boolean,
    onToggleDayNight: () -> Unit,
    onStartReading: () -> Unit,
    onOpenFehrist: () -> Unit,
    onOpenLatestBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    BackHandler(enabled = !isFehristOpen && pagerState.currentPage == 1) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "home_sunburst")
    val sunburstRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst_rot"
    )

    val screenBg by animateColorAsState(
        targetValue = if (isDayMode) BgDay else BgDark,
        animationSpec = tween(350),
        label = "home_bg"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // Top radial emerald glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GreenPrimary.copy(alpha = if (isDayMode) 0.14f else 0.55f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = size.height * 0.65f
                )
            )
        }

        // Rotating 700dp Sunburst (#home:before) masked radially from 20% to 68%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 150.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier
                    .size(700.dp)
                    .offset(y = (-350).dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        rotationZ = sunburstRotation
                    }
            ) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val rayColor = GoldAccent.copy(alpha = if (isDayMode) 0.11f else 0.06f)
                for (deg in 0 until 360 step 12) {
                    drawArc(
                        color = rayColor,
                        startAngle = deg.toFloat(),
                        sweepAngle = 6f,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f)
                    )
                }
                // Radial alpha mask: opaque up to 20%, fading to transparent at 68%
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to Color.Black,
                        0.20f to Color.Black,
                        0.68f to Color.Transparent,
                        1.0f to Color.Transparent,
                        center = center,
                        radius = radius
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HomeDayNightTopBar(
                isDayMode = isDayMode,
                onToggleDayNight = onToggleDayNight
            )

            // Main 2-page RTL Horizontal Pager (#hp)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> HomeCoverPage(
                        hasSavedProgress = hasSavedProgress,
                        latestBookmarkTitle = latestBookmarkTitle,
                        isDayMode = isDayMode,
                        onStartReading = onStartReading,
                        onOpenFehrist = onOpenFehrist,
                        onOpenLatestBookmark = onOpenLatestBookmark
                    )
                    1 -> HomePublisherPage(
                        isVisible = pagerState.currentPage == 1,
                        isDayMode = isDayMode
                    )
                }
            }

            // Bottom Navigation Pill (#nv): Opens "کتاب کے بارے میں" (Page 1) or returns to Cover (Page 0)
            HomeBottomNavBar(
                isAboutPage = pagerState.currentPage == 1,
                isActive = !isFehristOpen && pagerState.currentPage == 1,
                isDayMode = isDayMode,
                onTogglePage = {
                    val targetPage = if (pagerState.currentPage == 0) 1 else 0
                    scope.launch { pagerState.animateScrollToPage(targetPage) }
                }
            )
        }
    }
}

@Composable
private fun HomeDayNightTopBar(
    isDayMode: Boolean,
    onToggleDayNight: () -> Unit
) {
    val trackBg by animateColorAsState(
        targetValue = if (isDayMode) Card2Day else CardDark.copy(alpha = 0.85f),
        animationSpec = tween(250),
        label = "day_night_track"
    )
    val trackBorder by animateColorAsState(
        targetValue = if (isDayMode) LineDay else LineDark,
        animationSpec = tween(250),
        label = "day_night_border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
                .clip(RoundedCornerShape(99.dp))
                .clickable(onClick = onToggleDayNight)
                .semantics {
                    stateDescription = if (isDayMode) "دن موڈ فعال" else "رات موڈ فعال"
                }
                .testTag("home_theme_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(trackBg)
                    .border(1.dp, trackBorder, RoundedCornerShape(99.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Day segment
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isDayMode) AccentDayGreen else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LightMode,
                        contentDescription = "دن موڈ",
                        tint = if (isDayMode) TextPrimary else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "دن",
                        fontFamily = UrduNastaliqFontFamily,
                        fontWeight = if (isDayMode) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 22.sp,
                        color = if (isDayMode) TextPrimary else TextMuted
                    )
                }

                // Night segment
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (!isDayMode) GoldAccent else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = "رات موڈ",
                        tint = if (!isDayMode) GoldTextOnButton else TextDayMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "رات",
                        fontFamily = UrduNastaliqFontFamily,
                        fontWeight = if (!isDayMode) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 22.sp,
                        color = if (!isDayMode) GoldTextOnButton else TextDayMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCoverPage(
    hasSavedProgress: Boolean,
    latestBookmarkTitle: String?,
    isDayMode: Boolean,
    onStartReading: () -> Unit,
    onOpenFehrist: () -> Unit,
    onOpenLatestBookmark: () -> Unit
) {
    // Sequence matching HTML delays:
    // Cover pop: delay 900ms, 1000ms cubic-bezier(.2,1.1,.3,1)
    // Shimmer: delay 1900ms, 1400ms
    // Float: delay 2000ms, 6s infinite
    // Credits rise: delay 1300ms, 900ms
    // Start btn up: delay 1400ms, 700ms
    // Index btn up: delay 1550ms, 700ms
    val coverPop = remember { Animatable(0f) }
    val shimmer = remember { Animatable(-0.6f) }
    val startBtnUp = remember { Animatable(0f) }
    val indexBtnUp = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            delay(900L)
            coverPop.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = CubicBezierEasing(0.2f, 1.1f, 0.3f, 1f)
                )
            )
        }
        launch {
            delay(1400L)
            startBtnUp.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1550L)
            indexBtnUp.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1900L)
            shimmer.animateTo(1.3f, tween(1400, easing = FastOutSlowInEasing))
        }
    }

    val floatInfinite = rememberInfiniteTransition(label = "cover_float")
    val floatPhase by floatInfinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_phase"
    )
    // Start gentle float smoothly after coverPop completes
    val floatY = if (coverPop.value > 0.95f) (-10f * floatPhase) else 0f
    val floatRot = if (coverPop.value > 0.95f) (0.6f * floatPhase) else 0f

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val viewportHeightPx = with(density) { maxHeight.roundToPx() }
        val topPaddingPx = with(density) { 4.dp.roundToPx() }
        val coverToButtonsGapPx = with(density) { 12.dp.roundToPx() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Layout(
                content = {
                    // Child 0: Book Cover (.cvw / .cov2)
                    val coverShape = RoundedCornerShape(
                        topStart = 6.dp,
                        bottomStart = 6.dp,
                        topEnd = 14.dp,
                        bottomEnd = 14.dp
                    )
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .widthIn(max = 430.dp)
                            .graphicsLayer {
                                val p = coverPop.value
                                alpha = p.coerceIn(0f, 1f)
                                val s = 0.9f + 0.1f * p
                                scaleX = s
                                scaleY = s
                                translationY = ((1f - p) * 24.dp.toPx()) + floatY.dp.toPx()
                                rotationZ = floatRot
                            }
                .shadow(
                    elevation = 24.dp,
                    shape = coverShape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .clip(coverShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(CoverLight, CoverDark),
                        start = Offset.Zero,
                        end = Offset(600f, 1000f)
                    )
                )
                .border(1.dp, GoldAccent.copy(alpha = 0.35f), coverShape)
                .drawBehind {
                    // Left book spine inner shadow (-6px 0 0 rgba(0,0,0,.35) inset)
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset.Zero,
                        size = Size(6.dp.toPx(), size.height)
                    )
                    // Inner gold border (.cov2:before inset 9dp)
                    val inset = 9.dp.toPx()
                    drawRoundRect(
                        color = GoldAccent.copy(alpha = 0.7f),
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2f, size.height - inset * 2f),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
        ) {
            val coverW = maxWidth
            val titleSize = (coverW.value * 0.105f).coerceIn(30f, 48f).sp
            val subSize = (coverW.value * 0.038f).coerceIn(13f, 16f).sp
            val authorSize = (coverW.value * 0.045f).coerceIn(15f, 19f).sp
            val descSize = (coverW.value * 0.030f).coerceIn(10.5f, 12.5f).sp
            val pubSize = (coverW.value * 0.052f).coerceIn(17f, 21f).sp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 26.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_maktaba),
                    contentDescription = "مکتبہ مسیح الامت",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .width(86.dp)
                        .padding(bottom = 2.dp)
                )

                Text(
                    text = "دل کے آبگینے سے",
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = titleSize,
                    lineHeight = titleSize * 2.15f,
                    color = GoldAccent,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = "مجموعۂ حمد و نعت و نظم",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = subSize,
                    lineHeight = subSize * 2.1f,
                    color = MintAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "نتیجۂ فکر:",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 28.sp,
                    color = CoverNoteMint,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "حضرت مولانا مفتی محمد شعیب اللہ خان صاحب مفتاحی دامت برکاتہم",
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = authorSize,
                    lineHeight = authorSize * 2.25f,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "بانی و مہتمم الجامعۃ الاسلامیہ مسیح العلوم بنگلور\nو خلیفہ حضرت اقدس شاہ مفتی مظفر حسین صاحب رحمۃ اللہ علیہ ناظم مظاہر علوم وقف سہارنپور",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = descSize,
                    lineHeight = descSize * 2.2f,
                    color = CoverDescMint,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dashed gold divider
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                ) {
                    drawLine(
                        color = GoldAccent.copy(alpha = 0.5f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "مکتبہ مسیح الامت دیوبند و بنگلور",
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = pubSize,
                    lineHeight = pubSize * 2.15f,
                    color = GoldAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ڈیجیٹل کریٹر و ایپ ڈیولپر: فرید احمد عفی عنہ",
                        fontFamily = UrduNastaliqFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        lineHeight = 24.sp,
                        color = GoldLight,
                        textAlign = TextAlign.Center
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "Digital Creator & App Developer: Fareed Ahmad",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = MintAccent.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Diagonal shimmer sheen (:after animation: sh 1.4s 1.9s)
            if (shimmer.value > -0.59f && shimmer.value < 1.29f) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val sheenWidth = size.width * 0.4f
                    val startX = size.width * shimmer.value
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(startX, 0f),
                            end = Offset(startX + sheenWidth, size.height * 0.2f)
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(sheenWidth, size.height)
                    )
                }
            }
        }

                    // Child 1: Bottom navigation group (Resume/Start, Book Index, Bookmark) directly below green cover
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                // Primary Gold Pill Button (#start)
                val startInteraction = remember { MutableInteractionSource() }
                val startPressed by startInteraction.collectIsPressedAsState()
                val startPressScale by animateFloatAsState(
                    targetValue = if (startPressed) 0.97f else 1f,
                    animationSpec = tween(150),
                    label = "start_press"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp)
                        .padding(bottom = 10.dp)
                        .graphicsLayer {
                            val p = startBtnUp.value
                            alpha = p
                            translationY = (1f - p) * 22.dp.toPx()
                            scaleX = startPressScale
                            scaleY = startPressScale
                        }
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(99.dp),
                            ambientColor = GoldAccent.copy(alpha = 0.3f),
                            spotColor = GoldAccent.copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GoldLight, GoldDeep)
                            )
                        )
                        .clickable(
                            interactionSource = startInteraction,
                            indication = null,
                            onClick = onStartReading
                        )
                        .testTag("start_reading_button")
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (hasSavedProgress) "جہاں چھوڑا تھا وہاں سے پڑھیں" else "پڑھنا شروع کریں",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 19.sp,
                        lineHeight = 42.sp,
                        color = GoldTextOnButton,
                        textAlign = TextAlign.Center
                    )
                }

                // Secondary Mint/Emerald Outlined Pill Button (#oi)
                val indexAccent = if (isDayMode) AccentDayGreen else MintAccent
                val indexInteraction = remember { MutableInteractionSource() }
                val indexPressed by indexInteraction.collectIsPressedAsState()
                val indexPressScale by animateFloatAsState(
                    targetValue = if (indexPressed) 0.97f else 1f,
                    animationSpec = tween(150),
                    label = "index_press"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp)
                        .padding(bottom = 10.dp)
                        .graphicsLayer {
                            val p = indexBtnUp.value
                            alpha = p
                            translationY = (1f - p) * 22.dp.toPx()
                            scaleX = indexPressScale
                            scaleY = indexPressScale
                        }
                        .clip(RoundedCornerShape(99.dp))
                        .background(indexAccent.copy(alpha = if (isDayMode) 0.12f else 0.08f))
                        .border(1.5.dp, indexAccent, RoundedCornerShape(99.dp))
                        .clickable(
                            interactionSource = indexInteraction,
                            indication = null,
                            onClick = onOpenFehrist
                        )
                        .testTag("open_fehrist_button")
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☰  کتاب کی فہرست دیکھیں",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 19.sp,
                        lineHeight = 42.sp,
                        color = indexAccent,
                        textAlign = TextAlign.Center
                    )
                }

                // Latest Bookmark Pill Button grouped with the bottom navigation pills
                if (!latestBookmarkTitle.isNullOrBlank()) {
                    val bookmarkAccent = if (isDayMode) AccentDayGold else GoldLight
                    val bookmarkInteraction = remember { MutableInteractionSource() }
                    val bookmarkPressed by bookmarkInteraction.collectIsPressedAsState()
                    val bookmarkPressScale by animateFloatAsState(
                        targetValue = if (bookmarkPressed) 0.97f else 1f,
                        animationSpec = tween(150),
                        label = "bookmark_press"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 360.dp)
                            .padding(bottom = 10.dp)
                            .sizeIn(minHeight = 48.dp)
                            .graphicsLayer {
                                val p = indexBtnUp.value
                                alpha = p
                                translationY = (1f - p) * 22.dp.toPx()
                                scaleX = bookmarkPressScale
                                scaleY = bookmarkPressScale
                            }
                            .clip(RoundedCornerShape(99.dp))
                            .background(GoldAccent.copy(alpha = if (isDayMode) 0.18f else 0.14f))
                            .border(1.5.dp, bookmarkAccent.copy(alpha = 0.8f), RoundedCornerShape(99.dp))
                            .clickable(
                                interactionSource = bookmarkInteraction,
                                indication = null,
                                onClick = onOpenLatestBookmark
                            )
                            .testTag("home_bookmark_button")
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "بک مارک",
                                tint = bookmarkAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "بک مارک: $latestBookmarkTitle",
                                fontFamily = UrduNastaliqFontFamily,
                                fontSize = 17.sp,
                                lineHeight = 36.sp,
                                color = bookmarkAccent,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            }) { measurables, constraints ->
                val loose = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
                val bottomPlaceable = measurables[1].measure(loose)
                val targetCoverMinHeight = (viewportHeightPx - topPaddingPx - coverToButtonsGapPx - bottomPlaceable.height)
                    .coerceAtLeast(0)
                val coverPlaceable = measurables[0].measure(loose.copy(minHeight = targetCoverMinHeight))
                val totalHeight = topPaddingPx + coverPlaceable.height + coverToButtonsGapPx + bottomPlaceable.height
                layout(constraints.maxWidth, totalHeight) {
                    val coverX = ((constraints.maxWidth - coverPlaceable.width) / 2).coerceAtLeast(0)
                    val bottomX = ((constraints.maxWidth - bottomPlaceable.width) / 2).coerceAtLeast(0)
                    coverPlaceable.placeRelative(coverX, topPaddingPx)
                    bottomPlaceable.placeRelative(bottomX, topPaddingPx + coverPlaceable.height + coverToButtonsGapPx)
                }
            }
        }
    }
}

@Composable
private fun HomePublisherPage(
    isVisible: Boolean,
    isDayMode: Boolean
) {
    val cardPop = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        if (isVisible && cardPop.value == 0f) {
            cardPop.animateTo(
                1f,
                tween(800, easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1f))
            )
        }
    }

    val cardBg = if (isDayMode) CardDay else CardDark
    val footerBg = if (isDayMode) Card2Day else Card2Dark
    val borderCol = if (isDayMode) LineDay else LineDark
    val titleGold = if (isDayMode) AccentDayGold else GoldAccent
    val bodyText = if (isDayMode) TextDayPrimary else TextPrimary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val shape = RoundedCornerShape(22.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
                .padding(top = 4.dp)
                .graphicsLayer {
                    val p = if (cardPop.value == 0f && !isVisible) 1f else cardPop.value
                    alpha = p
                    val s = 0.9f + 0.1f * p
                    scaleX = s
                    scaleY = s
                    translationY = (1f - p) * 24.dp.toPx()
                }
                .clip(shape)
                .background(cardBg)
                .border(1.dp, borderCol, shape)
        ) {
            // Top green header (.rt)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GreenPrimary, GreenDark)
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جملہ حقوق بہ حق ناشر محفوظ ہیں",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 17.sp,
                        lineHeight = 36.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo_maktaba),
                    contentDescription = "مکتبہ مسیح الامت",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.width(120.dp)
                )
            }

            // Metadata rows (.rw)
            InfoRow(label = "نامِ کتاب", value = "دل کے آبگینے سے", isDayMode = isDayMode)
            InfoRow(
                label = "نتیجۂ فکر",
                value = "حضرت مولانا مفتی محمد شعیب اللہ خان صاحب مفتاحی دامت برکاتہم",
                subValue = "بانی و مہتمم الجامعۃ الاسلامیہ مسیح العلوم بنگلور\nو خلیفہ حضرت اقدس شاہ مفتی مظفر حسین صاحب رحمۃ اللہ علیہ ناظم مظاہر علوم وقف سہارنپور",
                isDayMode = isDayMode
            )
            InfoRow(label = "صفحات", value = "208", isLtr = true, isDayMode = isDayMode)
            InfoRow(label = "تاریخِ طباعت", value = "۱۰ ربیع الثانی ۱۴۳۶ھ ہجری", isDayMode = isDayMode)
            InfoRow(label = "ناشر", value = "مکتبہ مسیح الامت دیوبند و بنگلور", isDayMode = isDayMode)
            InfoRow(label = "موبائل نمبر", value = "9036701512 / 09634830797", isLtr = true, isDayMode = isDayMode)
            InfoRow(label = "ای میل", value = "maktabahmaseehulummat@gmail.com", isLtr = true, isDayMode = isDayMode)

            // Developer & Publisher Footer (.dv)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(footerBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ایپ ڈیولپر و ڈیجیٹل ڈیزائنر: فرید احمد عفی عنہ",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 31.sp,
                    color = titleGold,
                    textAlign = TextAlign.Center
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = "App Developer & Digital Designer: Fareed Ahmed Afi Anhu\n7406341264",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.5.sp,
                        lineHeight = 21.sp,
                        color = bodyText,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "پبلشڈ بائی: الفلاح اسلامک آن لائن انسٹیٹیوٹ",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 31.sp,
                    color = titleGold,
                    textAlign = TextAlign.Center
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = "Published by: Al-Falah Islamic Online Institute\nalfalahIslamiconlineinstitute1@gmail.com",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.5.sp,
                        lineHeight = 21.sp,
                        color = bodyText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    subValue: String? = null,
    isLtr: Boolean = false,
    isDayMode: Boolean = false
) {
    val labelCol = if (isDayMode) AccentDayGreen else MintAccent
    val valueCol = if (isDayMode) TextDayPrimary else TextPrimary
    val subCol = if (isDayMode) TextDayMuted else TextMuted
    val lineCol = if (isDayMode) LineDay else LineDark

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 13.sp,
                lineHeight = 30.sp,
                color = labelCol,
                modifier = Modifier.width(88.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                if (isLtr) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = value,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                            color = valueCol,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = value,
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 15.sp,
                        lineHeight = 34.sp,
                        color = valueCol
                    )
                }
                if (subValue != null) {
                    Text(
                        text = subValue,
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 27.sp,
                        color = subCol
                    )
                }
            }
        }
        HorizontalDivider(color = lineCol, thickness = 1.dp)
    }
}

@Composable
private fun HomeBottomNavBar(
    isAboutPage: Boolean,
    isActive: Boolean,
    isDayMode: Boolean,
    onTogglePage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navInteraction = remember { MutableInteractionSource() }
    val navPressed by navInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (navPressed) 0.97f else 1f,
        animationSpec = tween(150),
        label = "nav_home_press"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) GoldLight else GoldAccent.copy(alpha = 0.78f),
        animationSpec = tween(250),
        label = "nav_home_border"
    )
    val navContainerBg = when {
        !isAboutPage -> Color.Transparent
        isDayMode -> BottomNavDayBg
        else -> BottomNavBg
    }
    val dividerCol = if (isDayMode) LineDay else LineDark

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(navContainerBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAboutPage) {
            HorizontalDivider(color = dividerCol, thickness = 1.dp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = if (isAboutPage) 10.dp else 0.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(99.dp),
                        ambientColor = GoldAccent.copy(alpha = 0.25f),
                        spotColor = GoldAccent.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GreenPrimary, GreenDark)
                        )
                    )
                    .border(1.5.dp, borderColor, RoundedCornerShape(99.dp))
                    .clickable(
                        interactionSource = navInteraction,
                        indication = null,
                        onClick = onTogglePage
                    )
                    .testTag("nav_home_button")
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isAboutPage) Icons.Outlined.Home else Icons.Outlined.Info,
                        contentDescription = if (isAboutPage) "کتاب پر واپس جائیں" else "کتاب کے بارے میں",
                        tint = GoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isAboutPage) "واپس کتاب پر" else "کتاب کے بارے میں",
                        fontFamily = UrduNastaliqFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        lineHeight = 40.sp,
                        color = GoldLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
