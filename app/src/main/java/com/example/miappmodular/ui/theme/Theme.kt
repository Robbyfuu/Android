package com.example.miappmodular.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Esquema de color Light - Estilo shadcn.io
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Color.White,

    secondary = Slate600,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,

    tertiary = Accent,
    onTertiary = Color.White,
    tertiaryContainer = InfoLight,
    onTertiaryContainer = Slate900,

    error = Destructive,
    onError = Color.White,
    errorContainer = DestructiveLight,
    onErrorContainer = Slate900,

    background = Background,
    onBackground = Foreground,

    surface = Surface,
    onSurface = Foreground,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = ForegroundMuted,

    outline = Border,
    outlineVariant = Slate100,

    inverseSurface = Slate900,
    inverseOnSurface = Slate50,
    inversePrimary = Slate200,

    surfaceTint = Accent,
    scrim = Color.Black.copy(alpha = 0.32f)
)

// Esquema de color Dark - Estilo shadcn.io
private val DarkColorScheme = darkColorScheme(
    primary = Slate50,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Slate50,

    secondary = Slate400,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,

    tertiary = Accent,
    onTertiary = Color.White,
    tertiaryContainer = Slate800,
    onTertiaryContainer = InfoLight,

    error = Destructive,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D), // red-900
    onErrorContainer = DestructiveLight,

    background = Slate950,
    onBackground = Slate50,

    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,

    outline = Slate700,
    outlineVariant = Slate800,

    inverseSurface = Slate50,
    inverseOnSurface = Slate900,
    inversePrimary = Slate700,

    surfaceTint = Accent,
    scrim = Color.Black.copy(alpha = 0.32f)
)

// Shapes estilo shadcn.io (esquinas sutilmente redondeadas)
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun MiAppModularTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}