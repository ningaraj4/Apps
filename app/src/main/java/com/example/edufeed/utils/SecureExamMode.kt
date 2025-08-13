package com.example.edufeed.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.edufeed.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages secure exam mode to prevent cheating during quizzes
 */
class SecureExamMode(private val context: Context) {
    private val _isExamMode = MutableStateFlow(false)
    val isExamMode: StateFlow<Boolean> = _isExamMode

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                // Handle screen off event
                onViolationDetected("Screen turned off during exam")
            }
        }
    }

    /**
     * Enables secure exam mode with the specified restrictions
     */
    fun enable() {
        if (_isExamMode.value) return
        
        _isExamMode.value = true
        
        // Register screen off receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        }
        context.registerReceiver(screenOffReceiver, filter)
        
        // Disable screenshots and screen recording
        (context as? Activity)?.window?.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    /**
     * Disables secure exam mode and removes all restrictions
     */
    fun disable() {
        if (!_isExamMode.value) return
        
        try {
            context.unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) {
            // Receiver was not registered
        }
        
        // Re-enable screenshots and screen recording
        (context as? Activity)?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_SECURE or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        
        _isExamMode.value = false
    }
    
    /**
     * Handles security violations during exam mode
     */
    fun onViolationDetected(reason: String) {
        // Log the violation
        // In a production app, you might want to report this to your backend
        // and take appropriate action (e.g., auto-submit the exam)
        
        // For now, we'll just disable exam mode
        disable()
    }
}

/**
 * Composable that manages secure exam mode for a screen
 * @param isEnabled Whether secure exam mode should be enabled
 * @param onViolation Callback when a security violation is detected
 */
@Composable
fun SecureExamModeHandler(
    isEnabled: Boolean,
    onViolation: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val secureExamMode = remember { SecureExamMode(context) }
    
    // Handle lifecycle
    DisposableEffect(lifecycleOwner, isEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (secureExamMode.isExamMode.value) {
                        onViolation("App backgrounded during exam")
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isEnabled && !secureExamMode.isExamMode.value) {
                        secureExamMode.enable()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    secureExamMode.disable()
                }
                else -> {}
            }
        }
        
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        
        onDispose {
            lifecycle.removeObserver(observer)
            secureExamMode.disable()
        }
    }
    
    // Handle exam mode state changes
    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            secureExamMode.enable()
        } else {
            secureExamMode.disable()
        }
    }
}

/**
 * Composable that prevents screenshots and screen recording
 */
@Composable
fun PreventScreenshot() {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
