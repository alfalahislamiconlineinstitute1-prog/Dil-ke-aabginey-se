package com.example.data

import androidx.compose.ui.graphics.Color

data class BookSection(
    val title: String,
    val color: Color,
    val items: List<PoemEntry>
)

data class PoemEntry(
    val index: Int,
    val title: String,
    val pageNumber: Int,
    val category: String,
    val categoryColor: Color,
    val rawText: String?
)

data class ReadingPosition(
    val poemIndex: Int = -1,
    val scrollItemIndex: Int = 0,
    val scrollItemOffset: Int = 0
)

data class BookmarkEntry(
    val poemIndex: Int,
    val scrollItemIndex: Int,
    val scrollItemOffset: Int,
    val savedAtMillis: Long
)

sealed interface StanzaLine {
    data class Prose(val text: String) : StanzaLine
    data class Refrain(val text: String) : StanzaLine
    data class Subheading(val text: String) : StanzaLine
    data class Couplet(val first: String, val second: String) : StanzaLine
    data class Centered(val text: String) : StanzaLine
}

data class Stanza(
    val isBoxed: Boolean,
    val lines: List<StanzaLine>
)

fun parsePoemStanzas(rawText: String): List<Stanza> {
    return rawText.split("\n\n").mapNotNull { block ->
        val rawLines = block.split("\n").filter { it.isNotBlank() }
        if (rawLines.isEmpty()) return@mapNotNull null
        val isBoxed = rawLines.first().startsWith("#")
        val lines = rawLines.map { line ->
            when {
                line.startsWith("^") -> StanzaLine.Prose(line.drop(1))
                line.startsWith("~") -> StanzaLine.Refrain(line.drop(1))
                line.startsWith("#") -> StanzaLine.Subheading(line.drop(1))
                line.contains("|") -> {
                    val parts = line.split("|", limit = 2)
                    StanzaLine.Couplet(parts[0], parts.getOrElse(1) { "" })
                }
                else -> StanzaLine.Centered(line)
            }
        }
        Stanza(isBoxed = isBoxed, lines = lines)
    }
}

fun formatPoemPlainText(poem: PoemEntry): String {
    val body = poem.rawText.orEmpty().split("\n").joinToString("\n") { line ->
        when {
            line.startsWith("~") || line.startsWith("#") || line.startsWith("^") -> line.drop(1)
            else -> line.replace("|", "\n")
        }
    }
    return buildString {
        append(poem.title)
        append("\n\n")
        append(body)
        append("\n\n— نتیجۂ فکر: ")
        append("حضرت اقدس مفتی محمد شعیب اللہ خان صاحب مفتاحی دامت برکاتہم")
        append("\n")
        append("ڈیجیٹل کریٹر اینڈ ایپ ڈیولپر: فرید احمد عفی عنہ")
        append("\n(دل کے آبگینے سے)")
    }
}
