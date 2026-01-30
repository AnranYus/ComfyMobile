package moe.uni.comfyKmp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Light Mode Colors - Blue 蓝色系
private val LightPrimary = Color(0xFF2563EB)          // Blue 600
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFDBEAFE) // Blue 100
private val LightOnPrimaryContainer = Color(0xFF1E3A8A) // Blue 900
private val LightSecondary = Color(0xFF3B82F6)        // Blue 500
private val LightOnSecondary = Color.White
private val LightSecondaryContainer = Color(0xFFBFDBFE) // Blue 200
private val LightOnSecondaryContainer = Color(0xFF1E40AF) // Blue 800
private val LightTertiary = Color(0xFF8B5CF6)         // Violet 500
private val LightOnTertiary = Color.White
private val LightTertiaryContainer = Color(0xFFEDE9FE) // Violet 100
private val LightOnTertiaryContainer = Color(0xFF5B21B6) // Violet 800
private val LightBackground = Color(0xFFF8FAFC)       // Slate 50
private val LightOnBackground = Color(0xFF1E293B)     // Slate 800
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1E293B)        // Slate 800
private val LightSurfaceVariant = Color(0xFFF1F5F9)   // Slate 100
private val LightOnSurfaceVariant = Color(0xFF475569) // Slate 600
private val LightOutline = Color(0xFFCBD5E1)          // Slate 300
private val LightOutlineVariant = Color(0xFFE2E8F0)   // Slate 200
private val LightError = Color(0xFFDC2626)            // Red 600
private val LightOnError = Color.White

// Dark Mode Colors - Blue 蓝色系
private val DarkPrimary = Color(0xFF60A5FA)           // Blue 400
private val DarkOnPrimary = Color(0xFF1E3A8A)         // Blue 900
private val DarkPrimaryContainer = Color(0xFF1E40AF) // Blue 800
private val DarkOnPrimaryContainer = Color(0xFFDBEAFE) // Blue 100
private val DarkSecondary = Color(0xFF93C5FD)         // Blue 300
private val DarkOnSecondary = Color(0xFF1E3A8A)       // Blue 900
private val DarkSecondaryContainer = Color(0xFF1D4ED8) // Blue 700
private val DarkOnSecondaryContainer = Color(0xFFBFDBFE) // Blue 200
private val DarkTertiary = Color(0xFFA78BFA)          // Violet 400
private val DarkOnTertiary = Color(0xFF4C1D95)        // Violet 900
private val DarkTertiaryContainer = Color(0xFF6D28D9) // Violet 700
private val DarkOnTertiaryContainer = Color(0xFFEDE9FE) // Violet 100
private val DarkBackground = Color(0xFF0F172A)        // Slate 900
private val DarkOnBackground = Color(0xFFE2E8F0)      // Slate 200
private val DarkSurface = Color(0xFF1E293B)           // Slate 800
private val DarkOnSurface = Color(0xFFE2E8F0)         // Slate 200
private val DarkSurfaceVariant = Color(0xFF334155)    // Slate 700
private val DarkOnSurfaceVariant = Color(0xFF94A3B8)  // Slate 400
private val DarkOutline = Color(0xFF475569)           // Slate 600
private val DarkOutlineVariant = Color(0xFF334155)    // Slate 700
private val DarkError = Color(0xFFF87171)             // Red 400
private val DarkOnError = Color(0xFF7F1D1D)           // Red 900

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError
)

private val ComfyTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val ComfyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Extended colors for custom UI elements
@Immutable
data class ComfyExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val nodeRunning: Color,
    val nodeCompleted: Color,
    val nodePending: Color
)

private val LightExtendedColors = ComfyExtendedColors(
    success = Color(0xFF10B981),
    onSuccess = Color.White,
    successContainer = Color(0xFFD1FAE5),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF1A1A1A),
    warningContainer = Color(0xFFFEF3C7),
    cardBackground = Color.White,
    cardBorder = Color(0xFFE2E8F0),          // Slate 200
    shimmerBase = Color(0xFFE2E8F0),         // Slate 200
    shimmerHighlight = Color(0xFFF1F5F9),    // Slate 100
    gradientStart = Color(0xFFF8FAFC),       // Slate 50
    gradientEnd = Color(0xFFF1F5F9),         // Slate 100
    nodeRunning = Color(0xFF2563EB),         // Blue 600
    nodeCompleted = Color(0xFF10B981),
    nodePending = Color(0xFF94A3B8)          // Slate 400
)

private val DarkExtendedColors = ComfyExtendedColors(
    success = Color(0xFF34D399),
    onSuccess = Color(0xFF052E16),
    successContainer = Color(0xFF065F46),
    warning = Color(0xFFFBBF24),
    onWarning = Color(0xFF1A1A1A),
    warningContainer = Color(0xFF78350F),
    cardBackground = Color(0xFF1E293B),      // Slate 800
    cardBorder = Color(0xFF475569),          // Slate 600
    shimmerBase = Color(0xFF334155),         // Slate 700
    shimmerHighlight = Color(0xFF475569),    // Slate 600
    gradientStart = Color(0xFF0F172A),       // Slate 900
    gradientEnd = Color(0xFF1E293B),         // Slate 800
    nodeRunning = Color(0xFF60A5FA),         // Blue 400
    nodeCompleted = Color(0xFF34D399),
    nodePending = Color(0xFF64748B)          // Slate 500
)

val LocalComfyColors = staticCompositionLocalOf { LightExtendedColors }

// Animation easings
object ComfyEasing {
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val standardDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
    val standardAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)
}

// Spacing constants
object ComfySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

@Composable
fun ComfyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalComfyColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ComfyTypography,
            shapes = ComfyShapes,
            content = content
        )
    }
}

// Helper extension to access extended colors
val MaterialTheme.comfyColors: ComfyExtendedColors
    @Composable
    get() = LocalComfyColors.current
