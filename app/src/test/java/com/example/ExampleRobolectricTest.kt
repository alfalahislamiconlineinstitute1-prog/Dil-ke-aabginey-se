package com.example

import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.BookRepository
import com.example.data.BookmarkEntry
import com.example.data.PreferencesRepository
import com.example.data.ReadingPosition
import com.example.data.StanzaLine
import com.example.data.parsePoemStanzas
import com.example.share.ShareCardRenderer
import com.example.ui.theme.toUrduDigits
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("دل کے آبگینے سے", appName)
    }

    @Test
    fun `noto nastaliq urdu regular and semibold fonts load with full resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val regular = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu)
        val semiBold = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu_semibold)
        org.junit.Assert.assertNotNull(regular)
        org.junit.Assert.assertNotNull(semiBold)
        val regularBytes = context.resources.openRawResource(R.font.noto_nastaliq_urdu).use { it.readBytes() }
        val semiBoldBytes = context.resources.openRawResource(R.font.noto_nastaliq_urdu_semibold).use { it.readBytes() }
        assertTrue(regularBytes.size > 500_000)
        assertTrue(semiBoldBytes.size > 500_000)
    }

    @Test
    fun `book repository loads all 8 sections and 97 poems`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (sections, poems) = BookRepository(context).loadBookData()
        assertEquals(8, sections.size)
        assertEquals(97, poems.size)
        assertTrue(poems.all { !it.rawText.isNullOrBlank() })
    }

    @Test
    fun `stanza parser handles boxed subheadings couplets refrains and prose`() {
        val sample = "#عنوان\nپہلا مصرعہ|دوسرا مصرعہ\n~ردیف\n\n^یہ نثری پیراگراف ہے\n\nتنہا سطر"
        val stanzas = parsePoemStanzas(sample)
        assertEquals(3, stanzas.size)
        assertTrue(stanzas[0].isBoxed)
        assertTrue(stanzas[0].lines[0] is StanzaLine.Subheading)
        assertTrue(stanzas[0].lines[1] is StanzaLine.Couplet)
        assertTrue(stanzas[0].lines[2] is StanzaLine.Refrain)
        assertTrue(stanzas[1].lines[0] is StanzaLine.Prose)
        assertTrue(stanzas[2].lines[0] is StanzaLine.Centered)
        assertEquals("۲۰۸", 208.toUrduDigits())
    }

    @Test
    fun `share card renderer builds 9_16 bitmaps`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (_, poems) = BookRepository(context).loadBookData()
        val cards = ShareCardRenderer.renderCards(context, poems.first(), 0)
        assertTrue(cards.isNotEmpty())
        assertEquals(1080, cards.first().width)
        assertEquals(1920, cards.first().height)
    }

    @Test
    fun `preferences repository persists scroll position and bookmarks in datastore`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = PreferencesRepository(context)

        repo.saveReadingPosition(poemIndex = 12, scrollItemIndex = 4, scrollItemOffset = 180)
        val savedPosition = repo.lastReadingPosition.first()
        assertEquals(ReadingPosition(poemIndex = 12, scrollItemIndex = 4, scrollItemOffset = 180), savedPosition)

        val sampleBookmarks = listOf(
            BookmarkEntry(poemIndex = 12, scrollItemIndex = 4, scrollItemOffset = 180, savedAtMillis = 123456L),
            BookmarkEntry(poemIndex = 5, scrollItemIndex = 2, scrollItemOffset = 40, savedAtMillis = 123000L)
        )
        repo.saveBookmarks(sampleBookmarks)
        val restoredBookmarks = repo.bookmarks.first()
        assertEquals(sampleBookmarks, restoredBookmarks)
    }

    @Test
    fun `home navigation opens fehrist and reader overlays and toggles bookmark`() {
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2500L, java.util.concurrent.TimeUnit.MILLISECONDS)
        composeRule.mainClock.advanceTimeBy(1000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("splash_overlay").assertDoesNotExist()
        composeRule.onNodeWithTag("home_pager_dots").assertDoesNotExist()
        composeRule.onNodeWithTag("nav_home_button").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_home_button").assertTextContains("کتاب کے بارے میں")
        composeRule.onNodeWithTag("nav_fehrist_button").assertDoesNotExist()
        composeRule.onNodeWithTag("nav_read_button").assertDoesNotExist()

        composeRule.onNodeWithTag("home_theme_toggle_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_theme_toggle_button").assertTextContains("دن", substring = true)
        composeRule.onNodeWithTag("home_theme_toggle_button").assertTextContains("رات", substring = true)
        composeRule.onNodeWithTag("home_theme_toggle_button").assert(
            androidx.compose.ui.test.SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.StateDescription,
                "رات موڈ فعال"
            )
        )

        composeRule.onNodeWithTag("home_theme_toggle_button").performClick()
        org.robolectric.shadows.ShadowLooper.idleMainLooper(200L, java.util.concurrent.TimeUnit.MILLISECONDS)
        composeRule.mainClock.advanceTimeBy(400L)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_theme_toggle_button").assert(
            androidx.compose.ui.test.SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.StateDescription,
                "دن موڈ فعال"
            )
        )

        // Toggle back to default night mode for remaining assertions
        composeRule.onNodeWithTag("home_theme_toggle_button").performClick()
        org.robolectric.shadows.ShadowLooper.idleMainLooper(200L, java.util.concurrent.TimeUnit.MILLISECONDS)
        composeRule.mainClock.advanceTimeBy(400L)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_theme_toggle_button").assert(
            androidx.compose.ui.test.SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.StateDescription,
                "رات موڈ فعال"
            )
        )

        composeRule.onNodeWithText("فاروقی", substring = true).assertDoesNotExist()
        val urduCreditTop = composeRule.onNodeWithText("ڈیجیٹل کریٹر و ایپ ڈیولپر: فرید احمد عفی عنہ").fetchSemanticsNode().positionInRoot.y
        val englishCreditTop = composeRule.onNodeWithText("Digital Creator & App Developer: Fareed Ahmad").fetchSemanticsNode().positionInRoot.y
        val initialStartTop = composeRule.onNodeWithTag("start_reading_button").fetchSemanticsNode().positionInRoot.y
        assertTrue(
            "Expected urdu($urduCreditTop) < english($englishCreditTop) < start($initialStartTop)",
            urduCreditTop < englishCreditTop && englishCreditTop < initialStartTop
        )

        // Tapping "کتاب کے بارے میں" opens the publisher/about page, and tapping again returns
        composeRule.onNodeWithTag("nav_home_button").performClick()
        composeRule.mainClock.advanceTimeBy(800L)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("جملہ حقوق بہ حق ناشر محفوظ ہیں").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_home_button").assertTextContains("واپس کتاب پر")
        composeRule.onNodeWithTag("nav_home_button").performClick()
        composeRule.mainClock.advanceTimeBy(800L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("open_fehrist_button").performScrollTo()
        composeRule.onNodeWithTag("open_fehrist_button").assertTextContains("کتاب کی فہرست دیکھیں", substring = true)
        val startTop = composeRule.onNodeWithTag("start_reading_button").fetchSemanticsNode().boundsInRoot.top
        val fehristTop = composeRule.onNodeWithTag("open_fehrist_button").fetchSemanticsNode().boundsInRoot.top
        val navHomeTop = composeRule.onNodeWithTag("nav_home_button").fetchSemanticsNode().boundsInRoot.top
        assertTrue(startTop < fehristTop && fehristTop < navHomeTop)

        composeRule.onNodeWithTag("open_fehrist_button").performClick()
        composeRule.mainClock.advanceTimeBy(600L)
        composeRule.waitForIdle()
        try {
            composeRule.onNodeWithTag("fehrist_modal").assertIsDisplayed()
        } catch (e: AssertionError) {
            val node = composeRule.onNodeWithTag("fehrist_modal").fetchSemanticsNode()
            throw AssertionError("fehrist_modal boundsInWindow=${node.boundsInWindow} boundsInRoot=${node.boundsInRoot} size=${node.size}", e)
        }

        composeRule.waitUntil(timeoutMillis = 5000L) {
            composeRule.onAllNodesWithTag("fehrist_item_0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("fehrist_item_0").performClick()
        composeRule.mainClock.advanceTimeBy(600L)
        composeRule.waitForIdle()
        try {
            composeRule.onNodeWithTag("reader_overlay").assertIsDisplayed()
        } catch (e: AssertionError) {
            val node = composeRule.onNodeWithTag("reader_overlay").fetchSemanticsNode()
            throw AssertionError("reader_overlay boundsInWindow=${node.boundsInWindow} boundsInRoot=${node.boundsInRoot} size=${node.size}", e)
        }

        composeRule.onNodeWithTag("reader_bookmark_button").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("toast_banner").assertIsDisplayed()

        composeRule.onNodeWithTag("reader_share_button").performClick()
        composeRule.mainClock.advanceTimeBy(400L)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("share_modal").assertIsDisplayed()

        composeRule.onNodeWithTag("share_close_button").performClick()
        composeRule.mainClock.advanceTimeBy(400L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("reader_close_button").performClick()
        composeRule.mainClock.advanceTimeBy(600L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_bookmark_button").performScrollTo().assertIsDisplayed()
        val fehristTopWithBookmark = composeRule.onNodeWithTag("open_fehrist_button").fetchSemanticsNode().boundsInRoot.top
        val bookmarkTop = composeRule.onNodeWithTag("home_bookmark_button").fetchSemanticsNode().boundsInRoot.top
        val navHomeTopWithBookmark = composeRule.onNodeWithTag("nav_home_button").fetchSemanticsNode().boundsInRoot.top
        assertTrue(fehristTopWithBookmark < bookmarkTop && bookmarkTop < navHomeTopWithBookmark)
    }
}
