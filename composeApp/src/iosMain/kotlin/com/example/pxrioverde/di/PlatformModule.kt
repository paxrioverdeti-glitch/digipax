package com.example.pxrioverde.di

import com.example.pxrioverde.util.export.FileSharer
import com.example.pxrioverde.util.ImageCompressor
import com.example.pxrioverde.util.NotificationManager
import org.koin.dsl.module
import org.koin.core.module.Module

actual fun platformModule(): Module = module {
    single { FileSharer() }
    single { ImageCompressor() }
    single { NotificationManager() }
}
