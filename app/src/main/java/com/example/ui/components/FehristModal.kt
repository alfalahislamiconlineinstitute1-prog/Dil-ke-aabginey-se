package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookSection
import com.example.data.BookmarkEntry
import com.example.data.PoemEntry
import com.example.ui.theme.AccentDayGold
import com.example.ui.theme.AccentDayGreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.BgDay
import com.example.ui.theme.Card2Dark
import com.example.ui.theme.Card2Day
import com.example.ui.theme.CardDay
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldTextOnButton
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.LineDark
import com.example.ui.theme.LineDay
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TextDayPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import com.example.ui.theme.toUrduDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface FehristBlock {
    data class CategoryHeader(val title: String, val accentColor: Color) : FehristBlock
    data class ItemRow(val serialNumber: Int, val entry: PoemEntry, val rowIndexInPage: Int) : FehristBlock
}

private data class FehristPage(val blocks: List<FehristBlock>)

private fun paginateSections(sections: List<BookSection>, availableHeightDp: Float): List<FehristPage> {
    if (sections.isEmpty()) return emptyList()
    val usableHeight = (availableHeightDp - 16f).coerceAtLeast(220f)
    val categoryHeaderHeight = 52f
    val rowHeight = 52f

    val pages = mutableListOf<MutableList<FehristBlock>>(mutableListOf())
    var usedHeight = 0f

    sections.forEach { section ->
        var isFirstCategoryHeader = true
        fun makeHeader(): FehristBlock.CategoryHeader {
            val label = if (isFirstCategoryHeader) section.title else "${section.title} · جاری"
            isFirstCategoryHeader = false
            return FehristBlock.CategoryHeader(label, section.color)
        }

        // Start category header on current page if at least header + 1 item fits, otherwise move to next page
        if (usedHeight + categoryHeaderHeight + rowHeight > usableHeight && pages.last().isNotEmpty()) {
            pages.add(mutableListOf())
            usedHeight = 0f
        }
        pages.last().add(makeHeader())
        usedHeight += categoryHeaderHeight

        section.items.forEach { entry ->
            if (usedHeight + rowHeight > usableHeight && pages.last().isNotEmpty()) {
                // Remove trailing orphan category header if page ended cleanly before any item in it
                val prevPage = pages.last()
                if (prevPage.lastOrNull() is FehristBlock.CategoryHeader) {
                    prevPage.removeAt(prevPage.lastIndex)
                }
                if (prevPage.isNotEmpty()) {
                    pages.add(mutableListOf())
                }
                usedHeight = 0f
                pages.last().add(makeHeader())
                usedHeight += categoryHeaderHeight
            }
            val rowIdx = pages.last().count { it is FehristBlock.ItemRow }
            pages.last().add(
                FehristBlock.ItemRow(
                    serialNumber = entry.index + 1,
                    entry = entry,
                    rowIndexInPage = rowIdx
                )
            )
            usedHeight += rowHeight
        }
    }

    return pages.filter { it.isNotEmpty() }.map { FehristPage(it) }
}

@Composable
fun FehristModal(
    visible: Boolean,
    sections: List<BookSection>,
    poems: List<PoemEntry>,
    bookmarks: List<BookmarkEntry>,
    isDayMode: Boolean = false,
    onClose: () -> Unit,
    onSelectPoem: (Int) -> Unit,
    onSelectBookmark: (BookmarkEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val slideEasing = remember { CubicBezierEasing(0.2f, 0.9f, 0.2f, 1f) }
    val bookmarkedPoemIndices = remember(bookmarks) {
        bookmarks.map { it.poemIndex }.toSet()
    }
    val modalBg = if (isDayMode) BgDay else BgDark
    val barBg = if (isDayMode) Card2Day else Card2Dark
    val lineCol = if (isDayMode) LineDay else LineDark
    val chipGold = if (isDayMode) AccentDayGold else GoldAccent

    AnimatedVisibility(
        visible = visible,
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
        BackHandler(enabled = visible) {
            onClose()
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(modalBg)
                .testTag("fehrist_modal")
        ) {
            val topHeaderApprox = 64.dp
            val bottomFooterApprox = 60.dp
            val availableHeight = (maxHeight - topHeaderApprox - bottomFooterApprox - 40.dp).value

            val pages = remember(sections, availableHeight.toInt()) {
                paginateSections(sections, availableHeight)
            }
            val pageCount = pages.size.coerceAtLeast(1)
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
            val scope = rememberCoroutineScope()

            LaunchedEffect(visible) {
                if (visible && pagerState.currentPage != 0) {
                    pagerState.scrollToPage(0)
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header (.mh)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GreenPrimary, GreenDark)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FehristCircleIconButton(
                        symbol = "✕",
                        contentDescription = "بند کریں",
                        testTag = "fehrist_close_button",
                        onClick = onClose
                    )
                    Text(
                        text = "فہرست",
                        fontFamily = UrduNastaliqFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        lineHeight = 52.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "صفحہ ${(pagerState.currentPage + 1).toUrduDigits()} از ${pageCount.toUrduDigits()}",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 28.sp,
                        color = GoldAccent
                    )
                }

                if (bookmarks.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(barBg)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(
                            items = bookmarks.sortedByDescending { it.savedAtMillis },
                            key = { "bm_${it.poemIndex}" }
                        ) { bookmark ->
                            val bmTitle = poems.getOrNull(bookmark.poemIndex)?.title ?: return@items
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(GoldAccent.copy(alpha = if (isDayMode) 0.22f else 0.16f))
                                    .clickable { onSelectBookmark(bookmark) }
                                    .testTag("fehrist_bookmark_chip_${bookmark.poemIndex}")
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "بک مارک",
                                    tint = chipGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = bmTitle,
                                    fontFamily = UrduNastaliqFontFamily,
                                    fontSize = 12.5.sp,
                                    lineHeight = 24.sp,
                                    color = chipGold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = lineCol, thickness = 1.dp)
                }

                // Paginated RTL Horizontal Pager (#pgr)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val pageData = pages.getOrNull(pageIndex)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        pageData?.blocks?.forEach { block ->
                            when (block) {
                                is FehristBlock.CategoryHeader -> {
                                    FehristCategoryHeader(
                                        title = block.title,
                                        accentColor = block.accentColor,
                                        isDayMode = isDayMode
                                    )
                                }
                                is FehristBlock.ItemRow -> {
                                    FehristItemRow(
                                        serialNumber = block.serialNumber,
                                        entry = block.entry,
                                        isBookmarked = block.entry.index in bookmarkedPoemIndices,
                                        rowIndexInPage = block.rowIndexInPage,
                                        isCurrentPage = pagerState.currentPage == pageIndex,
                                        isDayMode = isDayMode,
                                        onClick = { onSelectPoem(block.entry.index) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer (.mf): Next (right in RTL), Dots, Prev (left in RTL)
                HorizontalDivider(color = lineCol, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(barBg)
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FehristFooterPillButton(
                        label = "اگلا",
                        enabled = pagerState.currentPage < pageCount - 1,
                        isDayMode = isDayMode,
                        testTag = "fehrist_next_page_button",
                        onClick = {
                            scope.launch {
                                val target = (pagerState.currentPage + 1).coerceAtMost(pageCount - 1)
                                pagerState.animateScrollToPage(target)
                            }
                        }
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        repeat(pageCount) { idx ->
                            val isSelected = pagerState.currentPage == idx
                            val dotW by animateDpAsState(
                                targetValue = if (isSelected) 22.dp else 7.dp,
                                animationSpec = tween(durationMillis = 350),
                                label = "fehrist_dot_w"
                            )
                            val dotColor by animateColorAsState(
                                targetValue = if (isSelected) chipGold else lineCol,
                                animationSpec = tween(durationMillis = 350),
                                label = "fehrist_dot_c"
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = dotW, height = 7.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(dotColor)
                                    .clickable {
                                        scope.launch { pagerState.animateScrollToPage(idx) }
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    FehristFooterPillButton(
                        label = "پچھلا",
                        enabled = pagerState.currentPage > 0,
                        isDayMode = isDayMode,
                        testTag = "fehrist_prev_page_button",
                        onClick = {
                            scope.launch {
                                val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                                pagerState.animateScrollToPage(target)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FehristCircleIconButton(
    symbol: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription },
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
                text = symbol,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun FehristCategoryHeader(
    title: String,
    accentColor: Color,
    isDayMode: Boolean
) {
    val headerBg = if (isDayMode) CardDay else Card2Dark
    val headerText = if (isDayMode) TextDayPrimary else TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(10.dp))
            .background(headerBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right accent border (Start in RTL) matching border-right: 6px solid var(--c)
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(accentColor)
        )
        Text(
            text = title,
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 16.sp,
            lineHeight = 36.sp,
            color = headerText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun FehristItemRow(
    serialNumber: Int,
    entry: PoemEntry,
    isBookmarked: Boolean,
    rowIndexInPage: Int,
    isCurrentPage: Boolean,
    isDayMode: Boolean,
    onClick: () -> Unit
) {
    val riseAnim = remember(entry.index) { Animatable(0f) }
    LaunchedEffect(isCurrentPage, entry.index) {
        if (isCurrentPage) {
            riseAnim.snapTo(0f)
            delay(120L + rowIndexInPage * 30L)
            riseAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        } else {
            riseAnim.snapTo(1f)
        }
    }

    val titleCol = if (isDayMode) TextDayPrimary else TextPrimary
    val accentCol = if (isDayMode) AccentDayGreen else MintAccent
    val bookmarkCol = if (isDayMode) AccentDayGold else GoldAccent
    val dividerCol = if (isDayMode) LineDay else LineDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val p = riseAnim.value
                alpha = p
                translationY = (1f - p) * 22.dp.toPx()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .clickable(onClick = onClick)
                .testTag("fehrist_item_${entry.index}")
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gold circular serial badge (.n)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(GoldAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = serialNumber.toUrduDigits(),
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 24.sp,
                    color = GoldTextOnButton,
                    textAlign = TextAlign.Center
                )
            }

            // Title (.t)
            Text(
                text = entry.title,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.5.sp,
                lineHeight = 34.sp,
                color = titleCol,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isBookmarked) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "بک مارک شدہ",
                    tint = bookmarkCol,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Available text pill badge (.tg)
            if (entry.rawText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(accentCol.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "متن",
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 20.sp,
                        color = accentCol
                    )
                }
            }

            // Urdu page number (.pn)
            Text(
                text = entry.pageNumber.toUrduDigits(),
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 13.sp,
                lineHeight = 28.sp,
                color = accentCol,
                textAlign = TextAlign.End,
                modifier = Modifier.width(32.dp)
            )
        }
        HorizontalDivider(color = dividerCol, thickness = 1.dp)
    }
}

@Composable
private fun FehristFooterPillButton(
    label: String,
    enabled: Boolean,
    isDayMode: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val disabledBg = if (isDayMode) LineDay else LineDark
    val disabledText = if (isDayMode) TextDayPrimary.copy(alpha = 0.4f) else TextPrimary.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 64.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(99.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(if (enabled) GoldAccent else disabledBg)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 32.sp,
                color = if (enabled) GoldTextOnButton else disabledText
            )
        }
    }
}
