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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.example.edufeed.R
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }
    var showError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is com.example.edufeed.viewmodel.AuthUiState.Loading

    val context = LocalContext.current
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    var showRoleDialog by remember { mutableStateOf(false) }
    var googleUserId by remember { mutableStateOf<String?>(null) }
    var googleUserName by remember { mutableStateOf("") }
    var googleUserEmail by remember { mutableStateOf("") }
    var googleRole by remember { mutableStateOf("student") }
    var googleSection by remember { mutableStateOf("") }
    var googleSignInCompleted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                googleUserId = it.id
                googleUserName = it.displayName ?: ""
                googleUserEmail = it.email ?: ""
                googleSignInCompleted = true
            }
        } catch (e: ApiException) {
            // Optionally show error
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(showError) {
        showError?.let {
            snackbarHostState.showSnackbar(it)
            showError = null
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is com.example.edufeed.viewmodel.AuthUiState.Success) {
            // Navigate to dashboard after registration
            val user = (uiState as com.example.edufeed.viewmodel.AuthUiState.Success).user
            user?.let {
                if (selectedRole == "teacher") {
                    navController.navigate("teacher_dashboard/${it.uid}/$name") {
                        popUpTo("register") { inclusive = true }
                    }
                } else {
                    navController.navigate("student_dashboard/${it.uid}/$section") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            }
        } else if (uiState is com.example.edufeed.viewmodel.AuthUiState.Error) {
            showError = (uiState as com.example.edufeed.viewmodel.AuthUiState.Error).message
        }
    }

    LaunchedEffect(googleSignInCompleted) {
        if (googleSignInCompleted && googleUserId != null) {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("Users").document(googleUserId!!).get().await()
            val userRole = doc.getString("role")
            val section = doc.getString("section") ?: ""
            if (userRole == null) {
                showRoleDialog = true
            } else if (userRole == "teacher") {
                navController.navigate("teacher_dashboard/${'$'}{googleUserId}/${'$'}{googleUserName}") {
                    popUpTo("register") { inclusive = true }
                }
            } else {
                navController.navigate("student_dashboard/${'$'}{googleUserId}/${'$'}{section}") {
                    popUpTo("register") { inclusive = true }
                }
            }
            googleSignInCompleted = false
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4A90E2), Color(0xFF3F51B5)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF7C3AED)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)

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
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Register", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = accentColor) },
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
                Spacer(Modifier.height(8.dp))
                Text("Select Role:", fontWeight = FontWeight.Medium, color = textColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Student option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedRole = "student" }
                    ) {
                        RadioButton(
                            selected = selectedRole == "student",
                            onClick = { selectedRole = "student" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4A90E2))
                        )
                        Text(
                            text = "Student",
                            color = if (selectedRole == "student") Color(0xFF4A90E2) else textColor,
                            fontWeight = if (selectedRole == "student") FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(min = 60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                    // Teacher option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedRole = "teacher" }
                    ) {
                        RadioButton(
                            selected = selectedRole == "teacher",
                            onClick = { selectedRole = "teacher" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4A90E2))
                        )
                        Text(
                            text = "Teacher",
                            color = if (selectedRole == "teacher") Color(0xFF4A90E2) else textColor,
                            fontWeight = if (selectedRole == "teacher") FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(min = 60.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (selectedRole == "student") {
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section", color = accentColor) },
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
                }
                Button(
                    onClick = {
                        viewModel.register(name, email, password, selectedRole, section)
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
                    enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && (selectedRole == "teacher" || section.isNotBlank()) && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Register", color = Color.White, fontWeight = FontWeight.Bold)
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
                        Text("Sign up with Google", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate("login") { popUpTo("register") { inclusive = true } } }) {
                    Text("Already have an account? Sign In", color = accentColor)
                }
                Spacer(Modifier.height(12.dp))
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    if (showRoleDialog && googleUserId != null) {
        androidx.activity.compose.BackHandler(enabled = true) {
            showRoleDialog = false
            googleUserId = null
            googleUserName = ""
            googleUserEmail = ""
            googleRole = "student"
            googleSection = ""
        }
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text("Complete Your Profile") },
            text = {
                Column {
                    Text("Please select your role to continue:")
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = googleRole == "student",
                            onClick = { googleRole = "student" }
                        )
                        Text("Student")
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = googleRole == "teacher",
                            onClick = { googleRole = "teacher"; googleSection = "" }
                        )
                        Text("Teacher")
                    }
                    if (googleRole == "student") {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = googleSection,
                            onValueChange = { googleSection = it },
                            label = { Text("Section") },
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
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val db = FirebaseFirestore.getInstance()
                    val userData = mutableMapOf(
                        "name" to googleUserName,
                        "email" to googleUserEmail,
                        "role" to if (googleRole == "student") "student" else "teacher",
                        "section" to if (googleRole == "student") googleSection else ""
                    )
                    db.collection("Users").document(googleUserId!!)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            showRoleDialog = false
                            if (googleRole == "teacher") {
                                navController.navigate("teacher_dashboard/${'$'}{googleUserId}/${'$'}{googleUserName}") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                navController.navigate("student_dashboard/${'$'}{googleUserId}/${'$'}{googleSection}") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        }
                }, enabled = googleRole == "teacher" || (googleRole == "student" && googleSection.isNotBlank())) {
                    Text("Continue")
                }
            }
        )
    }
} 