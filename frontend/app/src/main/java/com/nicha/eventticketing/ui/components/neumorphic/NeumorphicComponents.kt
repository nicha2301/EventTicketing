package com.nicha.eventticketing.ui.components.neumorphic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String? = null,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shadowColor: Color = Color.Black.copy(alpha = 0.2f)
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val shape = RoundedCornerShape(neumorphismStyle.cornerRadius)
    
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = shape,
                spotColor = shadowColor
            ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        if (icon != null && text != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = text)
            }
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        } else if (text != null) {
            Text(text = text)
        }
    }
}

@Composable
fun NeumorphicFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = CircleShape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    }
}

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    Card(
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun NeumorphicGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    contentColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    Card(
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            content()
        }
    }
}

@Composable
fun NeumorphicTag(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
    onClick: (() -> Unit)? = null
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val shape = RoundedCornerShape(12.dp)
    
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
                elevation = neumorphismStyle.shadowElevation,
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

@Composable
fun NeumorphicGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush,
    contentColor: Color = Color.White,
    text: String? = null,
    icon: ImageVector? = null
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val shape = RoundedCornerShape(neumorphismStyle.cornerRadius)
    
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
            containerColor = Color.Transparent,
            contentColor = contentColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    if (text != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NeumorphicSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    Surface(
        modifier = modifier
            .shadow(
                elevation = neumorphismStyle.shadowElevation,
                shape = shape,
                spotColor = neumorphismStyle.darkShadowColor,
                ambientColor = neumorphismStyle.lightShadowColor
            ),
        shape = shape,
        color = backgroundColor
    ) {
        content()
    }
} 