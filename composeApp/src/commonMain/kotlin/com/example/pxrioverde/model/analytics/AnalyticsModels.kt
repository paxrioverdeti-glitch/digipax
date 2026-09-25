package com.example.pxrioverde.model.analytics

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.example.pxrioverde.ui.admin.components.Appointment
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.PurchaseRequest

@Serializable
data class KmMetric(
    val month: String,
    @SerialName("total_km") val totalKm: Double
)

@Serializable
data class TicketMetric(
    val sector: String,
    val averageResolutionTimeHours: Double,
    @SerialName("total_tickets") val totalTickets: Int,
    @SerialName("resolved_count") val resolvedCount: Int = 0
)

data class TicketGroupMetric(
    val name: String,
    val pending: Int,
    val resolved: Int
) {
    val total get() = pending + resolved
    val percentResolved get() = if (total > 0) (resolved.toFloat() / total.toFloat()) * 100f else 0f
}

data class PurchaseMetric(
    val sector: String,
    val totalApproved: Double,
    val requestCount: Int
)

data class DashboardData(
    val kmMetrics: List<KmMetric>,
    val ticketMetrics: List<TicketMetric>,
    val tiPerformance: TicketGroupMetric = TicketGroupMetric("T.I Interno", 0, 0),
    val gobahPerformance: TicketGroupMetric = TicketGroupMetric("Gobah", 0, 0),
    val appointments: List<Appointment> = emptyList(),
    val allTickets: List<Ticket> = emptyList(),
    val pendingTicketsCount: Int = 0,
    val resolvedTicketsCount: Int = 0,
    val availableVehicles: List<String> = emptyList(),
    val purchaseMetrics: List<PurchaseMetric> = emptyList(),
    val totalApprovedValue: Double = 0.0,
    val allPurchases: List<PurchaseRequest> = emptyList()
)
