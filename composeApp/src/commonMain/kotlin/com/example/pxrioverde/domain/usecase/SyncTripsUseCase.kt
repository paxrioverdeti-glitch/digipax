package com.example.pxrioverde.domain.usecase

import com.example.pxrioverde.repository.TripRepository
import kotlinx.coroutines.delay

class SyncTripsUseCase(
    private val repository: TripRepository
) {
    suspend operator fun invoke() {
        var retryCount = 0
        val maxRetries = 3
        var success = false

        while (retryCount < maxRetries && !success) {
            try {
                repository.syncPendingUpdates()
                success = true
            } catch (e: Exception) {
                retryCount++
                if (retryCount < maxRetries) {
                    // Backoff exponencial simples: 2s, 4s, 8s
                    delay(2000L * (1 shl (retryCount - 1)))
                }
            }
        }
    }
}
