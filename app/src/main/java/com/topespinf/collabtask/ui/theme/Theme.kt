package com.topespinf.collabtask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    surface = ScreenBackground
)

private val DarkColors = darkColorScheme(
    primary = Secondary,
    secondary = Primary
)

@Composable
fun CollabTaskTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}

