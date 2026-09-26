package com.example.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =============================================================================
// MATERIAL 3 COLOR SCHEME (SCI-FI TACTICAL PALETTE)
// =============================================================================
private val HelionDarkColorScheme = darkColorScheme(
    primary = HelionCyan,
    onPrimary = HelionVoidBlack,
    primaryContainer = HelionSurfaceHigh,
    onPrimaryContainer = HelionCyanGlow,
    secondary = HelionAmber,
    onSecondary = HelionVoidBlack,
    secondaryContainer = HelionSurfaceVariant,
    onSecondaryContainer = HelionAmber,
    tertiary = HelionShieldBlue,
    onTertiary = HelionVoidBlack,
    tertiaryContainer = HelionSurfaceHigh,
    onTertiaryContainer = HelionCyanGlow,
    background = HelionVoidBlack,
    onBackground = HelionTextPrimary,
    surface = HelionDeepGraphite,
    onSurface = HelionTextPrimary,
    surfaceVariant = HelionSurface,
    onSurfaceVariant = HelionTextSecondary,
    surfaceTint = HelionCyan,
    outline = HelionBorder,
    outlineVariant = HelionBorderGlow,
    error = HelionDangerRed,
    onError = HelionVoidBlack,
    errorContainer = Color(0xFF3B0D14),
    onErrorContainer = Color(0xFFFF8A9E)
)

// =============================================================================
// MATERIAL 3 SHAPES (AEROSPACE CHAMFERED CUT CORNERS)
// =============================================================================
val HelionShapes = Shapes(
    extraSmall = CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

// =============================================================================
// EXTENDED SCI-FI HUD COLORS
// =============================================================================
@Immutable
data class HelionExtendedColors(
    val highSecGreen: Color = HelionHighSecGreen,
    val lowSecOrange: Color = HelionLowSecOrange,
    val nullSecPurple: Color = HelionNullSecPurple,
    val hazardOrange: Color = HelionHazardOrange,
    val shieldBlue: Color = HelionShieldBlue,
    val cyanGlow: Color = HelionCyanGlow,
    val borderGlow: Color = HelionBorderGlow
)

val LocalHelionExtendedColors = staticCompositionLocalOf { HelionExtendedColors() }

val MaterialTheme.extendedColors: HelionExtendedColors
    @Composable
    get() = LocalHelionExtendedColors.current

// =============================================================================
// MAIN THEME COMPOSABLE
// =============================================================================
@Composable
fun HelionTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val extendedColors = HelionExtendedColors()

    CompositionLocalProvider(LocalHelionExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = HelionDarkColorScheme,
            typography = Typography,
            shapes = HelionShapes,
            content = content
        )
    }
}
