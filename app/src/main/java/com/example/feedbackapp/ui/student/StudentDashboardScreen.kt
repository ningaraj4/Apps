package com.example.feedbackapp.ui.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.text.input.KeyboardOptions

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.feedbackapp.viewmodel.StudentViewModel
import com.example.feedbackapp.viewmodel.StudentUiState
import com.example.feedbackapp.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.feedbackapp.R
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.InputStream
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import android.content.Context
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavController,
    studentId: String,
    section: String,
    viewModel: StudentViewModel,
    authViewModel: AuthViewModel
) {
    var code by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var studentRole by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Profile picture state
    var profilePicPath by remember { mutableStateOf<String?>(null) }
    val dataStore = context.applicationContext.dataStore
    val scope = rememberCoroutineScope()
    // Load persisted profile picture path
    LaunchedEffect(studentId) {
        val prefs = dataStore.data.first()
        prefs[stringPreferencesKey("profile_pic_path_$studentId")]?.let {
            profilePicPath = it
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Copy image to internal storage
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_pic_student_$studentId.jpg")
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            profilePicPath = file.absolutePath
            // Persist file path in DataStore
            scope.launch {
                dataStore.edit { prefs ->
                    prefs[stringPreferencesKey("profile_pic_path_$studentId")] = file.absolutePath
                }
            }
        } else {
            profilePicPath = null
            scope.launch {
                dataStore.edit { prefs ->
                    prefs.remove(stringPreferencesKey("profile_pic_path_$studentId"))
                }
            }
        }
    }

    LaunchedEffect(studentId) {
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("Users").document(studentId).get().await()
        studentName = doc.getString("name") ?: ""
        studentEmail = doc.getString("email") ?: ""
        studentRole = doc.getString("role") ?: "student"
        loading = false
    }

    // Handle UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is StudentUiState.SessionLoaded -> {
                val session = (uiState as StudentUiState.SessionLoaded).session
                navController.navigate("student_feedback/${'$'}{session.sessionId}/$studentId")
            }
            is StudentUiState.Error -> {
                val errorMsg = (uiState as StudentUiState.Error).message
                showError = if (errorMsg.contains("Section mismatch", ignoreCase = true)) {
                    "You cannot join this session. It is only for students of section: $section."
                } else errorMsg
            }
            else -> {}
        }
    }

    if (showError != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = { showError = null }) { Text("Dismiss") } }
        ) { Text(showError ?: "") }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE0ECFF), Color(0xFF90CAF9)),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF3F51B5)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        // Title at the top
        Text(
            text = "Student Dashboard",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = accentColor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 76.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp)) // Space below title
            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, accentColor, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePicPath != null) {
                            val bitmap = BitmapFactory.decodeFile(profilePicPath)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(80.dp).clip(CircleShape)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                modifier = Modifier.size(56.dp),
                                tint = Color(0xFF4A90E2)
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Welcome!",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = accentColor
                        )
                        Text(
                            text = studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Email: $studentEmail",
                                color = accentColor,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Section: $section",
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "Role: Student",
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp)) // Large space between profile and session join
            // Session Join
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = MaterialTheme.shapes.medium,    
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = { Text("Enter 6-digit Session Code", color = textColor) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = textColor)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.joinSession(code, section) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = code.length == 6 && uiState !is StudentUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Join Feedback Session", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (uiState is StudentUiState.Loading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
            }
            Spacer(Modifier.weight(1f)) // Push logout to bottom
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(onClick = {
                    authViewModel.signOut()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
} 