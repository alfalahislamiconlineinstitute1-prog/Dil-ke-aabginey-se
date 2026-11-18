package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val UrduNastaliqFontFamily = FontFamily(
    Font(R.font.noto_nastaliq_urdu, FontWeight.Normal),
    Font(R.font.noto_nastaliq_urdu, FontWeight.Medium),
    Font(R.font.noto_nastaliq_urdu_semibold, FontWeight.SemiBold),
    Font(R.font.noto_nastaliq_urdu_semibold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 68.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 54.sp
    ),
    titleLarge = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 46.sp
    ),
    titleMedium = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 38.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 38.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 32.sp
    ),
    labelLarge = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 32.sp
    ),
    labelSmall = TextStyle(
        fontFamily = UrduNastaliqFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 26.sp
    )
)

private val URDU_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun Int.toUrduDigits(): String {
    return this.toString().map { ch ->
        if (ch in '0'..'9') URDU_DIGITS[ch - '0'] else ch
    }.joinToString("")
}
