package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = DarkSageOnPrimary,
    primaryContainer = DarkSagePrimaryContainer,
    onPrimaryContainer = DarkSageOnPrimaryContainer,
    secondary = DarkSandSecondary,
    onSecondary = DarkSandOnSecondary,
    secondaryContainer = DarkSandSecondaryContainer,
    onSecondaryContainer = DarkSandOnSecondaryContainer,
    tertiary = DarkMistTertiary,
    onTertiary = DarkMistOnTertiary,
    tertiaryContainer = DarkMistTertiaryContainer,
    onTertiaryContainer = DarkMistOnTertiaryContainer,
    background = DarkCalmaBackground,
    onBackground = DarkCalmaOnBackground,
    surface = DarkCalmaSurface,
    onSurface = DarkCalmaOnSurface,
    surfaceVariant = DarkCalmaSurfaceVariant,
    onSurfaceVariant = DarkCalmaOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = SandSecondary,
    onSecondary = SandOnSecondary,
    secondaryContainer = SandSecondaryContainer,
    onSecondaryContainer = SandOnSecondaryContainer,
    tertiary = MistTertiary,
    onTertiary = MistOnTertiary,
    tertiaryContainer = MistTertiaryContainer,
    onTertiaryContainer = MistOnTertiaryContainer,
    background = CalmaBackground,
    onBackground = CalmaOnBackground,
    surface = CalmaSurface,
    onSurface = CalmaOnSurface,
    surfaceVariant = CalmaSurfaceVariant,
    onSurfaceVariant = CalmaOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Calm branding coherent by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
