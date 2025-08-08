package com.nicha.eventticketing.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.R
import com.nicha.eventticketing.ui.theme.BrandOrange
import com.nicha.eventticketing.util.ValidationUtils
import com.nicha.eventticketing.viewmodel.AuthState
import com.nicha.eventticketing.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetLinkSent: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.ForgotPasswordSuccess -> {
                onResetLinkSent()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E1E1E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Header Section
                Column {
                    Text(
                        text = "Forgot Password?",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF19171C),
                        letterSpacing = (-0.8).sp
                    )
                }
                
                // Description Text
                Text(
                    text = "Don't worry! It happens. Please enter the email address associated with your account.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8C8CA1),
                    lineHeight = 26.sp
                )
                
                // Email Field
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Email",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8C8CA1)
                    )
                    
                    androidx.compose.material3.TextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = null
                        },
                        placeholder = {
                            Text(
                                text = "sample@gmail.com",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF19171C).copy(alpha = 0.4f)
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF19171C)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (email.isNotEmpty() && ValidationUtils.validateEmail(email) == null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check_green),
                                    contentDescription = "Valid email",
                                    tint = Color(0xFF25D36C),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        isError = emailError != null,
                        singleLine = true
                    )
                    
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = Color(0xFFE74C3C),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                
                // Success Message
                if (authState is AuthState.ForgotPasswordSuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFE8F5E8),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check_green),
                                    contentDescription = "Success",
                                    tint = Color(0xFF25D36C),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Reset link sent!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF19171C)
                                )
                            }
                            
                            Text(
                                text = "We've sent a password reset link to your email address. Please check your inbox and follow the instructions to reset your password.",
                                fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8C8CA1),
                                    lineHeight = 20.sp
                            )
                        }
                    }
                }
                
                // Submit Button
                Button(
                    onClick = {
                        emailError = ValidationUtils.validateEmail(email)
                        
                        if (emailError == null) {
                            viewModel.forgotPassword(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = BrandOrange.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandOrange
                    ),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "SEND RESET LINK",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.72.sp
                        )
                    }
                }
                
                // Back to Login Link
                Text(
                    text = "Back to Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8C8CA1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
} 