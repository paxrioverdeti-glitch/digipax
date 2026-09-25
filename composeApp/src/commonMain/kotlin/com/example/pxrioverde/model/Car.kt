package com.example.pxrioverde.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Car(
    val id: String? = null,
    val name: String,
    val model: String,
    @SerialName("license_plate")
    val licensePlate: String
)

@Serializable
data class Booking(
    val id: String? = null,
    @SerialName("carId") val carId: String,
    @SerialName("userId") val userId: String,
    @SerialName("userName") val userName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bookingDate") val date: String,
    @SerialName("scheduledStartTime") val scheduledStartTime: String,
    @SerialName("scheduledEndTime") val scheduledEndTime: String? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("initialKm") val initialKm: Double? = null,
    @SerialName("finalKm") val finalKm: Double? = null,
    val status: String = "agendado",
    @SerialName("returnDate") val returnDate: String? = null,
    @SerialName("returnTime") val returnTime: String? = null,
    @SerialName("location") val destination: String? = null,
    @SerialName("sector") val department: String? = null,
    @SerialName("isEmergency") val isEmergency: Boolean = false
) {
    val userAvatarUrl: String? get() = avatarUrl
}

@Serializable
data class Trip(
    val id: String? = null,
    @SerialName("booking_id") val bookingId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("car_id") val carId: String,
    @SerialName("start_km") val startKm: Double,
    @SerialName("end_km") val endKm: Double? = null,
    @SerialName("start_time") val startTime: Long,
    @SerialName("end_time") val endTime: Long? = null,
    @SerialName("photo_urls") val photoUrls: List<String> = emptyList(),
    @SerialName("end_photo_urls") val endPhotoUrls: List<String> = emptyList(),
    val destination: String? = null,
    val status: String = "ativo"
)
