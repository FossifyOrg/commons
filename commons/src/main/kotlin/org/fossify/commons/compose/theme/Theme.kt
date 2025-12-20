package org.fossify.commons.compose.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import org.fossify.commons.compose.extensions.config
import org.fossify.commons.compose.theme.model.Theme
import org.fossify.commons.compose.theme.model.Theme.Companion.systemDefaultMaterialYou
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.helpers.FONT_TYPE_CUSTOM
import org.fossify.commons.helpers.FONT_TYPE_MONOSPACE
import org.fossify.commons.helpers.FontHelper
import org.fossify.commons.helpers.isSPlus

@Composable
internal fun Theme(
    theme: Theme = systemDefaultMaterialYou(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val baseConfig = remember { context.config }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val colorScheme = if (!view.isInEditMode) {
        when {
            theme is Theme.SystemDefaultMaterialYou && isSPlus() -> {
                if (isSystemInDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            theme is Theme.Custom && theme.backgroundColor.isLitWell() -> lightColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                surface = theme.backgroundColor,
                onSurface = theme.textColor,
                surfaceVariant = theme.backgroundColor
            )

            theme is Theme.Custom -> darkColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                surface = theme.backgroundColor,
                onSurface = theme.textColor,
                surfaceVariant = theme.backgroundColor
            )

            theme is Theme.Dark -> darkColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                surface = theme.backgroundColor,
                onSurface = theme.textColor
            )

            theme is Theme.White -> lightColorScheme(
                primary = Color(theme.accentColor),
                onPrimary = Color(theme.accentColor.getContrastColor()),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor
            )

            theme is Theme.BlackAndWhite -> darkColorScheme(
                primary = Color(theme.accentColor),
                onPrimary = Color(theme.accentColor.getContrastColor()),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor
            )

            else -> darkColorScheme
        }
    } else {
        previewColorScheme()
    }

    SideEffect {
        updateRecentsAppIcon(baseConfig, context)
    }

    val dimensions = CommonDimensions
    
    val customTypography = if (!view.isInEditMode) {
        getCustomTypography(context)
    } else {
        Typography()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        shapes = Shapes,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides dynamicRippleConfiguration(),
                LocalTheme provides theme,
                LocalDimensions provides dimensions
            ) {
                content()
            }
        },
    )
}

@Composable
private fun getCustomTypography(context: Context): Typography {
    val baseConfig = context.config
    val fontType = baseConfig.fontType
    
    val fontFamily = when (fontType) {
        FONT_TYPE_MONOSPACE -> FontFamily.Monospace
        FONT_TYPE_CUSTOM -> {
            val typeface = FontHelper.getTypeface(context)
            FontFamily(Typeface(typeface))
        }
        else -> FontFamily.Default
    }
    
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily),
    )
}

val LocalTheme: ProvidableCompositionLocal<Theme> =
    staticCompositionLocalOf { Theme.Custom(1, 1, 1, 1) }

@Composable
private fun previewColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme
} else {
    lightColorScheme
}

