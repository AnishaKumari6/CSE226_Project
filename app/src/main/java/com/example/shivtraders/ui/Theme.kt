
package com.shivtraders.inventory.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary Burgundy Palette
val Burgundy = Color(0xFF8C2946)
val BurgundyDark = Color(0xFF702039)
val BurgundyLight = Color(0xFFA94C66)
val BurgundySoft = Color(0xFFF5E7EB)

// Backgrounds
val Canvas = Color(0xFFF7F3F1)
val SurfaceWhite = Color(0xFFFFFFFF)
val RoseSurface = Color(0xFFF5EFEE)
val RoseBorder = Color(0xFFE9DDE0)

// Typography
val Ink = Color(0xFF282326)
val Muted = Color(0xFF898084)
val SoftInk = Color(0xFF514449)

// Borders
val BorderSoft = Color(0xFFEAE2E2)

// Status Colors
val Emerald = Color(0xFF39765D)
val EmeraldLight = Color(0xFFE8F1EB)

val Warning = Color(0xFFB77A31)
val WarningLight = Color(0xFFFFF3E2)

val Danger = Color(0xFFB84C49)
val DangerLight = Color(0xFFFBECEB)

// Material Color Scheme
private val AppColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,

    primaryContainer = BurgundySoft,
    onPrimaryContainer = BurgundyDark,

    secondary = BurgundyLight,
    onSecondary = Color.White,

    secondaryContainer = RoseSurface,
    onSecondaryContainer = BurgundyDark,

    tertiary = BurgundyDark,
    onTertiary = Color.White,

    background = Canvas,
    onBackground = Ink,

    surface = SurfaceWhite,
    onSurface = Ink,

    surfaceVariant = RoseSurface,
    onSurfaceVariant = SoftInk,

    outline = BorderSoft,
    outlineVariant = RoseBorder,

    error = Danger,
    onError = Color.White,
    errorContainer = DangerLight,
    onErrorContainer = Danger
)

@Composable
fun ShivTradersTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
