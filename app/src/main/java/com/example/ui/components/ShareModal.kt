package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.share.SHARE_THEMES
import com.example.share.ShareTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldTextOnButton
import com.example.ui.theme.LineDark
import com.example.ui.theme.MintAccent
import com.example.ui.theme.ShareBackdrop
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import com.example.ui.theme.toUrduDigits
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ShareModal(
    visible: Boolean,
    cards: List<Bitmap>,
    isGeneratingCards: Boolean,
    selectedThemeIndex: Int,
    onClose: () -> Unit,
    onSelectTheme: (Int) -> Unit,
    onShareSingleCard: (Int) -> Unit,
    onShareAllCards: () -> Unit,
    onSaveCardToGallery: (Int) -> Unit,
    onSharePlainText: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        BackHandler(enabled = visible) {
            onClose()
        }

        val pageCount = cards.size.coerceAtLeast(1)
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
        val scope = rememberCoroutineScope()

        LaunchedEffect(cards.size) {
            if (pagerState.currentPage >= pageCount) {
                pagerState.scrollToPage((pageCount - 1).coerceAtLeast(0))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ShareBackdrop)
                .systemBarsPadding()
                .testTag("share_modal")
        ) {
            // Header (.shh)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .testTag("share_close_button")
                        .semantics { contentDescription = "بند کریں" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 17.sp,
                            lineHeight = 20.sp,
                            color = TextPrimary
                        )
                    }
                }

                Text(
                    text = "شیئر کریں",
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 44.sp,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )

                if (cards.size > 1) {
                    Text(
                        text = "صفحہ ${(pagerState.currentPage + 1).toUrduDigits()} از ${cards.size.toUrduDigits()}",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 28.sp,
                        color = MintAccent
                    )
                }
            }

            // Card Pager (#shp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (cards.isEmpty()) {
                    Text(
                        text = "تصویر بن رہی ہے…",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 30.sp,
                        color = MintAccent,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val cardsAlpha by animateFloatAsState(
                        targetValue = if (isGeneratingCards) 0.35f else 1f,
                        animationSpec = tween(250),
                        label = "share_regen_alpha"
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(cardsAlpha)
                    ) { pageIndex ->
                        val bmp = cards[pageIndex]
                        val cardPop = remember(cards, pageIndex) { Animatable(0f) }
                        LaunchedEffect(cards, pageIndex) {
                            cardPop.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1f)
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "شیئر کارڈ صفحہ ${(pageIndex + 1).toUrduDigits()}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                                    .graphicsLayer {
                                        val p = cardPop.value
                                        alpha = p
                                        val s = 0.9f + 0.1f * p
                                        scaleX = s
                                        scaleY = s
                                        translationY = (1f - p) * 24.dp.toPx()
                                    }
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = Color.Black,
                                        spotColor = Color.Black
                                    )
                                    .clip(RoundedCornerShape(18.dp))
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { onSaveCardToGallery(pageIndex) }
                                    )
                                    .testTag("share_card_image_$pageIndex")
                            )
                        }
                    }
                }
            }

            // Card Page Dots (#shd)
            if (cards.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(cards.size) { idx ->
                        val isSelected = pagerState.currentPage == idx
                        val dotW by animateDpAsState(
                            targetValue = if (isSelected) 26.dp else 8.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "share_dot_w"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) GoldAccent else LineDark,
                            animationSpec = tween(durationMillis = 300),
                            label = "share_dot_c"
                        )
                        Box(
                            modifier = Modifier
                                .size(width = dotW, height = 8.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(dotColor)
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(idx) }
                                }
                        )
                    }
                }
            }

            // 7 Circular Gradient Color Swatches (.shs)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "رنگ",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 24.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 6.dp)
                )
                SHARE_THEMES.forEachIndexed { idx, theme ->
                    ThemeSwatchButton(
                        theme = theme,
                        isSelected = idx == selectedThemeIndex,
                        testTag = "share_theme_$idx",
                        onClick = { onSelectTheme(idx) }
                    )
                }
            }

            // Action Buttons (.sha)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShareActionButton(
                    label = "اس صفحے کو شیئر کریں",
                    isGhost = false,
                    enabled = cards.isNotEmpty(),
                    testTag = "share_single_button",
                    onClick = { onShareSingleCard(pagerState.currentPage) }
                )
                ShareActionButton(
                    label = "سب صفحات شیئر کریں",
                    isGhost = false,
                    enabled = cards.isNotEmpty(),
                    testTag = "share_all_button",
                    onClick = onShareAllCards
                )
                ShareActionButton(
                    label = "متن شیئر / کاپی",
                    isGhost = true,
                    enabled = true,
                    testTag = "share_text_button",
                    onClick = onSharePlainText
                )
            }

            // Footer instructional note (.shn)
            Text(
                text = "اسٹوری اور اسٹیٹس کے لیے ۹:۱۶ سائز · تصویر کو دیر تک دبا کر بھی محفوظ کر سکتے ہیں",
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 12.sp,
                lineHeight = 26.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun ThemeSwatchButton(
    theme: ShareTheme,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "swatch_scale"
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .semantics { contentDescription = theme.name },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(theme.bgColors.first()),
                            Color(theme.bgColors.last())
                        )
                    )
                )
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(theme.accentColor))
            )
        }
    }
}

@Composable
private fun ShareActionButton(
    label: String,
    isGhost: Boolean,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bgColor = when {
        isGhost -> Color.Transparent
        enabled -> GoldAccent
        else -> LineDark
    }
    val borderColor = if (isGhost) MintAccent else Color.Transparent
    val txtColor = when {
        isGhost -> MintAccent
        enabled -> GoldTextOnButton
        else -> TextMuted
    }

    Box(
        modifier = Modifier
            .sizeIn(minHeight = 48.dp)
            .clip(RoundedCornerShape(99.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(bgColor)
                .then(
                    if (isGhost) {
                        Modifier.border(1.5.dp, borderColor, RoundedCornerShape(99.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 30.sp,
                color = txtColor
            )
        }
    }
}
