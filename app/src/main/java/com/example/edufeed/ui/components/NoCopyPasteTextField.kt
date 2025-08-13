package com.example.edufeed.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.ExperimentalComposeUiApi

// Modifier to prevent copy/paste actions
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.noCopyPaste(): Modifier = this.then(
    Modifier
        // This prevents long-press to show copy/paste menu
        .pointerInteropFilter { event ->
            // Consume all pointer events to prevent context menu
            true
        }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoCopyPasteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isEnabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    placeholder: @Composable (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Prevent copy/paste menu
    val interactionSource = remember { MutableInteractionSource() }
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = label,
        modifier = modifier
            .focusRequester(focusRequester)
            .noCopyPaste(),
        readOnly = !isEnabled,
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onImeAction()
                keyboardController?.hide()
            },
            onNext = {
                onImeAction()
            }
        ),
        visualTransformation = VisualTransformation.None,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = textStyle,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF5E60CE),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red,
            cursorColor = Color(0xFF5E60CE),
            errorCursorColor = Color.Red,
            focusedLabelColor = Color(0xFF5E60CE),
            unfocusedLabelColor = Color.Gray,
            disabledLabelColor = Color.LightGray,
            errorLabelColor = Color.Red,
            // placeholderColor is not a valid parameter in the current version
            // Using the default placeholder color instead
            // disabledPlaceholderColor is also not a valid parameter
        ),
        placeholder = placeholder,
        isError = value.length > maxLength
    )
    
    // Show character count if maxLength is set
    if (maxLength < Int.MAX_VALUE) {
        Text(
            text = "${value.length}/$maxLength",
            style = textStyle.copy(
                color = if (value.length > maxLength) Color.Red else Color.Gray,
                fontSize = 12.sp
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}