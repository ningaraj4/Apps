package com.example.edufeed.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import android.content.ClipboardManager

/**
 * A secure text field that prevents copy/paste during exam mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    secureMode: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    var textFieldValue by remember { 
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    
    // Update the text field value when the value parameter changes
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }
    
    // Handle secure mode (prevent copy/paste)
    val isFocused by interactionSource.collectIsFocusedAsState()
    val context = LocalContext.current
    
    DisposableEffect(secureMode, isFocused) {
        if (secureMode && isFocused) {
            // In secure mode, clear the clipboard when the field is focused
            val clipboardManager = context.getSystemService(ClipboardManager::class.java)
            clipboardManager?.clearPrimaryClip()
            
            onDispose {
                // Optional: Clear clipboard again when losing focus
                clipboardManager?.clearPrimaryClip()
            }
        } else {
            onDispose {}
        }
    }
    
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            if (secureMode) {
                // In secure mode, prevent pasting by checking if the change is too large
                // (which would indicate a paste operation)
                val isPaste = newValue.text.length - textFieldValue.text.length > 1
                if (!isPaste) {
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                } else {
                    // Optionally show a message that pasting is disabled
                    // You could use a Snackbar or other UI feedback here
                }
            } else {
                textFieldValue = newValue
                onValueChange(newValue.text)
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource
    )
}
