package com.example.pxrioverde.util

import com.example.pxrioverde.IosBootTrace

actual fun bootTrace(etapa: String) {
    IosBootTrace.passo(etapa)
}
