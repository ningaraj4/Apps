package com.example.edufeed.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.edufeed.viewmodel.AuthViewModel
import com.example.edufeed.data.models.UserRole
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Animation states
    var cardVisible by remember { mutableStateOf(false) }
    
    // Collect auth state
    val uiState by authViewModel.uiState.collectAsState()
    
    // Trigger animations
    LaunchedEffect(Unit) {
        delay(200)
        cardVisible = true
    }
    
    // Responsive sizing
    val cardPadding = if (screenWidth < 360.dp) 16.dp else if (screenWidth < 600.dp) 24.dp else 32.dp
    val fieldHeight = if (screenHeight < 600.dp) 50.dp else 56.dp
    val buttonHeight = if (screenHeight < 600.dp) 50.dp else 56.dp
    val titleSize = if (screenWidth < 360.dp) 24.sp else if (screenWidth < 600.dp) 28.sp else 32.sp
    
    // Beautiful gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2),
            Color(0xFF6B73FF),
            Color(0xFF9A4DFF)
        ),
        startY = 0f,
        endY = screenHeight.value * 1.5f
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (screenWidth < 360.dp) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Spacer(modifier = Modifier.height(if (screenHeight < 600.dp) 48.dp else 64.dp))
            
            // Animated Card
            AnimatedVisibility(
                visible = cardVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        
                        // Title
                        Text(
                            text = "Sign In",
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fieldHeight),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6B73FF),
                                focusedLabelColor = Color(0xFF6B73FF),
                                cursorColor = Color(0xFF6B73FF)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fieldHeight),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6B73FF),
                                focusedLabelColor = Color(0xFF6B73FF),
                                cursorColor = Color(0xFF6B73FF)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Forgot Password Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 14.sp,
                                color = Color(0xFF6B73FF),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    // TODO: Navigate to forgot password screen
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Sign In Button
                        Button(
                            onClick = {
                                isLoading = true
                                authViewModel.login(email, password)
                            },
                            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B73FF),
                                disabledContainerColor = Color(0xFF6B73FF).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    fontSize = if (screenWidth < 360.dp) 16.sp else 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Google Sign In Button
                        OutlinedButton(
                            onClick = { /* TODO: Implement Google Sign In */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF6B73FF)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp, 
                                Color(0xFF6B73FF).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Sign in with Google",
                                fontSize = if (screenWidth < 360.dp) 16.sp else 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B73FF)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Register Link
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Don't have an account? ",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Register",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B73FF),
                                modifier = Modifier.clickable {
                                    navController.navigate("register")
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(if (screenHeight < 600.dp) 48.dp else 64.dp))
        }
    }
    
    // Handle auth state changes
    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is com.example.edufeed.viewmodel.AuthUiState.Success -> {
                isLoading = false
                val user = currentState.user
                if (user != null) {
                    // Navigate to appropriate dashboard based on user role
                    // TODO: Get user role from Firebase/database to determine navigation
                    // For now, navigate to student dashboard with default section
                    navController.navigate("student_dashboard/${user.uid}/default") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            is com.example.edufeed.viewmodel.AuthUiState.Error -> {
                isLoading = false
                // Show error message - can be enhanced with Snackbar later
            }
            is com.example.edufeed.viewmodel.AuthUiState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }
}
