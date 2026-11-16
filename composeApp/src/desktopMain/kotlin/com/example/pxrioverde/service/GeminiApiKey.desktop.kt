package com.example.pxrioverde.service

actual fun geminiApiKey(): String = System.getenv("GEMINI_API_KEY").orEmpty()
