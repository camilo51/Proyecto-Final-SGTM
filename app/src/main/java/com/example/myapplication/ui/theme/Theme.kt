package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val lightScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = AccentContainer,
    onPrimaryContainer = OnAccentContainer,
    secondary = Dark600,
    onSecondary = Color.White,
    secondaryContainer = BorderLight,
    onSecondaryContainer = TextLight,
    tertiary = Info,
    onTertiary = Color.White,
    tertiaryContainer = InfoBackground,
    onTertiaryContainer = Color(0xFF1E3A8A),
    error = Danger,
    onError = Color.White,
    errorContainer = DangerBackground,
    onErrorContainer = Color(0xFF7F1D1D),
    background = BackgroundLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = BorderLight,
    onSurfaceVariant = MutedLight,
    outline = SubtleLight,
    outlineVariant = BorderLight,
    inverseSurface = Dark800,
    inverseOnSurface = Dark300,
    inversePrimary = Color(0xFFFFB77D),
)

private val darkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Dark950,
    primaryContainer = AccentHover,
    onPrimaryContainer = Color.White,
    secondary = Dark300,
    onSecondary = Dark950,
    secondaryContainer = Dark700,
    onSecondaryContainer = Dark300,
    tertiary = Info,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = InfoBackground,
    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = DangerBackground,
    background = Dark950,
    onBackground = Dark300,
    surface = Dark900,
    onSurface = Dark300,
    surfaceVariant = Dark700,
    onSurfaceVariant = Dark400,
    outline = Dark600,
    outlineVariant = Dark700,
    inverseSurface = BackgroundLight,
    inverseOnSurface = TextLight,
    inversePrimary = AccentHover,
)

val AppShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
)

@Composable
fun AppOutlinedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkScheme else lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
