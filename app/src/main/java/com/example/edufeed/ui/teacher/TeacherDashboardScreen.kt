@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edufeed.ui.teacher

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edufeed.viewmodel.AuthViewModel
import androidx.compose.runtime.remember
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.InputStream
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import android.content.Context
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer

// ClassSphere-inspired colors
private val White = Color(0xFFF9F9FC)
private val TopBarBlue = Color(0xFF40476D)
private val Lavender = Color(0xFFE2DEEE)
private val WelcomeBlue = Color(0xFF5661AB)
private val SlateBlue = Color(0xFF5E6399)
private val DarkGray = Color(0xFF5A5A68)
private val Plum = Color(0xFF7A5E75)
private val DarkBlue = Color(0xFF525E91)
private val LogoutRed = Color(0xFFC0392B)
private val FeatureBlue = Color(0xFF6366F1)
private val FeaturePurple = Color(0xFF8B5CF6)
private val FeaturePink = Color(0xFFEC4899)
private val FeatureGreen = Color(0xFF10B981)

val Context.dataStore by preferencesDataStore(name = "profile_prefs")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    teacherId: String,
    teacherName: String = "Ramachandra",
    authViewModel: AuthViewModel
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    val showLogoutDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var profilePicPath by remember { mutableStateOf<String?>(null) }
    val dataStore = context.applicationContext.dataStore
    val scope = rememberCoroutineScope()
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }
    
    // Trigger animations
    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(300)
        titleVisible = true
        kotlinx.coroutines.delay(200)
        cardsVisible = true
    }
    
    // Responsive sizing
    val cardPadding = if (screenWidth < 360.dp) 16.dp else 20.dp
    val titleSize = if (screenWidth < 360.dp) 28.sp else 32.sp
    val buttonHeight = if (screenHeight < 600.dp) 56.dp else 64.dp
    
    // Load persisted profile picture path
    LaunchedEffect(teacherId) {
        val prefs = dataStore.data.first()
        prefs[stringPreferencesKey("profile_pic_path_$teacherId")]?.let {
            profilePicPath = it
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Copy image to internal storage
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_pic_teacher_$teacherId.jpg")
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            profilePicPath = file.absolutePath
            // Persist file path in DataStore
            scope.launch {
                dataStore.edit { prefs ->
                    prefs[stringPreferencesKey("profile_pic_path_$teacherId")] = file.absolutePath
                }
            }
        } else {
            profilePicPath = null
            scope.launch {
                dataStore.edit { prefs ->
                    prefs.remove(stringPreferencesKey("profile_pic_path_$teacherId"))
                }
            }
        }
    }
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2),
            Color(0xFF6B73FF),
            Color(0xFF9A4DFF)
        ),
        startY = 0f,
        endY = screenHeight.value * 1.2f
    )
    val accentColor = Color(0xFF7C3AED)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (screenWidth < 360.dp) 16.dp else 20.dp)
        ) {
            Spacer(modifier = Modifier.height(if (screenHeight < 600.dp) 40.dp else 56.dp))
            
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
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Icon with gradient
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF4FC3F7),
                                            Color(0xFF29B6F6),
                                            Color(0xFF0288D1)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePicPath != null) {
                                val bitmap = BitmapFactory.decodeFile(profilePicPath)
                                if (bitmap != null) {
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
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Teacher Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Teacher Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = teacherName,
                                fontSize = if (screenWidth < 360.dp) 20.sp else 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Manage your feedback sessions and analytics below.",
                                fontSize = if (screenWidth < 360.dp) 14.sp else 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 500))
            ) {
                Text(
                    text = "Teacher Dashboard",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Animated Feature Cards - ClassSphere Style
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
                    // Feedback Features
                    FeatureCard(
                        title = "Create Feedback Session",
                        icon = Icons.Default.Create,
                        backgroundColor = FeatureBlue,
                        onClick = { navController.navigate("create_feedback_session/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    FeatureCard(
                        title = "Manage Question Bank",
                        icon = Icons.Default.QuestionAnswer,
                        backgroundColor = FeaturePurple,
                        onClick = { navController.navigate("feedback_question_bank/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    FeatureCard(
                        title = "View Previous Sessions",
                        icon = Icons.Default.History,
                        backgroundColor = FeaturePink,
                        onClick = { navController.navigate("feedback_session_list/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    FeatureCard(
                        title = "View Section Analytics",
                        icon = Icons.Default.Analytics,
                        backgroundColor = FeatureGreen,
                        onClick = { navController.navigate("feedback_analytics/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quiz Features Section
                    Text(
                        text = "Quiz Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    FeatureCard(
                        title = "Create Quiz Session",
                        icon = Icons.Default.Quiz,
                        backgroundColor = Color(0xFF3B82F6),
                        onClick = { navController.navigate("create_quiz_session/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    FeatureCard(
                        title = "Manage Quiz Questions",
                        icon = Icons.Default.QuestionMark,
                        backgroundColor = FeaturePurple,
                        onClick = { navController.navigate("quiz_question_bank/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    FeatureCard(
                        title = "View Quiz Sessions",
                        icon = Icons.Default.List,
                        backgroundColor = FeaturePink,
                        onClick = { navController.navigate("quiz_session_list/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                    
                    FeatureCard(
                        title = "Quiz Analytics",
                        icon = Icons.Default.BarChart,
                        backgroundColor = FeatureGreen,
                        onClick = { navController.navigate("quiz_analytics/$teacherId") },
                        screenWidth = screenWidth,
                        buttonHeight = buttonHeight
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logout Button
            Button(
                onClick = { 
                    authViewModel.signOut()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LogoutRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(onClick = {
                    authViewModel.signOut()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LogoutRed)) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Teacher Dashboard",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun WelcomeCard(
    name: String,
    profilePicPath: String? = null,
    onProfilePicClick: (() -> Unit)? = null,
    context: android.content.Context? = null
) {
    val cardGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF7C3AED), // Vibrant purple
            Color(0xFF4A90E2), // Blue
            Color(0xFFFFB6EC)  // Soft pink
        )
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable { onProfilePicClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePicPath != null && context != null) {
                        val bitmap = BitmapFactory.decodeFile(profilePicPath)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(64.dp).clip(CircleShape)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Icon",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(Modifier.width(18.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = WelcomeBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Manage your feedback sessions and analytics below.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedDashboardButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    DashboardButton(
        label = label,
        icon = icon,
        color = color,
        onClick = {
            pressed = true
            onClick()
            pressed = false
        },
        modifier = modifier.graphicsLayer {
            scaleX = if (pressed) 0.96f else 1f
            scaleY = if (pressed) 0.96f else 1f
            alpha = if (pressed) 0.7f else 1f
        }
    )
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp,
    buttonHeight: androidx.compose.ui.unit.Dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = if (screenWidth < 360.dp) 14.sp else 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun DashboardButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 17.sp)
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit, icon: ImageVector = Icons.AutoMirrored.Filled.Logout) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LogoutRed),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text("Logout", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}
