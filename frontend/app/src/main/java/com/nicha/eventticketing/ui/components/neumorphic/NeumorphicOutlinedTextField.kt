package com.nicha.eventticketing.ui.components.neumorphic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.ui.theme.CardBackground
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle

@Composable
fun NeumorphicOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF121212)
    
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(neumorphismStyle.cornerRadius),
                    spotColor = neumorphismStyle.darkShadowColor,
                    ambientColor = neumorphismStyle.lightShadowColor
                )
                .clip(RoundedCornerShape(neumorphismStyle.cornerRadius))
                .background(if (isDarkTheme) CardBackground else Color.White)
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(neumorphismStyle.cornerRadius)
                )
                .padding(16.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = if (readOnly) { _ -> } else onValueChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isDarkTheme) Color.White else Color.Black
                ),
                modifier = Modifier.fillMaxWidth(),
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation
            )
            
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (readOnly) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else if (isDarkTheme) 
                        Color.Gray 
                    else 
                        Color.Gray.copy(alpha = 0.6f)
                )
            }
        }
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
} 