package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dil_ke_abginay_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LAST_POEM_INDEX = intPreferencesKey("dka2")
        val LAST_SCROLL_ITEM_INDEX = intPreferencesKey("last_scroll_item_index")
        val LAST_SCROLL_ITEM_OFFSET = intPreferencesKey("last_scroll_item_offset")
        val BOOKMARKS_JSON = stringPreferencesKey("saved_bookmarks_json")
        val SHARE_THEME_INDEX = intPreferencesKey("dkth")
        val IS_SEPIA_READER = booleanPreferencesKey("reader_sepia")
        val READER_FONT_SIZE = floatPreferencesKey("reader_font_sp")
    }

    val lastPoemIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_POEM_INDEX] ?: -1
    }

    val lastReadingPosition: Flow<ReadingPosition> = context.dataStore.data.map { prefs ->
        ReadingPosition(
            poemIndex = prefs[Keys.LAST_POEM_INDEX] ?: -1,
            scrollItemIndex = prefs[Keys.LAST_SCROLL_ITEM_INDEX] ?: 0,
            scrollItemOffset = prefs[Keys.LAST_SCROLL_ITEM_OFFSET] ?: 0
        )
    }

    val bookmarks: Flow<List<BookmarkEntry>> = context.dataStore.data.map { prefs ->
        decodeBookmarks(prefs[Keys.BOOKMARKS_JSON])
    }

    val shareThemeIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHARE_THEME_INDEX] ?: 0
    }

    val isSepiaReader: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_SEPIA_READER] ?: false
    }

    val readerFontSize: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.READER_FONT_SIZE] ?: 24f
    }

    suspend fun saveLastPoemIndex(index: Int) {
        context.dataStore.edit {
            it[Keys.LAST_POEM_INDEX] = index
            it[Keys.LAST_SCROLL_ITEM_INDEX] = 0
            it[Keys.LAST_SCROLL_ITEM_OFFSET] = 0
        }
    }

    suspend fun saveReadingPosition(poemIndex: Int, scrollItemIndex: Int, scrollItemOffset: Int) {
        context.dataStore.edit {
            it[Keys.LAST_POEM_INDEX] = poemIndex
            it[Keys.LAST_SCROLL_ITEM_INDEX] = scrollItemIndex.coerceAtLeast(0)
            it[Keys.LAST_SCROLL_ITEM_OFFSET] = scrollItemOffset.coerceAtLeast(0)
        }
    }

    suspend fun saveBookmarks(entries: List<BookmarkEntry>) {
        context.dataStore.edit {
            it[Keys.BOOKMARKS_JSON] = encodeBookmarks(entries)
        }
    }

    suspend fun saveShareThemeIndex(index: Int) {
        context.dataStore.edit { it[Keys.SHARE_THEME_INDEX] = index }
    }

    suspend fun saveIsSepiaReader(sepia: Boolean) {
        context.dataStore.edit { it[Keys.IS_SEPIA_READER] = sepia }
    }

    suspend fun saveReaderFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.READER_FONT_SIZE] = sizeSp }
    }

    private fun encodeBookmarks(entries: List<BookmarkEntry>): String {
        val arr = JSONArray()
        entries.forEach { entry ->
            arr.put(
                JSONObject().apply {
                    put("p", entry.poemIndex)
                    put("i", entry.scrollItemIndex)
                    put("o", entry.scrollItemOffset)
                    put("t", entry.savedAtMillis)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeBookmarks(raw: String?): List<BookmarkEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (idx in 0 until arr.length()) {
                    val obj = arr.getJSONObject(idx)
                    add(
                        BookmarkEntry(
                            poemIndex = obj.getInt("p"),
                            scrollItemIndex = obj.optInt("i", 0).coerceAtLeast(0),
                            scrollItemOffset = obj.optInt("o", 0).coerceAtLeast(0),
                            savedAtMillis = obj.optLong("t", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
