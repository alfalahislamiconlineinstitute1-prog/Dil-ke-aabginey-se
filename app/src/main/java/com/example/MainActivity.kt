package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BookViewModel
import com.example.ui.components.FehristModal
import com.example.ui.components.HomeScreen
import com.example.ui.components.ReaderOverlay
import com.example.ui.components.ShareModal
import com.example.ui.components.SplashOverlay
import com.example.ui.theme.DilKayAbginayTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.ToastBg
import com.example.ui.theme.UrduNastaliqFontFamily

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            DilKayAbginayTheme {
                DilKayAbginayApp()
            }
        }
    }
}

@Composable
fun DilKayAbginayApp(
    viewModel: BookViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentPoem = state.currentPoem
    val prevPoem = state.poems.getOrNull(state.currentPoemIndex - 1)
    val nextPoem = state.poems.getOrNull(state.currentPoemIndex + 1)
    val latestBookmark = state.latestBookmark
    val latestBookmarkTitle = latestBookmark?.let { bm -> state.poems.getOrNull(bm.poemIndex)?.title }

    val lightStatusBar = state.isSepiaReader && !state.isReaderOpen && !state.isFehristOpen && !state.showSplash
    val lightNavBar = state.isSepiaReader && !state.isShareOpen && !state.showSplash
    LaunchedEffect(lightStatusBar, lightNavBar) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = if (lightStatusBar) {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            } else {
                SystemBarStyle.dark(Color.TRANSPARENT)
            },
            navigationBarStyle = if (lightNavBar) {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            } else {
                SystemBarStyle.dark(Color.TRANSPARENT)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1 (z-index: 1): Home Screen with rotating sunburst, cover pager, credits & bottom About Book pill
        HomeScreen(
            hasSavedProgress = state.lastSavedPoemIndex >= 0,
            latestBookmarkTitle = latestBookmarkTitle,
            isFehristOpen = state.isFehristOpen,
            isDayMode = state.isSepiaReader,
            onToggleDayNight = viewModel::toggleReaderTheme,
            onStartReading = viewModel::resumeReading,
            onOpenFehrist = viewModel::openFehrist,
            onOpenLatestBookmark = {
                latestBookmark?.let(viewModel::openBookmark)
            }
        )

        // Layer 2 (z-index: 10): Poem Reader Overlay
        ReaderOverlay(
            visible = state.isReaderOpen,
            poem = currentPoem,
            prevPoem = prevPoem,
            nextPoem = nextPoem,
            targetScrollItemIndex = state.readerTargetScrollItemIndex,
            targetScrollItemOffset = state.readerTargetScrollItemOffset,
            scrollRestoreNonce = state.readerScrollRestoreNonce,
            isBookmarked = state.isCurrentPoemBookmarked,
            fontSizeSp = state.readerFontSizeSp,
            isSepia = state.isSepiaReader,
            onClose = viewModel::closeReader,
            onOpenShare = viewModel::openShare,
            onOpenFehrist = viewModel::openFehrist,
            onToggleTheme = viewModel::toggleReaderTheme,
            onDecreaseFont = viewModel::decreaseFontSize,
            onIncreaseFont = viewModel::increaseFontSize,
            onSelectPoem = viewModel::selectPoemInReader,
            onScrollPositionChanged = viewModel::updateReaderScrollPosition,
            onToggleBookmark = viewModel::toggleBookmark
        )

        // Layer 3 (z-index: 30): Paginated Fehrist Index Modal
        FehristModal(
            visible = state.isFehristOpen,
            sections = state.sections,
            poems = state.poems,
            bookmarks = state.bookmarks,
            isDayMode = state.isSepiaReader,
            onClose = viewModel::closeFehrist,
            onSelectPoem = viewModel::openPoem,
            onSelectBookmark = viewModel::openBookmark
        )

        // Layer 4 (z-index: 40): 9:16 Story/Status Share Card Modal
        ShareModal(
            visible = state.isShareOpen,
            cards = state.shareCards,
            isGeneratingCards = state.isGeneratingCards,
            selectedThemeIndex = state.shareThemeIndex,
            onClose = viewModel::closeShare,
            onSelectTheme = viewModel::selectShareTheme,
            onShareSingleCard = viewModel::shareSingleCard,
            onShareAllCards = viewModel::shareAllCards,
            onSaveCardToGallery = viewModel::saveCardToGallery,
            onSharePlainText = viewModel::sharePlainText
        )

        // Layer 5 (z-index: 50): Animated Islamic Geometric Splash Overlay
        SplashOverlay(visible = state.showSplash)

        // Layer 6 (z-index: 60): Floating Toast Banner (.ts)
        AnimatedVisibility(
            visible = state.toastMessage != null,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(250)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(99.dp)
                    )
                    .clip(RoundedCornerShape(99.dp))
                    .background(ToastBg)
                    .testTag("toast_banner")
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.toastMessage.orEmpty(),
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 28.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
