package com.example.pxrioverde.util

import androidx.compose.runtime.Composable

expect class NotificationManager {
    fun showNotification(title: String, message: String)
}

@Composable
expect fun rememberNotificationManager(): NotificationManager
