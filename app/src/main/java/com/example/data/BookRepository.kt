package com.example.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BookRepository(private val context: Context) {

    suspend fun loadBookData(): Pair<List<BookSection>, List<PoemEntry>> =
        withContext(Dispatchers.IO) {
            val jsonString = context.assets.open("book_data.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val root = JSONObject(jsonString)
            val textsObj = root.getJSONObject("texts")
            val sectionsArray = root.getJSONArray("sections")

            val sections = mutableListOf<BookSection>()
            val flatPoems = mutableListOf<PoemEntry>()
            var globalIndex = 0

            for (i in 0 until sectionsArray.length()) {
                val secObj = sectionsArray.getJSONObject(i)
                val groupTitle = secObj.getString("g")
                val colorStr = secObj.getString("c")
                val groupColor = parseHexColor(colorStr)
                val itemsArray = secObj.getJSONArray("it")

                val sectionPoems = mutableListOf<PoemEntry>()
                for (j in 0 until itemsArray.length()) {
                    val pair = itemsArray.getJSONArray(j)
                    val title = pair.getString(0)
                    val pageNum = pair.getInt(1)
                    val rawText = textsObj.optString(pageNum.toString(), "").takeIf { it.isNotEmpty() }

                    val entry = PoemEntry(
                        index = globalIndex,
                        title = title,
                        pageNumber = pageNum,
                        category = groupTitle,
                        categoryColor = groupColor,
                        rawText = rawText
                    )
                    sectionPoems.add(entry)
                    flatPoems.add(entry)
                    globalIndex++
                }
                sections.add(
                    BookSection(
                        title = groupTitle,
                        color = groupColor,
                        items = sectionPoems
                    )
                )
            }
            sections to flatPoems
        }

    private fun parseHexColor(hex: String): Color {
        val clean = hex.removePrefix("#")
        val value = clean.toLong(16)
        return if (clean.length == 6) {
            Color(0xFF000000L or value)
        } else {
            Color(value)
        }
    }
}
