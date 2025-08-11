package com.nicha.eventticketing.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppOutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.R
import com.nicha.eventticketing.data.auth.GoogleAuthManager
import com.nicha.eventticketing.data.auth.GoogleSignInResult
import com.nicha.eventticketing.ui.theme.BrandOrange
import com.nicha.eventticketing.util.ValidationUtils
import com.nicha.eventticketing.viewmodel.AuthState
import com.nicha.eventticketing.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    googleAuthManager: GoogleAuthManager = GoogleAuthManager(
        LocalContext.current
    )
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val signInResult = googleAuthManager.handleSignInResult(result)
        when (signInResult) {
            is GoogleSignInResult.Success -> {
                val account = signInResult.account
                viewModel.loginWithGoogle(
                    idToken = account.idToken ?: "",
                    email = account.email ?: "",
                    name = account.displayName ?: "",
                    profilePictureUrl = account.photoUrl?.toString()
                )
            }

            is GoogleSignInResult.Error -> {
                Toast.makeText(
                    context,
                    "Google Sign-In failed: ${signInResult.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is GoogleSignInResult.Cancelled -> {
                Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                onRegisterSuccess()
            }

            is AuthState.RegistrationSuccess -> {
                onRegisterSuccess()
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
            // Top Navigation with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Header Section
                Column {
                    Text(
                        text = "Sign up to Vevent !",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF19171C),
                        letterSpacing = (-0.8).sp
                    )
                }

                // Welcome Text
                Text(
                    text = "We are glad to have you",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8C8CA1)
                )

                // Registration Form Fields
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Full Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Full Name",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8C8CA1)
                        )

                        androidx.compose.material3.TextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                fullNameError = null
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your full name",
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            isError = fullNameError != null,
                            singleLine = true
                        )

                        if (fullNameError != null) {
                            Text(
                                text = fullNameError!!,
                                color = Color(0xFFE74C3C),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

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
                                imeAction = ImeAction.Next
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

                    // Phone Number Field
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Phone Number",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8C8CA1)
                        )

                        TextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                phoneNumberError = null
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your phone number",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF19171C).copy(alpha = 0.4f)
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF19171C)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                                unfocusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            isError = phoneNumberError != null,
                            singleLine = true
                        )

                        if (phoneNumberError != null) {
                            Text(
                                text = phoneNumberError!!,
                                color = Color(0xFFE74C3C),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    // Password Field
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8C8CA1)
                        )

                        TextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = null
                                if (confirmPassword.isNotEmpty()) {
                                    confirmPasswordError =
                                        ValidationUtils.validateConfirmPassword(confirmPassword, it)
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your password",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF19171C).copy(alpha = 0.4f)
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF19171C)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                                unfocusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            isError = passwordError != null,
                            singleLine = true
                        )

                        if (passwordError != null) {
                            Text(
                                text = passwordError!!,
                                color = Color(0xFFE74C3C),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    // Confirm Password Field
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Confirm Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8C8CA1)
                        )

                        TextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError =
                                    ValidationUtils.validateConfirmPassword(it, password)
                            },
                            placeholder = {
                                Text(
                                    text = "Confirm your password",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF19171C).copy(alpha = 0.4f)
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF19171C)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                                unfocusedIndicatorColor = Color(0xFFCECEE0).copy(alpha = 0.5f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            isError = confirmPasswordError != null,
                            singleLine = true
                        )

                        if (confirmPasswordError != null) {
                            Text(
                                text = confirmPasswordError!!,
                                color = Color(0xFFE74C3C),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                // Terms and Conditions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = BrandOrange,
                            uncheckedColor = Color(0xFF8C8CA1)
                        ),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "By signing up, I confirm I accept the Terms of Use",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF19171C),
                        letterSpacing = 0.42.sp
                    )
                }

                // Social Sign Up Buttons
                Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                    // Google Sign Up
                    AppOutlinedButton(
                        onClick = {
                            googleSignInLauncher.launch(googleAuthManager.getSignInIntent())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign up with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF120D26).copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Sign Up Button
                AppButton(
                    onClick = {
                        fullNameError = ValidationUtils.validateFullName(fullName)
                        emailError = ValidationUtils.validateEmail(email)
                        phoneNumberError = ValidationUtils.validatePhoneNumber(phoneNumber)
                        passwordError = ValidationUtils.validatePassword(password)
                        confirmPasswordError =
                            ValidationUtils.validateConfirmPassword(confirmPassword, password)

                        if (fullNameError == null && emailError == null && phoneNumberError == null &&
                            passwordError == null && confirmPasswordError == null && acceptTerms
                        ) {
                            viewModel.register(fullName, email, password, phoneNumber)
                        } else if (!acceptTerms) {
                            Toast.makeText(
                                context,
                                "Please accept the Terms of Use",
                                Toast.LENGTH_SHORT
                            ).show()
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
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "SIGN UP",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.72.sp
                        )
                    }
                }

                // Login Link
                Text(
                    text = "Got an account? Log in",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8C8CA1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLogin() }
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
} 