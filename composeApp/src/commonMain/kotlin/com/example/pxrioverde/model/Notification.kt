package com.example.pxrioverde.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class CorporateNotification @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var isRead: Boolean = false
)
