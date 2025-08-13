package com.example.edufeed

import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.edufeed.ui.AppNavHost
import com.example.edufeed.ui.theme.EduFeedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Security features will be enabled only during quiz/feedback sessions
        // No global security activation here
        enableEdgeToEdge()
        setContent {
            EduFeedTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
    // To exit lock task mode, call stopLockTask()
}