package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookRepository
import com.example.data.BookSection
import com.example.data.BookmarkEntry
import com.example.data.PoemEntry
import com.example.data.PreferencesRepository
import com.example.data.formatPoemPlainText
import com.example.share.ShareCardRenderer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookUiState(
    val isLoading: Boolean = true,
    val showSplash: Boolean = true,
    val sections: List<BookSection> = emptyList(),
    val poems: List<PoemEntry> = emptyList(),
    val isFehristOpen: Boolean = false,
    val isReaderOpen: Boolean = false,
    val currentPoemIndex: Int = 0,
    val lastSavedPoemIndex: Int = -1,
    val lastSavedScrollItemIndex: Int = 0,
    val lastSavedScrollItemOffset: Int = 0,
    val readerTargetScrollItemIndex: Int = 0,
    val readerTargetScrollItemOffset: Int = 0,
    val readerScrollRestoreNonce: Int = 0,
    val bookmarks: List<BookmarkEntry> = emptyList(),
    val readerFontSizeSp: Float = 24f,
    val isSepiaReader: Boolean = false,
    val isShareOpen: Boolean = false,
    val shareThemeIndex: Int = 0,
    val shareCards: List<Bitmap> = emptyList(),
    val isGeneratingCards: Boolean = false,
    val toastMessage: String? = null
) {
    val currentPoem: PoemEntry?
        get() = poems.getOrNull(currentPoemIndex)

    val isCurrentPoemBookmarked: Boolean
        get() = bookmarks.any { it.poemIndex == currentPoemIndex }

    val latestBookmark: BookmarkEntry?
        get() = bookmarks.maxByOrNull { it.savedAtMillis }
}

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookRepository = BookRepository(application)
    private val prefsRepository = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(BookUiState())
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    private var shareJob: Job? = null
    private var toastJob: Job? = null
    private var scrollSaveJob: Job? = null

    init {
        viewModelScope.launch {
            val (sections, poems) = bookRepository.loadBookData()
            val savedPosition = prefsRepository.lastReadingPosition.first()
            val savedBookmarks = prefsRepository.bookmarks.first()
            val savedTheme = prefsRepository.shareThemeIndex.first()
            val savedSepia = prefsRepository.isSepiaReader.first()
            val savedFont = prefsRepository.readerFontSize.first()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    sections = sections,
                    poems = poems,
                    lastSavedPoemIndex = savedPosition.poemIndex,
                    lastSavedScrollItemIndex = savedPosition.scrollItemIndex,
                    lastSavedScrollItemOffset = savedPosition.scrollItemOffset,
                    currentPoemIndex = savedPosition.poemIndex.coerceIn(0, (poems.size - 1).coerceAtLeast(0)),
                    readerTargetScrollItemIndex = savedPosition.scrollItemIndex,
                    readerTargetScrollItemOffset = savedPosition.scrollItemOffset,
                    bookmarks = savedBookmarks,
                    shareThemeIndex = savedTheme.coerceIn(0, 6),
                    isSepiaReader = savedSepia,
                    readerFontSizeSp = savedFont.coerceIn(18f, 40f)
                )
            }
        }

        // Exact 2200ms splash duration as in HTML
        viewModelScope.launch {
            delay(2200L)
            _uiState.update { it.copy(showSplash = false) }
        }
    }

    fun openFehrist() {
        _uiState.update { it.copy(isFehristOpen = true) }
    }

    fun closeFehrist() {
        _uiState.update { it.copy(isFehristOpen = false) }
    }

    fun openPoem(index: Int) {
        val total = _uiState.value.poems.size
        if (total == 0) return
        val safeIdx = index.coerceIn(0, total - 1)
        scrollSaveJob?.cancel()
        _uiState.update {
            it.copy(
                isFehristOpen = false,
                isReaderOpen = true,
                currentPoemIndex = safeIdx,
                lastSavedPoemIndex = safeIdx,
                lastSavedScrollItemIndex = 0,
                lastSavedScrollItemOffset = 0,
                readerTargetScrollItemIndex = 0,
                readerTargetScrollItemOffset = 0,
                readerScrollRestoreNonce = it.readerScrollRestoreNonce + 1
            )
        }
        viewModelScope.launch {
            prefsRepository.saveReadingPosition(safeIdx, 0, 0)
        }
    }

    fun resumeReading() {
        val state = _uiState.value
        val total = state.poems.size
        if (total == 0) return
        val safeIdx = if (state.lastSavedPoemIndex >= 0) {
            state.lastSavedPoemIndex.coerceIn(0, total - 1)
        } else {
            0
        }
        val targetItem = if (state.lastSavedPoemIndex >= 0) state.lastSavedScrollItemIndex else 0
        val targetOffset = if (state.lastSavedPoemIndex >= 0) state.lastSavedScrollItemOffset else 0

        _uiState.update {
            it.copy(
                isFehristOpen = false,
                isReaderOpen = true,
                currentPoemIndex = safeIdx,
                lastSavedPoemIndex = safeIdx,
                readerTargetScrollItemIndex = targetItem,
                readerTargetScrollItemOffset = targetOffset,
                readerScrollRestoreNonce = it.readerScrollRestoreNonce + 1
            )
        }
    }

    fun openBookmark(bookmark: BookmarkEntry) {
        val total = _uiState.value.poems.size
        if (total == 0) return
        val safeIdx = bookmark.poemIndex.coerceIn(0, total - 1)
        scrollSaveJob?.cancel()
        _uiState.update {
            it.copy(
                isFehristOpen = false,
                isReaderOpen = true,
                currentPoemIndex = safeIdx,
                lastSavedPoemIndex = safeIdx,
                lastSavedScrollItemIndex = bookmark.scrollItemIndex,
                lastSavedScrollItemOffset = bookmark.scrollItemOffset,
                readerTargetScrollItemIndex = bookmark.scrollItemIndex,
                readerTargetScrollItemOffset = bookmark.scrollItemOffset,
                readerScrollRestoreNonce = it.readerScrollRestoreNonce + 1
            )
        }
        viewModelScope.launch {
            prefsRepository.saveReadingPosition(safeIdx, bookmark.scrollItemIndex, bookmark.scrollItemOffset)
        }
    }

    fun closeReader() {
        val state = _uiState.value
        scrollSaveJob?.cancel()
        if (state.poems.isNotEmpty()) {
            viewModelScope.launch {
                prefsRepository.saveReadingPosition(
                    state.currentPoemIndex,
                    state.lastSavedScrollItemIndex,
                    state.lastSavedScrollItemOffset
                )
            }
        }
        _uiState.update { it.copy(isReaderOpen = false) }
    }

    fun selectPoemInReader(index: Int) {
        val total = _uiState.value.poems.size
        if (total == 0) return
        val safeIdx = index.coerceIn(0, total - 1)
        scrollSaveJob?.cancel()
        _uiState.update {
            it.copy(
                currentPoemIndex = safeIdx,
                lastSavedPoemIndex = safeIdx,
                lastSavedScrollItemIndex = 0,
                lastSavedScrollItemOffset = 0,
                readerTargetScrollItemIndex = 0,
                readerTargetScrollItemOffset = 0,
                readerScrollRestoreNonce = it.readerScrollRestoreNonce + 1
            )
        }
        viewModelScope.launch {
            prefsRepository.saveReadingPosition(safeIdx, 0, 0)
        }
    }

    fun updateReaderScrollPosition(poemIndex: Int, itemIndex: Int, itemOffset: Int) {
        val state = _uiState.value
        if (!state.isReaderOpen || state.currentPoemIndex != poemIndex) return
        val safeItem = itemIndex.coerceAtLeast(0)
        val safeOffset = itemOffset.coerceAtLeast(0)
        if (state.lastSavedPoemIndex == poemIndex &&
            state.lastSavedScrollItemIndex == safeItem &&
            state.lastSavedScrollItemOffset == safeOffset
        ) {
            return
        }
        _uiState.update {
            it.copy(
                lastSavedPoemIndex = poemIndex,
                lastSavedScrollItemIndex = safeItem,
                lastSavedScrollItemOffset = safeOffset
            )
        }
        scrollSaveJob?.cancel()
        scrollSaveJob = viewModelScope.launch {
            delay(300L)
            prefsRepository.saveReadingPosition(poemIndex, safeItem, safeOffset)
        }
    }

    fun toggleBookmark(itemIndex: Int, itemOffset: Int) {
        val state = _uiState.value
        val poemIdx = state.currentPoemIndex
        val existing = state.bookmarks.firstOrNull { it.poemIndex == poemIdx }
        val updated = if (existing != null) {
            state.bookmarks.filterNot { it.poemIndex == poemIdx }
        } else {
            state.bookmarks + BookmarkEntry(
                poemIndex = poemIdx,
                scrollItemIndex = itemIndex.coerceAtLeast(0),
                scrollItemOffset = itemOffset.coerceAtLeast(0),
                savedAtMillis = System.currentTimeMillis()
            )
        }
        _uiState.update { it.copy(bookmarks = updated) }
        viewModelScope.launch {
            prefsRepository.saveBookmarks(updated)
        }
        showToast(if (existing != null) "بک مارک ہٹا دیا گیا" else "بک مارک محفوظ ہو گیا")
    }

    fun increaseFontSize() {
        val next = (_uiState.value.readerFontSizeSp + 2f).coerceAtMost(40f)
        _uiState.update { it.copy(readerFontSizeSp = next) }
        viewModelScope.launch { prefsRepository.saveReaderFontSize(next) }
    }

    fun decreaseFontSize() {
        val next = (_uiState.value.readerFontSizeSp - 2f).coerceAtLeast(18f)
        _uiState.update { it.copy(readerFontSizeSp = next) }
        viewModelScope.launch { prefsRepository.saveReaderFontSize(next) }
    }

    fun toggleReaderTheme() {
        val next = !_uiState.value.isSepiaReader
        _uiState.update { it.copy(isSepiaReader = next) }
        viewModelScope.launch { prefsRepository.saveIsSepiaReader(next) }
    }

    fun openShare() {
        val poem = _uiState.value.currentPoem ?: return
        if (poem.rawText == null) return
        _uiState.update {
            it.copy(
                isShareOpen = true,
                isGeneratingCards = true,
                shareCards = emptyList()
            )
        }
        generateShareCards(poem, _uiState.value.shareThemeIndex)
    }

    fun closeShare() {
        shareJob?.cancel()
        _uiState.update { it.copy(isShareOpen = false) }
    }

    fun selectShareTheme(themeIndex: Int) {
        if (themeIndex == _uiState.value.shareThemeIndex) return
        val poem = _uiState.value.currentPoem ?: return
        _uiState.update {
            it.copy(
                shareThemeIndex = themeIndex,
                isGeneratingCards = true
            )
        }
        viewModelScope.launch {
            prefsRepository.saveShareThemeIndex(themeIndex)
        }
        generateShareCards(poem, themeIndex)
    }

    private fun generateShareCards(poem: PoemEntry, themeIndex: Int) {
        shareJob?.cancel()
        shareJob = viewModelScope.launch {
            val cards = ShareCardRenderer.renderCards(getApplication(), poem, themeIndex)
            _uiState.update {
                it.copy(
                    shareCards = cards,
                    isGeneratingCards = false
                )
            }
        }
    }

    fun shareSingleCard(pageIndex: Int) {
        val cards = _uiState.value.shareCards
        val poem = _uiState.value.currentPoem ?: return
        val card = cards.getOrNull(pageIndex) ?: return
        viewModelScope.launch {
            ShareCardRenderer.shareBitmaps(getApplication(), listOf(card), poem.title)
        }
    }

    fun shareAllCards() {
        val cards = _uiState.value.shareCards
        val poem = _uiState.value.currentPoem ?: return
        if (cards.isEmpty()) return
        viewModelScope.launch {
            ShareCardRenderer.shareBitmaps(getApplication(), cards, poem.title)
        }
    }

    fun saveCardToGallery(pageIndex: Int) {
        val cards = _uiState.value.shareCards
        val poem = _uiState.value.currentPoem ?: return
        val card = cards.getOrNull(pageIndex) ?: return
        viewModelScope.launch {
            val ok = ShareCardRenderer.saveBitmapToGallery(getApplication(), card, poem.title)
            showToast(if (ok) "تصویر گیلری میں محفوظ ہو گئی" else "تصویر محفوظ نہیں ہو سکی")
        }
    }

    fun sharePlainText() {
        val poem = _uiState.value.currentPoem ?: return
        val text = formatPoemPlainText(poem)
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(poem.title, text))

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, poem.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, poem.title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(chooser)
        }.onFailure {
            showToast("متن کاپی ہو گیا")
        }
    }

    fun copyPlainTextOnly() {
        val poem = _uiState.value.currentPoem ?: return
        val text = formatPoemPlainText(poem)
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(poem.title, text))
        showToast("متن کاپی ہو گیا")
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = message) }
        toastJob = viewModelScope.launch {
            delay(3200L)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }
}
