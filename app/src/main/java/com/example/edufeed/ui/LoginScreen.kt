package com.example.edufeed.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edufeed.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalContext
import com.example.edufeed.R
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.example.edufeed.ui.RoleButton
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var checkingGoogleUser by remember { mutableStateOf(false) }
    var showSetPasswordPrompt by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("student") }
    val isLoading = uiState is com.example.edufeed.viewmodel.AuthUiState.Loading || checkingGoogleUser

    val context = LocalContext.current
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.firebaseAuthWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            // Optionally show error
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4A90E2), Color(0xFF3F51B5)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF3F51B5)
    val disabledColor = Color(0xFFE0E7FF) // Lighter blue for disabled state
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(showError) {
        showError?.let {
            snackbarHostState.showSnackbar(it)
            showError = null
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is com.example.edufeed.viewmodel.AuthUiState.Success) {
            val user = (uiState as com.example.edufeed.viewmodel.AuthUiState.Success).user
            user?.let {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val doc = db.collection("Users").document(it.uid).get().await()
                    val userRole = doc.getString("role")
                    val section = doc.getString("section") ?: ""
                    val teacherName = doc.getString("name") ?: "Teacher"
                    Log.d("LoginScreen", "userRole=$userRole, section=$section, teacherName=$teacherName")
                    if (userRole.isNullOrEmpty()) {
                        showError = "User role not found. Please contact support."
                        return@let
                    }
                    if (userRole == "teacher") {
                        if (teacherName.isEmpty()) {
                            showError = "Teacher name not found. Please contact support."
                            return@let
                        }
                        navController.navigate("teacher_dashboard/${it.uid}/$teacherName") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else if (userRole == "student") {
                        if (section.isEmpty()) {
                            showError = "Student section not found. Please contact support."
                            return@let
                        }
                        navController.navigate("student_dashboard/${it.uid}/$section") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        showError = "Unknown user role: $userRole. Please contact support."
                    }
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Firestore error: ", e)
                    showError = "Failed to fetch user info. Please try again."
                }
            }
        } else if (uiState is com.example.edufeed.viewmodel.AuthUiState.Error) {
            // Check if this is a Google user
            checkingGoogleUser = true
            viewModel.checkIfGoogleUser(email) { isGoogle ->
                checkingGoogleUser = false
                if (isGoogle) {
                    showSetPasswordPrompt = true
                } else {
                    showError = (uiState as com.example.edufeed.viewmodel.AuthUiState.Error).message
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sign In", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = accentColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = textColor),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = accentColor,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = accentColor,
                        cursorColor = accentColor
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = accentColor) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = textColor),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = accentColor,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = accentColor,
                        cursorColor = accentColor
                    )
                )
                TextButton(onClick = { showResetDialog = true }, modifier = Modifier.align(Alignment.End)) {
                    Text("Forgot Password?", color = accentColor)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.login(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4A90E2), Color(0xFF7C3AED)),
                                startY = 0f,
                                endY = 100f
                            ),
                            shape = MaterialTheme.shapes.medium
                        ),
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4A90E2), Color(0xFF7C3AED)),
                                startY = 0f,
                                endY = 100f
                            ),
                            shape = MaterialTheme.shapes.medium
                        ),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sign in with Google", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate("register") { popUpTo("login") { inclusive = true } } }) {
                    Text("Don't have an account? Register", color = accentColor)
                }
                Spacer(Modifier.height(12.dp))
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email to receive a password reset link.")
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetMessage != null) {
                        Text(resetMessage!!, color = if (resetMessage!!.contains("sent")) Color.Green else Color.Red)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendPasswordResetEmail(resetEmail) { success, msg ->
                        resetMessage = if (success) "Reset email sent! Check your inbox." else (msg ?: "Failed to send reset email.")
                    }
                }) { Text("Send Reset Email") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false; resetMessage = null }) { Text("Cancel") }
            }
        )
    }
    if (showSetPasswordPrompt) {
        AlertDialog(
            onDismissRequest = { showSetPasswordPrompt = false },
            title = { Text("Set Password Required") },
            text = { Text("You signed up with Google and haven't set a password yet. Please set a password to log in with email and password.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendPasswordResetEmail(email) { success, msg ->
                        showSetPasswordPrompt = false
                        showError = if (success) "Password reset email sent! Check your inbox." else (msg ?: "Failed to send reset email.")
                    }
                }) { Text("Set Password") }
            },
            dismissButton = {
                TextButton(onClick = { showSetPasswordPrompt = false }) { Text("Cancel") }
            }
        )
    }
} 