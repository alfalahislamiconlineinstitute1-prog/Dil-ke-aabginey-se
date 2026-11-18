package com.example.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.PoemEntry
import com.example.ui.theme.toUrduDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ShareTheme(
    val name: String,
    val bgColors: IntArray,
    val glowColor: Int,
    val accentColor: Int,
    val mintColor: Int,
    val textColor: Int,
    val darkenLogo: Boolean = false
)

val SHARE_THEMES = listOf(
    ShareTheme(
        name = "زمردی سبز",
        bgColors = intArrayOf(0xFF0D4A37.toInt(), 0xFF0A2C22.toInt(), 0xFF050F0B.toInt()),
        glowColor = Color.argb(128, 23, 140, 100),
        accentColor = 0xFFE5B14A.toInt(),
        mintColor = 0xFF7FD6B4.toInt(),
        textColor = 0xFFECE8DC.toInt()
    ),
    ShareTheme(
        name = "شاہی نیلا",
        bgColors = intArrayOf(0xFF14407A.toInt(), 0xFF0B2447.toInt(), 0xFF050D1F.toInt()),
        glowColor = Color.argb(128, 40, 110, 200),
        accentColor = 0xFFF0C35E.toInt(),
        mintColor = 0xFF8CC4FF.toInt(),
        textColor = 0xFFEEF3FB.toInt()
    ),
    ShareTheme(
        name = "ارغوانی",
        bgColors = intArrayOf(0xFF51206F.toInt(), 0xFF2C0F47.toInt(), 0xFF12061F.toInt()),
        glowColor = Color.argb(128, 150, 70, 200),
        accentColor = 0xFFF2C46B.toInt(),
        mintColor = 0xFFD7A8FF.toInt(),
        textColor = 0xFFF5EDFB.toInt()
    ),
    ShareTheme(
        name = "عنابی سرخ",
        bgColors = intArrayOf(0xFF7F2230.toInt(), 0xFF4A1019.toInt(), 0xFF1F070B.toInt()),
        glowColor = Color.argb(128, 200, 60, 80),
        accentColor = 0xFFF1C36A.toInt(),
        mintColor = 0xFFFFB3BD.toInt(),
        textColor = 0xFFFBEFE9.toInt()
    ),
    ShareTheme(
        name = "سیاہ و سنہری",
        bgColors = intArrayOf(0xFF262626.toInt(), 0xFF131313.toInt(), 0xFF050505.toInt()),
        glowColor = Color.argb(128, 110, 110, 110),
        accentColor = 0xFFE5B14A.toInt(),
        mintColor = 0xFFC9C1A8.toInt(),
        textColor = 0xFFF3EFE4.toInt()
    ),
    ShareTheme(
        name = "فیروزی",
        bgColors = intArrayOf(0xFF10636D.toInt(), 0xFF093A41.toInt(), 0xFF04181B.toInt()),
        glowColor = Color.argb(128, 30, 170, 180),
        accentColor = 0xFFF2C768.toInt(),
        mintColor = 0xFF8FE6E0.toInt(),
        textColor = 0xFFECF8F7.toInt()
    ),
    ShareTheme(
        name = "کریم (روشن)",
        bgColors = intArrayOf(0xFFFBF3DF.toInt(), 0xFFF1E4C3.toInt(), 0xFFE6D3A6.toInt()),
        glowColor = Color.argb(128, 255, 255, 255),
        accentColor = 0xFF8A5200.toInt(),
        mintColor = 0xFF2F6F56.toInt(),
        textColor = 0xFF2B2418.toInt(),
        darkenLogo = true
    )
)

object ShareCardRenderer {

    private const val W = 1080
    private const val H = 1920
    private const val CR1 = "حضرت اقدس مفتی محمد شعیب اللہ خان صاحب مفتاحی دامت برکاتہم"
    private const val CR2 = "ڈیجیٹل کریٹر اینڈ ایپ ڈیولپر: فرید احمد عفی عنہ"

    private sealed class MeasuredBlock(val height: Int) {
        class Couplet(val a: StaticLayout, val b: StaticLayout, val aH: Int, h: Int) : MeasuredBlock(h)
        class Centered(val layout: StaticLayout, h: Int) : MeasuredBlock(h)
        class Refrain(val layout: StaticLayout, h: Int) : MeasuredBlock(h)
        class Heading(val layout: StaticLayout, h: Int) : MeasuredBlock(h)
        class Prose(val layout: StaticLayout, h: Int) : MeasuredBlock(h)
        class Gap(h: Int = 34) : MeasuredBlock(h)
    }

    suspend fun renderCards(
        context: Context,
        poem: PoemEntry,
        themeIndex: Int
    ): List<Bitmap> = withContext(Dispatchers.Default) {
        val rawText = poem.rawText ?: return@withContext emptyList()
        val theme = SHARE_THEMES.getOrElse(themeIndex) { SHARE_THEMES[0] }
        val typeface = ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu) ?: Typeface.SERIF
        val typefaceSemiBold = ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu_semibold)
            ?: Typeface.create(typeface, Typeface.BOLD)
        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_maktaba)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typefaceSemiBold
            textSize = 64f
            color = theme.accentColor
        }
        val titleLayout = buildStaticLayout(poem.title, titlePaint, 860, Layout.Alignment.ALIGN_CENTER)
        val titleTop = 278f
        val hdrBottom = (titleTop + titleLayout.height).toInt().coerceAtLeast(370)
        val divY = hdrBottom + 22f
        val bodyTop = (divY + 42f).toInt()
        val bodyBot = H - 390
        val bodyH = (bodyBot - bodyTop).coerceAtLeast(400)

        val coupletPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = 40f
            color = theme.textColor
        }
        val refrainPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = 36f
            color = theme.accentColor
        }
        val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typefaceSemiBold
            textSize = 36f
            color = theme.accentColor
        }
        val prosePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = 34f
            color = theme.textColor
        }

        val blocks = mutableListOf<MeasuredBlock>()
        rawText.split("\n\n").forEach { stanza ->
            stanza.split("\n").filter { it.isNotBlank() }.forEach { line ->
                when {
                    line.startsWith("~") -> {
                        val sl = buildStaticLayout(line.drop(1), refrainPaint, 840, Layout.Alignment.ALIGN_CENTER)
                        blocks.add(MeasuredBlock.Refrain(sl, sl.height + 10))
                    }
                    line.startsWith("#") -> {
                        val sl = buildStaticLayout(line.drop(1), headingPaint, 840, Layout.Alignment.ALIGN_CENTER)
                        blocks.add(MeasuredBlock.Heading(sl, sl.height + 20))
                    }
                    line.startsWith("^") -> {
                        val words = line.drop(1).split(Regex("\\s+")).filter { it.isNotBlank() }
                        val chunkSize = 75
                        var idx = 0
                        while (idx < words.size) {
                            val chunk = words.subList(idx, minOf(idx + chunkSize, words.size)).joinToString(" ")
                            val sl = buildStaticLayout(chunk, prosePaint, 840, Layout.Alignment.ALIGN_NORMAL)
                            blocks.add(MeasuredBlock.Prose(sl, sl.height + 16))
                            idx += chunkSize
                        }
                    }
                    line.contains("|") -> {
                        val parts = line.split("|", limit = 2)
                        val slA = buildStaticLayout(parts[0], coupletPaint, 840, Layout.Alignment.ALIGN_NORMAL)
                        val slB = buildStaticLayout(parts.getOrElse(1) { "" }, coupletPaint, 840, Layout.Alignment.ALIGN_OPPOSITE)
                        blocks.add(MeasuredBlock.Couplet(slA, slB, slA.height, slA.height + slB.height + 24))
                    }
                    else -> {
                        val sl = buildStaticLayout(line, coupletPaint, 840, Layout.Alignment.ALIGN_CENTER)
                        blocks.add(MeasuredBlock.Centered(sl, sl.height + 10))
                    }
                }
            }
            blocks.add(MeasuredBlock.Gap())
        }

        val pages = mutableListOf<MutableList<MeasuredBlock>>(mutableListOf())
        var usedH = 0
        for (block in blocks) {
            val currentPage = pages.last()
            if (usedH + block.height > bodyH && currentPage.isNotEmpty()) {
                pages.add(mutableListOf())
                usedH = 0
            }
            val targetPage = pages.last()
            if (targetPage.isEmpty() && block is MeasuredBlock.Gap) continue
            targetPage.add(block)
            usedH += block.height
        }
        pages.forEach { p ->
            while (p.isNotEmpty() && p.last() is MeasuredBlock.Gap) {
                p.removeAt(p.lastIndex)
            }
        }
        if (pages.size > 1 && pages.last().isEmpty()) {
            pages.removeAt(pages.lastIndex)
        }

        pages.mapIndexed { pageIdx, pageBlocks ->
            val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawBackground(canvas, theme)

            // Draw Logo
            if (logoBitmap != null) {
                val lw = 130f
                val lh = lw * logoBitmap.height / logoBitmap.width.toFloat()
                val left = (W - lw) / 2f
                val top = 86f
                val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    alpha = 242
                    if (theme.darkenLogo) {
                        val cm = ColorMatrix().apply {
                            setScale(0.45f, 0.45f, 0.45f, 1f)
                        }
                        colorFilter = ColorMatrixColorFilter(cm)
                    }
                }
                canvas.drawBitmap(logoBitmap, null, RectF(left, top, left + lw, top + lh), logoPaint)
            }

            // Category Subtitle
            val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                textSize = 32f
                color = withAlpha(theme.mintColor, 0.9f)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(poem.category, W / 2f, 258f, catPaint)

            // Poem Title
            canvas.save()
            canvas.translate(110f, titleTop)
            titleLayout.draw(canvas)
            canvas.restore()

            // Ornament Divider
            val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = withAlpha(theme.accentColor, 0.55f)
                strokeWidth = 2f
            }
            canvas.drawLine(300f, divY, 500f, divY, divPaint)
            canvas.drawLine(580f, divY, 780f, divY, divPaint)
            val ornPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = Typeface.SERIF
                textSize = 26f
                color = theme.accentColor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("❖", W / 2f, divY + 9f, ornPaint)

            // Draw Body Stanzas
            var cy = bodyTop.toFloat()
            val sepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = withAlpha(theme.accentColor, 0.55f)
            }
            pageBlocks.forEach { block ->
                when (block) {
                    is MeasuredBlock.Couplet -> {
                        canvas.save()
                        canvas.translate(120f, cy)
                        block.a.draw(canvas)
                        canvas.translate(0f, block.aH.toFloat())
                        block.b.draw(canvas)
                        canvas.restore()
                        canvas.drawRect(W / 2f - 24f, cy + block.height - 16f, W / 2f + 24f, cy + block.height - 14f, sepPaint)
                    }
                    is MeasuredBlock.Centered -> {
                        canvas.save()
                        canvas.translate(120f, cy)
                        block.layout.draw(canvas)
                        canvas.restore()
                    }
                    is MeasuredBlock.Refrain -> {
                        canvas.save()
                        canvas.translate(120f, cy)
                        block.layout.draw(canvas)
                        canvas.restore()
                    }
                    is MeasuredBlock.Heading -> {
                        canvas.save()
                        canvas.translate(120f, cy)
                        block.layout.draw(canvas)
                        canvas.restore()
                    }
                    is MeasuredBlock.Prose -> {
                        canvas.save()
                        canvas.translate(120f, cy)
                        block.layout.draw(canvas)
                        canvas.restore()
                    }
                    is MeasuredBlock.Gap -> Unit
                }
                cy += block.height
            }

            // Footer
            val fy = (H - 384).toFloat()
            val fLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = withAlpha(theme.accentColor, 0.5f)
                strokeWidth = 2f
            }
            canvas.drawLine(200f, fy, 880f, fy, fLinePaint)

            val fLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                textSize = 30f
                color = withAlpha(theme.mintColor, 0.9f)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("نتیجۂ فکر", W / 2f, fy + 52f, fLabelPaint)

            val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typefaceSemiBold
                textSize = 34f
                color = withAlpha(theme.textColor, 0.95f)
            }
            val authorLayout = buildStaticLayout(CR1, authorPaint, 900, Layout.Alignment.ALIGN_CENTER)
            canvas.save()
            canvas.translate(90f, fy + 66f)
            authorLayout.draw(canvas)
            canvas.restore()

            val cr2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                textSize = 28f
                color = withAlpha(theme.accentColor, 0.88f)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(CR2, W / 2f, H - 114f, cr2Paint)

            val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                textSize = 24f
                color = withAlpha(theme.textColor, 0.55f)
                textAlign = Paint.Align.CENTER
            }
            val footerPage = "دل کے آبگینے سے  ·  ${(pageIdx + 1).toUrduDigits()} / ${pages.size.toUrduDigits()}"
            canvas.drawText(footerPage, W / 2f, H - 64f, pagePaint)

            bmp
        }
    }

    private fun drawBackground(canvas: Canvas, theme: ShareTheme) {
        val grad = LinearGradient(
            0f, 0f, 0f, H.toFloat(),
            theme.bgColors,
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), Paint().apply { shader = grad })

        val glow = RadialGradient(
            W / 2f, 260f, 760f,
            intArrayOf(theme.glowColor, Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), Paint().apply { shader = glow })

        // Geometric Islamic lattice pattern
        val patPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(theme.accentColor, 0.075f)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        var y = -40
        while (y < H + 80) {
            val stagger = if (Math.round(y / 140f) % 2 != 0) 70 else 0
            var x = -40
            while (x < W + 80) {
                canvas.save()
                canvas.translate((x + stagger).toFloat(), y.toFloat())
                canvas.drawRect(-36f, -36f, 36f, 36f, patPaint)
                canvas.rotate(45f)
                canvas.drawRect(-36f, -36f, 36f, 36f, patPaint)
                canvas.restore()
                x += 140
            }
            y += 140
        }

        // Outer solid border
        val outerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(theme.accentColor, 0.9f)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(RectF(34f, 34f, W - 34f, H - 34f), 38f, 38f, outerBorder)

        // Inner dashed border
        val innerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(theme.accentColor, 0.35f)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        canvas.drawRoundRect(RectF(56f, 56f, W - 56f, H - 56f), 28f, 28f, innerBorder)
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment
    ): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(true)
            .build()
    }

    private fun withAlpha(color: Int, alphaFraction: Float): Int {
        val a = (alphaFraction.coerceIn(0f, 1f) * 255).toInt()
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color))
    }

    suspend fun shareBitmaps(context: Context, bitmaps: List<Bitmap>, title: String) {
        if (bitmaps.isEmpty()) return
        val uris = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "shared_cards").apply {
                mkdirs()
                listFiles()?.forEach { it.delete() }
            }
            bitmaps.mapIndexed { idx, bmp ->
                val file = File(dir, "dil-ke-abginay-${idx + 1}.jpg")
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 93, out)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        }

        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uris.first())
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title — دل کے آبگینے سے")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/jpeg"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title — دل کے آبگینے سے")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val filename = "DilKeAbginay_${System.currentTimeMillis()}.jpg"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.TITLE, title)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DilKeAbginay")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val resolver = context.contentResolver
                val uri: Uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext false
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                true
            }.getOrDefault(false)
        }
}
