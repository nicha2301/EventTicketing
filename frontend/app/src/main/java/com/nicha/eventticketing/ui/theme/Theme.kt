package com.nicha.eventticketing.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Primary colors
val Primary = Color(0xFF246BFD)
val PrimaryDark = Color(0xFF1A4CCD)
val Secondary = Color(0xFFFFC700)
val AccentSkyBlue = Color(0xFFA2CCFF)

// Dark mode colors
val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF1E1E1E)
val SoftShadow = Color(0xFFD3D3D3)

// Neumorphism style configuration
data class NeumorphismStyle(
    val cornerRadius: Dp = 16.dp,
    val lightShadowColor: Color = Color.White.copy(alpha = 0.7f),
    val darkShadowColor: Color = Color.Black.copy(alpha = 0.2f),
    val shadowElevation: Dp = 6.dp,
    val backgroundColor: Color = Color.Transparent
)

// CompositionLocal for Neumorphism style
val LocalNeumorphismStyle = staticCompositionLocalOf {
    NeumorphismStyle()
}

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = AccentSkyBlue,
    background = DarkBackground,
    surface = CardBackground
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = AccentSkyBlue,
    background = Color.White,
    surface = Color.White,
    onBackground = Color(0xFF1E1E1E),
    onSurface = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFF6B7280),
    surfaceVariant = Color(0xFFF3F4F6),
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFD1D5DB)
)

@Composable
fun EventTicketingTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Set system bars color
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = true
        )
        onDispose {}
    }

    val neumorphismStyle = NeumorphismStyle(
        lightShadowColor = if (darkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(
            alpha = 0.7f
        ),
        darkShadowColor = if (darkTheme) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.15f)
    )

    CompositionLocalProvider(LocalNeumorphismStyle provides neumorphismStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun NeumorphicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current

    Surface(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        shape = shape,
        color = color,
        contentColor = contentColor,
        content = content
    )
}

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

    NeumorphicSurface(
        modifier = modifier,
        shape = RoundedCornerShape(neumorphismStyle.cornerRadius),
        color = if (isDarkTheme) CardBackground else Color.White,
        shadowElevation = neumorphismStyle.shadowElevation,
        content = content
    )
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        content = content
    )
}

@Composable
fun NeumorphicGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val shape = RoundedCornerShape(16.dp)

    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = shape
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun NeumorphicTag(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val shape = RoundedCornerShape(12.dp)
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .shadow(
                elevation = 4.dp,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        shape = shape,
        color = backgroundColor
    ) {
        Text(
            text = text,
            style = textStyle,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeumorphicSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val shape = RoundedCornerShape(16.dp)
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val bgColor = if (isDarkTheme) CardBackground else Color.White
    val modifierWithClick = if (onClick != null && readOnly) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = modifierWithClick
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            )
            .background(bgColor, shape),
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent
        ),
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = if (isDarkTheme) Color.White else Color.Gray
                )
            }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            {
                Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = if (isDarkTheme) Color.White else Color.Gray,
                    modifier = if (onTrailingIconClick != null) {
                        Modifier.clickable(onClick = onTrailingIconClick)
                    } else {
                        Modifier
                    }
                )
            }
        } else null,
        readOnly = readOnly,
        singleLine = true
    )
}