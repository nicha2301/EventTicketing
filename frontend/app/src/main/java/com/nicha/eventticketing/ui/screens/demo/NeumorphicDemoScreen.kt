package com.nicha.eventticketing.ui.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.ui.components.inputs.NeumorphicOutlinedTextField
import com.nicha.eventticketing.ui.components.inputs.NeumorphicSearchField
import com.nicha.eventticketing.ui.components.inputs.NeumorphicTextField
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicGradientButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicTag
import com.nicha.eventticketing.ui.theme.CardBackground
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.ui.theme.Primary
import com.nicha.eventticketing.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeumorphicDemoScreen(
    onBackClick: () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    var searchQuery by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neumorphic UI Demo") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = neumorphismStyle.darkShadowColor,
                                ambientColor = neumorphismStyle.lightShadowColor
                            )
                            .clip(CircleShape)
                            .background(CardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Section: Buttons
            SectionTitle("Buttons")
            
            // Regular Button
            NeumorphicButton(
                onClick = { /* Handle click */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Neumorphic Button",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Gradient Button
            NeumorphicGradientButton(
                onClick = { /* Handle click */ },
                modifier = Modifier.fillMaxWidth(),
                gradient = Brush.horizontalGradient(
                    colors = listOf(
                        Primary,
                        Primary.copy(alpha = 0.8f)
                    )
                )
            ) {
                Text(
                    text = "Gradient Button",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Section: Cards
            SectionTitle("Cards")
            
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Neumorphic Card",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "This is a card with neumorphic design style, featuring soft shadows that create a subtle 3D effect.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Section: Tags
            SectionTitle("Tags")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeumorphicTag(
                    text = "Primary",
                    backgroundColor = Primary,
                    textColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                
                NeumorphicTag(
                    text = "Secondary",
                    backgroundColor = Secondary,
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                NeumorphicTag(
                    text = "Neutral",
                    backgroundColor = CardBackground,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Section: Text Fields
            SectionTitle("Text Fields")
            
            NeumorphicSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search...",
                modifier = Modifier.fillMaxWidth()
            )
            
            NeumorphicTextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = "Email",
                placeholder = "Enter your email",
                modifier = Modifier.fillMaxWidth()
            )
            
            NeumorphicOutlinedTextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = "Password",
                placeholder = "Enter your password",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Section: Additional Components
            SectionTitle("Additional Components")
            
            // Example of a complex card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Complex Card Example",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "This card demonstrates how neumorphic components can be combined to create rich, interactive interfaces.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeumorphicButton(
                            onClick = { /* Handle action */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Action 1",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        NeumorphicButton(
                            onClick = { /* Handle action */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Action 2",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
} 