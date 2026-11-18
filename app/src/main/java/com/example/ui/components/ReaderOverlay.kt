package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PoemEntry
import com.example.data.Stanza
import com.example.data.StanzaLine
import com.example.data.parsePoemStanzas
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenDark
import com.example.ui.theme.MintAccent
import com.example.ui.theme.ReaderAccentDark
import com.example.ui.theme.ReaderAccentSepia
import com.example.ui.theme.ReaderBgDark
import com.example.ui.theme.ReaderBgSepia
import com.example.ui.theme.ReaderLineDark
import com.example.ui.theme.ReaderLineSepia
import com.example.ui.theme.ReaderTextDark
import com.example.ui.theme.ReaderTextSepia
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ReaderOverlay(
    visible: Boolean,
    poem: PoemEntry?,
    prevPoem: PoemEntry?,
    nextPoem: PoemEntry?,
    targetScrollItemIndex: Int,
    targetScrollItemOffset: Int,
    scrollRestoreNonce: Int,
    isBookmarked: Boolean,
    fontSizeSp: Float,
    isSepia: Boolean,
    onClose: () -> Unit,
    onOpenShare: () -> Unit,
    onOpenFehrist: () -> Unit,
    onToggleTheme: () -> Unit,
    onDecreaseFont: () -> Unit,
    onIncreaseFont: () -> Unit,
    onSelectPoem: (Int) -> Unit,
    onScrollPositionChanged: (poemIndex: Int, itemIndex: Int, itemOffset: Int) -> Unit,
    onToggleBookmark: (itemIndex: Int, itemOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val slideEasing = remember { CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f) }

    AnimatedVisibility(
        visible = visible && poem != null,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 500, easing = slideEasing),
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 500, easing = slideEasing),
            targetOffsetY = { it }
        ),
        modifier = modifier
    ) {
        if (poem == null) return@AnimatedVisibility

        val listState = rememberLazyListState()

        val closeWithSave = {
            onScrollPositionChanged(
                poem.index,
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
            onClose()
        }

        BackHandler(enabled = visible) {
            closeWithSave()
        }

        val bgColor by animateColorAsState(
            targetValue = if (isSepia) ReaderBgSepia else ReaderBgDark,
            animationSpec = tween(400),
            label = "reader_bg"
        )
        val textColor by animateColorAsState(
            targetValue = if (isSepia) ReaderTextSepia else ReaderTextDark,
            animationSpec = tween(400),
            label = "reader_text"
        )
        val accentColor by animateColorAsState(
            targetValue = if (isSepia) ReaderAccentSepia else ReaderAccentDark,
            animationSpec = tween(400),
            label = "reader_accent"
        )
        val lineColor by animateColorAsState(
            targetValue = if (isSepia) ReaderLineSepia else ReaderLineDark,
            animationSpec = tween(400),
            label = "reader_line"
        )

        val stanzas = remember(poem.index, poem.rawText) {
            poem.rawText?.let { parsePoemStanzas(it) } ?: emptyList()
        }

        // Restore saved scroll index/offset whenever opening or restoring a bookmark/poem
        LaunchedEffect(poem.index, scrollRestoreNonce) {
            val safeIdx = targetScrollItemIndex.coerceAtLeast(0)
            val safeOffset = targetScrollItemOffset.coerceAtLeast(0)
            listState.scrollToItem(safeIdx, safeOffset)
        }

        // Persist scroll position continuously as user reads
        LaunchedEffect(visible, poem.index) {
            if (visible) {
                snapshotFlow {
                    listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                }
                    .distinctUntilChanged()
                    .collect { (idx, offset) ->
                        onScrollPositionChanged(poem.index, idx, offset)
                    }
            }
        }

        val scrollProgress by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                if (totalItems <= 1) {
                    0f
                } else {
                    val visibleItems = layoutInfo.visibleItemsInfo
                    val first = visibleItems.firstOrNull()
                    if (first == null) {
                        0f
                    } else if (!listState.canScrollForward && listState.canScrollBackward) {
                        1f
                    } else {
                        val itemHeight = first.size.coerceAtLeast(1).toFloat()
                        val fractionInFirst = (-first.offset / itemHeight).coerceIn(0f, 1f)
                        val maxFirstIndex = (totalItems - visibleItems.size).coerceAtLeast(1).toFloat()
                        ((first.index + fractionInFirst) / maxFirstIndex).coerceIn(0f, 1f)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .testTag("reader_overlay")
        ) {
            // Top bar (.bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenDark)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                ReaderBarButton(
                    label = "✕",
                    contentDescription = "بند کریں",
                    testTag = "reader_close_button",
                    onClick = closeWithSave
                )
                Text(
                    text = poem.title,
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 32.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                ReaderBookmarkIconButton(
                    isBookmarked = isBookmarked,
                    testTag = "reader_bookmark_button",
                    onClick = {
                        onToggleBookmark(
                            listState.firstVisibleItemIndex,
                            listState.firstVisibleItemScrollOffset
                        )
                    }
                )
                if (poem.rawText != null) {
                    ReaderBarButton(
                        label = "⤴",
                        contentDescription = "شیئر",
                        testTag = "reader_share_button",
                        onClick = onOpenShare
                    )
                }
                ReaderBarButton(
                    label = "☰",
                    contentDescription = "فہرست",
                    testTag = "reader_fehrist_button",
                    onClick = onOpenFehrist
                )
                ReaderBarButton(
                    label = "◐",
                    contentDescription = "تھیم",
                    testTag = "reader_theme_button",
                    onClick = onToggleTheme
                )
                ReaderBarButton(
                    label = "A−",
                    contentDescription = "حروف چھوٹے کریں",
                    testTag = "reader_font_dec_button",
                    onClick = onDecreaseFont
                )
                ReaderBarButton(
                    label = "A+",
                    contentDescription = "حروف بڑے کریں",
                    testTag = "reader_font_inc_button",
                    onClick = onIncreaseFont
                )
            }

            // 3.dp Gold Scroll Progress Bar (.pg)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0x407F7F7F))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(scrollProgress)
                        .fillMaxHeight()
                        .background(GoldAccent)
                )
            }

            // Reader Body (#st / #ar)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 44.dp
                    )
                ) {
                    // Poem Header (.ph.rv)
                    item(key = "header_${poem.index}") {
                        ScrollRevealBox(revealKey = "header_${poem.index}") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp, bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = poem.category,
                                    fontFamily = UrduNastaliqFontFamily,
                                    fontSize = 13.sp,
                                    lineHeight = 28.sp,
                                    color = poem.categoryColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = poem.title,
                                    fontFamily = UrduNastaliqFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = (fontSizeSp * 1.35f).sp,
                                    lineHeight = (fontSizeSp * 1.35f * 2.15f).sp,
                                    color = accentColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "❖ ❖ ❖",
                                    fontSize = 12.sp,
                                    letterSpacing = 6.sp,
                                    color = accentColor.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                if (poem.rawText != null) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .sizeIn(minHeight = 48.dp)
                                            .clip(RoundedCornerShape(99.dp))
                                            .clickable(onClick = onOpenShare)
                                            .testTag("reader_header_share_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(99.dp))
                                                .border(
                                                    width = 1.dp,
                                                    color = MintAccent.copy(alpha = 0.45f),
                                                    shape = RoundedCornerShape(99.dp)
                                                )
                                                .padding(horizontal = 18.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "⤴ شیئر",
                                                fontFamily = UrduNastaliqFontFamily,
                                                fontSize = 13.sp,
                                                lineHeight = 24.sp,
                                                color = if (isSepia) accentColor else MintAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stanzas or Fallback Notice
                    if (poem.rawText == null) {
                        item(key = "empty_${poem.index}") {
                            ScrollRevealBox(revealKey = "empty_${poem.index}") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(GoldAccent.copy(alpha = 0.12f))
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "اس کلام کا متن جلد شامل کیا جائے گا",
                                        fontFamily = UrduNastaliqFontFamily,
                                        fontSize = (fontSizeSp * 0.65f).coerceAtLeast(15f).sp,
                                        lineHeight = (fontSizeSp * 0.65f * 2.1f).coerceAtLeast(32f).sp,
                                        color = textColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = stanzas,
                            key = { idx, _ -> "stanza_${poem.index}_$idx" }
                        ) { idx, stanza ->
                            ScrollRevealBox(revealKey = "stanza_${poem.index}_$idx") {
                                StanzaView(
                                    stanza = stanza,
                                    fontSizeSp = fontSizeSp,
                                    textColor = textColor,
                                    accentColor = accentColor,
                                    lineColor = lineColor
                                )
                            }
                        }
                    }

                    // Next / Previous Poem Footer (.pn2.rv)
                    item(key = "footer_${poem.index}") {
                        ScrollRevealBox(revealKey = "footer_${poem.index}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(top = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (nextPoem != null) {
                                    ReaderFooterNavButton(
                                        directionLabel = "اگلی ←",
                                        poemTitle = nextPoem.title,
                                        textColor = textColor,
                                        accentColor = accentColor,
                                        testTag = "reader_next_button",
                                        onClick = { onSelectPoem(nextPoem.index) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (prevPoem != null) {
                                    ReaderFooterNavButton(
                                        directionLabel = "→ پچھلی",
                                        poemTitle = prevPoem.title,
                                        textColor = textColor,
                                        accentColor = accentColor,
                                        testTag = "reader_prev_button",
                                        onClick = { onSelectPoem(prevPoem.index) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollRevealBox(
    revealKey: String,
    content: @Composable () -> Unit
) {
    val progress = remember(revealKey) { Animatable(0f) }
    LaunchedEffect(revealKey) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
            )
        )
    }

    Box(
        modifier = Modifier.graphicsLayer {
            val p = progress.value
            alpha = p
            translationY = (1f - p) * 26.dp.toPx()
        }
    ) {
        content()
    }
}

@Composable
private fun ReaderBarButton(
    label: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 36.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReaderBookmarkIconButton(
    isBookmarked: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 36.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isBookmarked) GoldAccent.copy(alpha = 0.22f)
                    else Color.White.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isBookmarked) "بک مارک ہٹائیں" else "بک مارک محفوظ کریں",
                tint = if (isBookmarked) GoldAccent else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StanzaView(
    stanza: Stanza,
    fontSizeSp: Float,
    textColor: Color,
    accentColor: Color,
    lineColor: Color
) {
    val baseModifier = if (stanza.isBoxed) {
        Modifier
            .fillMaxWidth()
            .padding(bottom = 22.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GoldAccent.copy(alpha = 0.07f))
            .border(1.5.dp, accentColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(bottom = 22.dp)
    }

    Column(modifier = baseModifier) {
        stanza.lines.forEach { line ->
            when (line) {
                is StanzaLine.Couplet -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = line.first,
                            fontFamily = UrduNastaliqFontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 2.7f).sp,
                            color = textColor,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = line.second,
                            fontFamily = UrduNastaliqFontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 2.7f).sp,
                            color = textColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is StanzaLine.Centered -> {
                    Text(
                        text = line.text,
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * 2.7f).sp,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
                is StanzaLine.Refrain -> {
                    Text(
                        text = line.text,
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = (fontSizeSp * 0.86f).sp,
                        lineHeight = (fontSizeSp * 0.86f * 2.45f).sp,
                        color = accentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
                is StanzaLine.Subheading -> {
                    Text(
                        text = line.text,
                        fontFamily = UrduNastaliqFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (fontSizeSp * 0.95f).sp,
                        lineHeight = (fontSizeSp * 0.95f * 2.45f).sp,
                        color = accentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                }
                is StanzaLine.Prose -> {
                    val proseSize = (fontSizeSp * 0.78f).coerceAtLeast(15f)
                    Text(
                        text = line.text,
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = proseSize.sp,
                        lineHeight = (proseSize * 2.65f).sp,
                        color = textColor,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
        if (!stanza.isBoxed) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = lineColor, thickness = 1.dp)
        }
    }
}

@Composable
private fun ReaderFooterNavButton(
    directionLabel: String,
    poemTitle: String,
    textColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, accentColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = directionLabel,
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 12.sp,
            lineHeight = 26.sp,
            color = accentColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = poemTitle,
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 14.sp,
            lineHeight = 30.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
