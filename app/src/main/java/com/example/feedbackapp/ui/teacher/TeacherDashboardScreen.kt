@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.feedbackapp.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.feedbackapp.viewmodel.AuthViewModel
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

// Custom colors
private val White = Color(0xFFF9F9FC)
private val TopBarBlue = Color(0xFF40476D)
private val Lavender = Color(0xFFE2DEEE)
private val WelcomeBlue = Color(0xFF5661AB)
private val SlateBlue = Color(0xFF5E6399)
private val DarkGray = Color(0xFF5A5A68)
private val Plum = Color(0xFF7A5E75)
private val DarkBlue = Color(0xFF525E91)
private val LogoutRed = Color(0xFFC0392B)

val Context.dataStore by preferencesDataStore(name = "profile_prefs")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    teacherId: String,
    teacherName: String = "Suhas",
    authViewModel: AuthViewModel
) {
    val showLogoutDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var profilePicPath by remember { mutableStateOf<String?>(null) }
    val dataStore = context.applicationContext.dataStore
    val scope = rememberCoroutineScope()
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
            Color(0xFF7C3AED), // Vibrant purple
            Color(0xFF4A90E2), // Blue
            Color(0xFFFFB6EC)  // Soft pink
        ),
        startY = 0f,
        endY = 1000f
    )
    val accentColor = Color(0xFF7C3AED)
    val cardColor = Color.White
    val textColor = Color(0xFF22223B)
    val mutedTextColor = Color(0xFF6B7280)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TopBar()
        Spacer(Modifier.height(24.dp))
        WelcomeCard(
            name = teacherName,
            profilePicPath = profilePicPath,
            onProfilePicClick = { imagePickerLauncher.launch("image/*") },
            context = context
        )
        Spacer(Modifier.height(32.dp))
        DashboardButton(
            label = "Create Feedback Session",
            icon = Icons.Default.Create,
            color = accentColor,
            onClick = { navController.navigate("teacher_create_session/$teacherId") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        DashboardButton(
            label = "Manage Question Bank",
            icon = Icons.Default.QuestionAnswer,
            color = accentColor,
            onClick = { navController.navigate("teacher_question_bank/$teacherId") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        DashboardButton(
            label = "View Previous Sessions",
            icon = Icons.AutoMirrored.Filled.List,
            color = accentColor,
            onClick = { navController.navigate("teacher_sessions/$teacherId") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        DashboardButton(
            label = "View Section Analytics",
            icon = Icons.Default.Analytics,
            color = accentColor,
            onClick = { navController.navigate("teacher_analytics/$teacherId") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))
        LogoutButton(
            onClick = { showLogoutDialog.value = true },
            icon = Icons.AutoMirrored.Filled.Logout
        )
        Spacer(Modifier.height(48.dp))
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
