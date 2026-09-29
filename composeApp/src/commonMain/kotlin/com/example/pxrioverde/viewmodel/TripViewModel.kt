package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.repository.TripRepository
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.model.Trip
import com.example.pxrioverde.model.Car
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TripViewModel(
    private val repository: TripRepository,
    private val syncTripsUseCase: com.example.pxrioverde.domain.usecase.SyncTripsUseCase
) : ViewModel() {
    
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings = _bookings.asStateFlow()

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings = _allBookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isBookingSubmitting = MutableStateFlow(false)
    val isBookingSubmitting = _isBookingSubmitting.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _capturedPhotos = MutableStateFlow<List<ByteArray?>>(listOf(null, null, null, null))
    val capturedPhotos = _capturedPhotos.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _availableCars = MutableStateFlow<List<Car>>(emptyList())
    val availableCars = _availableCars.asStateFlow()

    fun loadBookings(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _bookings.value = repository.getBookings(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allBookings.value = repository.getAllBookings()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBooking(booking: Booking, onResult: (Boolean, String?) -> Unit) {
        _isBookingSubmitting.value = true
        _isSuccess.value = false

        viewModelScope.launch(Dispatchers.Default) {
            delay(300)

            try {
                // Validação básica: fim deve ser após o início se no mesmo dia
                // (Nota: Se permitir múltiplos dias, essa lógica precisa ser expandida)
                val startVal = booking.scheduledStartTime.replace(":", "").toIntOrNull() ?: 0
                val endVal = booking.scheduledEndTime?.replace(":", "")?.toIntOrNull() ?: 2359
                
                if (!booking.isEmergency && booking.scheduledEndTime != null && endVal <= startVal) {
                    withContext(Dispatchers.Main) {
                        _isBookingSubmitting.value = false
                        onResult(false, "O horário de término deve ser posterior ao horário de início.")
                    }
                    return@launch
                }

                // Validação de conflito usando horários agendados
                // Filtramos para ignorar agendamentos que não ocupam mais o veículo
                val hasConflict = _allBookings.value.any { existing ->
                    val status = existing.status.uppercase()
                    existing.carId == booking.carId &&
                    existing.date == booking.date &&
                    status != "CANCELADO" && 
                    status != "FINALIZADO" &&
                    isOverlapping(
                        existing.scheduledStartTime, existing.scheduledEndTime,
                        booking.scheduledStartTime, booking.scheduledEndTime
                    )
                }

                if (hasConflict) {
                    withContext(Dispatchers.Main) {
                        _isBookingSubmitting.value = false
                        onResult(false, "Este veículo já possui um agendamento para este horário.")
                    }
                    return@launch
                }

                // Todo agendamento começa no check-in, inclusive o emergencial.
                val finalBooking = booking.copy(
                    status = "agendado",
                    startTime = null,
                    endTime = null,
                    initialKm = null,
                    finalKm = null
                )

                // Cria o agendamento no Supabase e recupera o objeto criado (com ID)
                val createdBooking = repository.createBooking(finalBooking)
                val bookingId = createdBooking?.id
                    ?: error("O agendamento foi salvo, mas não foi possível recuperar seu identificador.")
                
                withContext(Dispatchers.Main) {
                    _isSuccess.value = true
                    _isBookingSubmitting.value = false
                    onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isBookingSubmitting.value = false
                    onResult(false, e.message ?: "Erro desconhecido")
                }
            }
        }
    }

    fun finishBooking(booking: Booking, finalKm: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val photoUrls = mutableListOf<String>()
                _capturedPhotos.value.forEachIndexed { index, bytes ->
                    bytes?.let {
                        val url = repository.uploadTripPhoto(it, "trip_finish_${booking.id}_$index.jpg")
                        photoUrls.add(url)
                    }
                }

                val nowInstant = Clock.System.now()
                val kotlinxInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(nowInstant.toEpochMilliseconds())
                val now = kotlinxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
                
                val updatedBooking = booking.copy(
                    status = "finalizado",
                    endTime = timeStr,
                    finalKm = finalKm
                )

                repository.updateBooking(updatedBooking)

                // CORREÇÃO: Usar o novo método finishTrip que busca por bookingId diretamente
                repository.finishTrip(
                    bookingId = booking.id!!,
                    endKm = finalKm,
                    endTime = Clock.System.now().toEpochMilliseconds(),
                    photoUrls = photoUrls
                )
                
                _capturedPhotos.value = listOf(null, null, null, null)
                loadBookings(booking.userId)
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun cancelBooking(bookingId: String, userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.cancelBooking(bookingId)
                loadBookings(userId)
                loadAllBookings()
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    private fun isOverlapping(s1: String, e1: String?, s2: String, e2: String?): Boolean {
        val start1 = s1.replace(":", "").toIntOrNull() ?: 0
        val end1 = e1?.replace(":", "")?.toIntOrNull() ?: 2359
        val start2 = s2.replace(":", "").toIntOrNull() ?: 0
        val end2 = e2?.replace(":", "")?.toIntOrNull() ?: 2359

        // Se o horário de início for igual ou maior que o de término (mesmo dia), tratamos como inválido
        // mas aqui focamos na sobreposição entre dois agendamentos
        return start1 < end2 && start2 < end1
    }

    fun loadAvailableCars() {
        viewModelScope.launch {
            try {
                _availableCars.value = repository.getAllCars()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCapturedPhoto(index: Int, bytes: ByteArray) {
        val current = _capturedPhotos.value.toMutableList()
        if (index in 0..3) {
            current[index] = bytes
            _capturedPhotos.value = current
        }
    }

    fun submitTripStart(booking: Booking, km: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val photoUrls = mutableListOf<String>()
                _capturedPhotos.value.forEachIndexed { index, bytes ->
                    bytes?.let {
                        val url = repository.uploadTripPhoto(it, "trip_${booking.id}_$index.jpg")
                        photoUrls.add(url)
                    }
                }

                val nowInstant = Clock.System.now()
                val kotlinxInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(nowInstant.toEpochMilliseconds())
                val now = kotlinxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
                
                val updatedBooking = booking.copy(
                    status = "EM_ANDAMENTO",
                    startTime = timeStr,
                    initialKm = km
                )
                repository.updateBooking(updatedBooking)

                                val newTrip = Trip(
                    bookingId = booking.id!!,
                    userId = booking.userId,
                    carId = booking.carId,
                    startKm = km,
                    startTime = Clock.System.now().toEpochMilliseconds(),
                    photoUrls = photoUrls,
                                    destination = booking.destination,
                                    status = "ativo"
                                )

                repository.startTrip(newTrip)
                
                _capturedPhotos.value = listOf(null, null, null, null)
                loadBookings(booking.userId)
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearData() {
        _bookings.value = emptyList()
        _allBookings.value = emptyList()
        _availableCars.value = emptyList()
        _capturedPhotos.value = listOf(null, null, null, null)
        _isSuccess.value = false
    }
}
