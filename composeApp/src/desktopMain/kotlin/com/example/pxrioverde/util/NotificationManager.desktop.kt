package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu

actual class NotificationManager {
    actual fun showNotification(title: String, message: String) {
        if (!SystemTray.isSupported()) return
        
        val tray = SystemTray.getSystemTray()
        val image: Image = Toolkit.getDefaultToolkit().createImage("") // Empty image for now
        val trayIcon = TrayIcon(image, "Px Rio Verde")
        trayIcon.isImageAutoSize = true
        
        try {
            tray.add(trayIcon)
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
            // Remove after showing or keep it? Desktop notifications usually stay until clicked or timed out.
            // Simplified for now.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
actual fun rememberNotificationManager(): NotificationManager = remember { NotificationManager() }
