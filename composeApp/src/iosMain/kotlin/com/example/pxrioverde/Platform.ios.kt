package com.example.pxrioverde

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getCurrentTimestamp(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun generateUUID(): String = platform.Foundation.NSUUID.UUID().UUIDString()
