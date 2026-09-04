package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val BaseTypography = Typography()

// Android uses its bundled sans-serif until an Inter .ttf is added to res/font.
val AppTypography = BaseTypography.copy(
    headlineLarge = BaseTypography.headlineLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
    ),
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = BaseTypography.titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = BaseTypography.titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = BaseTypography.labelLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    ),
)
