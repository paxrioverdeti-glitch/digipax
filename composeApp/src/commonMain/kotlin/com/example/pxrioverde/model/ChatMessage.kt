package com.example.pxrioverde.model

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class MessageSender { USER, AI }

data class ChatMessage @OptIn(ExperimentalTime::class) constructor(
    val id: String = Clock.System.now().toEpochMilliseconds().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
