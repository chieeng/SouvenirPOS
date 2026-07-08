package edu.cit.erag.souvenirpos.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brand = Color(0xFF1E3A5F)
private val BrandLight = Color(0xFF2E5A8F)
private val Accent = Color(0xFF2E7D5B)

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = BrandLight,
    tertiary = Accent,
)

private val DarkColors = darkColorScheme(
    primary = BrandLight,
    secondary = Brand,
    tertiary = Accent,
)

@Composable
fun SouvenirPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
