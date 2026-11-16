package com.example.pxrioverde.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalFocusManager
import com.example.pxrioverde.ui.platform.iosDismissKeyboardOnTapOutside

/** iPhone: tocar em area que nao e botao nem campo fecha o teclado. */
actual fun Modifier.dismissKeyboardOnTapIos(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    this.iosDismissKeyboardOnTapOutside(focusManager)
}
