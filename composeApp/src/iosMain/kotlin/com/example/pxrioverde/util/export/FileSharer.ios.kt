package com.example.pxrioverde.util.export

import platform.Foundation.*
import platform.UIKit.*
import kotlinx.cinterop.ExperimentalForeignApi

actual class FileSharer {
    @OptIn(ExperimentalForeignApi::class)
    actual fun shareCsv(content: String, fileName: String) {
        val temporaryDirectory = NSTemporaryDirectory()
        val filePath = temporaryDirectory + "$fileName.csv"
        
        (content as NSString).writeToFile(filePath, true, NSUTF8StringEncoding, null)
        
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(activityViewController, true, null)
    }
}
