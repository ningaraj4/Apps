package com.example.edufeed.ui.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edufeed.viewmodel.StudentViewModel
import com.example.edufeed.viewmodel.StudentUiState
import com.example.edufeed.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import android.content.Context
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Composable
fun ActionButton(
    title: String,
    subtitle: String,
    icon: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavController,
    studentId: String,
    section: String,
    viewModel: StudentViewModel,
    authViewModel: AuthViewModel
) {
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

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }
    var scoresVisible by remember { mutableStateOf(false) }
    
    // Form states for joining sessions
    var feedbackCode by remember { mutableStateOf("") }
    var quizCode by remember { mutableStateOf("") }
    
    // Trigger animations
    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(300)
        cardsVisible = true
        kotlinx.coroutines.delay(400)
        scoresVisible = true
    }
    
    // Responsive sizing
    val cardPadding = if (screenWidth < 360.dp) 16.dp else 20.dp
    val titleSize = if (screenWidth < 360.dp) 20.sp else 24.sp
    val subtitleSize = if (screenWidth < 360.dp) 14.sp else 16.sp
    val buttonHeight = if (screenHeight < 600.dp) 48.dp else 52.dp
    
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD),
            Color(0xFFBBDEFB),
            Color(0xFF90CAF9),
            Color(0xFF64B5F6)
        ),
        startY = 0f,
        endY = screenHeight.value * 1.2f
    )
    val accentColor = Color(0xFF4299E1)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)

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
        
        // Main Column with scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            
            // Animated Header Card - ClassSphere Style
            AnimatedVisibility(
                visible = headerVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D3748)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Icon
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Color(0xFF4299E1),
                                    shape = CircleShape
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePicPath != null) {
                                val file = File(profilePicPath!!)
                                if (file.exists()) {
                                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome!",
                                fontSize = 16.sp,
                                color = Color(0xFF4299E1)
                            )
                            Text(
                                text = if (studentName.isNotEmpty()) studentName else "Ningaraj",
                                fontSize = titleSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (studentEmail.isNotEmpty()) studentEmail else "ningarajpt.cs22@rvce.edu.in",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Section: $section • Role: Student",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Animated Action Cards - ClassSphere Style
            AnimatedVisibility(
                visible = cardsVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Join Feedback Session Card - Dark theme like ClassSphere
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2D3748)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(cardPadding)
                        ) {
                            Text(
                                text = "Enter 6-digit Feedback Code",
                                fontSize = subtitleSize,
                                color = Color(0xFF4299E1),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = feedbackCode,
                                onValueChange = { if (it.length <= 6) feedbackCode = it },
                                placeholder = { Text("Enter code", color = Color(0xFF9CA3AF)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4299E1),
                                    unfocusedBorderColor = Color(0xFF4A5568),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF4299E1)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { 
                                    if (feedbackCode.length == 6) {
                                        navController.navigate("student_feedback_join/$studentId/$feedbackCode")
                                    }
                                },
                                enabled = feedbackCode.length == 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(buttonHeight),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374151),
                                    disabledContainerColor = Color(0xFF374151).copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Join Feedback Session",
                                    fontSize = if (screenWidth < 360.dp) 14.sp else 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Join Quiz Card - Blue theme like ClassSphere
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4299E1)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(cardPadding)
                        ) {
                            Text(
                                text = "Enter 6-digit Quiz Code",
                                fontSize = subtitleSize,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = quizCode,
                                onValueChange = { if (it.length <= 6) quizCode = it },
                                placeholder = { Text("Enter code", color = Color.White.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { 
                                    if (quizCode.length == 6) {
                                        navController.navigate("student_quiz_join/$studentId/$quizCode")
                                    }
                                },
                                enabled = quizCode.length == 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(buttonHeight),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2B6CB0),
                                    disabledContainerColor = Color(0xFF2B6CB0).copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Join Quiz",
                                    fontSize = if (screenWidth < 360.dp) 14.sp else 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated Quiz Scores Section - ClassSphere Style
            AnimatedVisibility(
                visible = scoresVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Quiz Scores",
                                tint = Color(0xFF4299E1),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quiz Scores",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mock quiz scores for demonstration
                        val mockScores = listOf(
                            Triple("Quiz 1", "Completed", 85),
                            Triple("Quiz 2", "Completed", 90),
                            Triple("Quiz 3", "Completed", 95)
                        )
                        
                        mockScores.forEach { (quizName, status, score) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        navController.navigate("student_quiz_details/$studentId/${quizName.replace(" ", "_")}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8F9FA)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = quizName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Text(
                                            text = status,
                                            fontSize = 14.sp,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                    Text(
                                        text = "$score/100",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4299E1)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "No quiz scores available yet. Complete a quiz to see your results here.",
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
            
            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut()
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}