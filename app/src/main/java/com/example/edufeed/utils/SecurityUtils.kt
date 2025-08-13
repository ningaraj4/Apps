package com.example.edufeed.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Composable that prevents screenshots and screen recording when applied to a screen.
 * It also disables system UI elements like the status bar and navigation bar.
 */
@Composable
fun PreventScreenshotLegacy() {
    val context = LocalContext.current
    val view = LocalView.current
    
    DisposableEffect(Unit) {
        val activity = context as? Activity
        
        // Set FLAG_SECURE to prevent screenshots and screen recording
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // Hide system UI elements
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        
        onDispose {
            // Clean up when the composable is removed from the composition
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            
            // Restore system UI elements
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowCompat.getInsetsController(window, view).show(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
                )
            }
        }
    }
}

/**
 * Enables or disables screenshots for the given activity.
 * @param enabled If true, prevents screenshots. If false, allows screenshots.
 */
fun setScreenshotPrevention(activity: Activity, enabled: Boolean) {
    if (enabled) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

/**
 * Locks the device in a kiosk/pinned mode if supported.
 * Requires the device owner permission or special permissions.
 */
fun enableKioskMode(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        try {
            activity.startLockTask()
        } catch (_: Exception) { /* no-op */ }
    }
}

/**
 * Disables kiosk/pinned mode.
 */
fun disableKioskMode(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        try {
            activity.stopLockTask()
        } catch (_: Exception) { /* no-op */ }
    }
}

// Wrapper class to access DevicePolicyManager's hidden methods
// Removed DevicePolicyManagerWrapper and advanced kiosk setup due to limited API availability.
