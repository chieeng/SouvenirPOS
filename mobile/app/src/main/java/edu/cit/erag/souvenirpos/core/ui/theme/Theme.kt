package edu.cit.erag.souvenirpos.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Paper,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDark,
    secondary = Amber,
    onSecondary = Paper,
    secondaryContainer = AmberSoft,
    onSecondaryContainer = AmberText,
    tertiary = Green,
    onTertiary = Paper,
    background = Sand,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = SurfaceVariantSand,
    onSurfaceVariant = Muted,
    outline = Faint,
    outlineVariant = Line,
    error = Red,
    onError = Paper,
    errorContainer = RedSoft,
    onErrorContainer = Red,
)

private val DarkColors = darkColorScheme(
    primary = TealSoft,
    onPrimary = TealDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealSoft,
    secondary = Amber,
    onSecondary = Ink,
    tertiary = Green,
    error = RedSoft,
)

@Composable
fun SouvenirPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SouvenirTypography,
        content = content,
    )
}
