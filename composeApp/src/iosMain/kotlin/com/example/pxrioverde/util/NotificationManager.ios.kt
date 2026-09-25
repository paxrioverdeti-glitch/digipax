package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationPresentationOptionAlert
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNTimeIntervalNotificationTrigger

import com.example.pxrioverde.getCurrentTimestamp

actual class NotificationManager {
    actual fun showNotification(title: String, message: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)
        val request = UNNotificationRequest.requestWithIdentifier(
            getCurrentTimestamp().toString(),
            content,
            trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("DEBUG: Error showing iOS notification: ${error.localizedDescription}")
            }
        }
    }
}

@Composable
actual fun rememberNotificationManager(): NotificationManager {
    return remember { NotificationManager() }
}
