package com.example.pxrioverde.repository

import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.model.Trip
import com.example.pxrioverde.model.Car
import com.example.pxrioverde.model.analytics.KmMetric
import com.example.pxrioverde.util.ImageCompressor
import com.example.pxrioverde.database.TripDao
import com.example.pxrioverde.database.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
private data class BookingWritePayload(
    @SerialName("car_id")
    val carId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("booking_date")
    val date: String,
    @SerialName("scheduled_start_time")
    val scheduledStartTime: String,
    @SerialName("scheduled_end_time")
    val scheduledEndTime: String? = null,
    @SerialName("return_date")
    val returnDate: String? = null,
    @SerialName("return_time")
    val returnTime: String? = null,
    val location: String? = null,
    val sector: String? = null,
    @SerialName("is_emergency")
    val isEmergency: Boolean = false,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("initial_km")
    val initialKm: Double? = null,
    @SerialName("final_km")
    val finalKm: Double? = null,
    val status: String = "agendado"
)

@Serializable
private data class BookingStatusPayload(
    val status: String
)

class TripRepository(
    private val supabase: SupabaseClient,
    private val imageCompressor: ImageCompressor,
    private val tripDao: TripDao? = null
) {
    suspend fun getBookings(userId: String, page: Int? = null, pageSize: Int = 20): List<Booking> {
        return supabase.from("bookings_api")
            .select {
                filter {
                    eq("userId", userId)
                }
                order("createdAt", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                
                // Aplica paginação ou um limite padrão de segurança
                val start = (page ?: 0) * pageSize
                val end = start + pageSize - 1
                range(start.toLong(), end.toLong())
            }
            .decodeList<Booking>()
    }

    suspend fun getAllBookings(page: Int? = null, pageSize: Int = 50): List<Booking> {
        return try {
            supabase.from("bookings_api")
                .select(Columns.raw("*, avatar_url")) {
                    order("createdAt", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    
                    val start = (page ?: 0) * pageSize
                    val end = start + pageSize - 1
                    range(start.toLong(), end.toLong())
                }
                .decodeList<Booking>()
        } catch (e: Exception) {
            println("Erro ao buscar todos os agendamentos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllTrips(page: Int? = null, pageSize: Int = 50): List<Trip> {
        return try {
            supabase.from("trips").select {
                val start = (page ?: 0) * pageSize
                val end = start + pageSize - 1
                range(start.toLong(), end.toLong())
            }.decodeList<Trip>()
        } catch (e: Exception) {
            println("Erro ao buscar todas as trips: ${e.message}")
            emptyList()
        }
    }

    suspend fun uploadTripPhoto(bytes: ByteArray, fileName: String): String {
        val path = "trips/${Clock.System.now().toEpochMilliseconds()}_$fileName"
        val bucket = supabase.storage.from("trip_photos")
        require(bytes.isNotEmpty()) { "A foto da viagem está vazia." }
        require(bytes.size <= 10 * 1024 * 1024) {
            "A foto da viagem excede o limite de 10 MB."
        }
        var lastError: Exception? = null
        repeat(2) {
            try {
                println("Enviando foto da viagem: ${bytes.size} bytes")
                bucket.upload(path, bytes) {
                    contentType = ContentType.Image.JPEG
                    upsert = true
                }
                return bucket.publicUrl(path)
            } catch (error: Exception) {
                lastError = error
                delay(750)
            }
        }
        throw lastError ?: IllegalStateException("Não foi possível enviar a foto da viagem.")
    }

    suspend fun startTrip(trip: Trip) {
        supabase.from("trips").insert(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        supabase.from("trips").update(trip) {
            filter {
                eq("id", trip.id ?: "")
            }
        }
    }

    suspend fun finishTrip(bookingId: String, endKm: Double, endTime: Long, photoUrls: List<String>) {
        try {
            supabase.from("trips").update({
                set("end_km", endKm)
                set("end_time", endTime)
                set("end_photo_urls", photoUrls)
                set("status", "finalizado")
            }) {
                filter {
                    eq("booking_id", bookingId)
                }
            }
        } catch (e: Exception) {
            println("Erro ao finalizar trip no banco: ${e.message}")
            throw e
        }
    }

    suspend fun createBooking(booking: Booking): Booking? {
        return try {
            supabase.from("bookings").insert(booking.toWritePayload())

            getBookings(booking.userId, page = 0, pageSize = 50)
                .firstOrNull {
                    it.carId == booking.carId &&
                        it.date == booking.date &&
                        it.scheduledStartTime == booking.scheduledStartTime &&
                        it.userId == booking.userId
                }
        } catch (e: Exception) {
            println("Erro ao criar agendamento: ${e.message}")
            null
        }
    }

    suspend fun updateBooking(booking: Booking) {
        supabase.from("bookings").update(booking.toWritePayload()) {
            filter {
                eq("id", booking.id ?: "")
            }
        }
    }

    suspend fun cancelBooking(bookingId: String) {
        supabase.from("bookings").update(BookingStatusPayload("cancelado")) {
            filter {
                eq("id", bookingId)
            }
        }
    }

    private fun Booking.toWritePayload() = BookingWritePayload(
        carId = carId,
        userId = userId,
        userName = userName,
        date = date,
        scheduledStartTime = scheduledStartTime,
        scheduledEndTime = scheduledEndTime,
        returnDate = returnDate,
        returnTime = returnTime,
        location = destination,
        sector = department,
        isEmergency = isEmergency,
        startTime = startTime,
        endTime = endTime,
        initialKm = initialKm,
        finalKm = finalKm,
        status = status
    )

    suspend fun getAllCars(): List<Car> {
        return supabase.from("cars").select {
            range(0, 100) // Limite de segurança para frota
        }.decodeList<Car>()
    }

    suspend fun getKmMetrics(): List<KmMetric> {
        return try {
            supabase.from("km_by_month")
                .select {
                    range(0, 24) // Últimos 2 anos
                }
                .decodeList<KmMetric>()
        } catch (e: Exception) {
            println("Erro ao buscar métricas de KM: ${e.message}")
            emptyList()
        }
    }

    suspend fun syncPendingUpdates() {
        val currentDao = tripDao ?: return
        val pendingUpdates = currentDao.getPendingUpdates(SyncStatus.PENDING)

        for (update in pendingUpdates) {
            // Marca como processando para evitar duplicidade
            currentDao.updateStatus(update.id, SyncStatus.SYNCING)

            try {
                // 1. Upload das fotos para o Storage
                val photoUrls = if (update.photoPaths.isNotEmpty()) {
                    update.photoPaths.split(",").filter { it.isNotBlank() }.map { path ->
                        val bytes = imageCompressor.readBytes(path)
                        uploadTripPhoto(bytes, path.substringAfterLast("/"))
                    }
                } else emptyList()

                // 2. Enviar para o Postgrest
                if (update.type == "CHECK_OUT") {
                    // Implementação Check-out (simulado para este MVP)
                } else if (update.type == "CHECK_IN") {
                    supabase.from("trips").update({
                        set("end_km", update.kmValue)
                        set("end_time", update.timestamp)
                        set("end_photo_urls", photoUrls)
                        set("status", "finalizado")
                    }) {
                        filter { eq("id", update.tripId) }
                    }
                }

                // 3. Sucesso: Deleta do banco local
                currentDao.delete(update)
            } catch (e: Exception) {
                // Falha: Volta para PENDING para o retry do UseCase
                currentDao.updateStatus(update.id, SyncStatus.PENDING)
                throw e // Propaga para o UseCase gerenciar o backoff
            }
        }
    }
}
