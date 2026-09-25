package com.example.pxrioverde.ui.feed

import androidx.compose.runtime.Composable

@Composable
expect fun FeedImagePicker(
    onImageSelected: (bytes: ByteArray, fileName: String) -> Unit
)
