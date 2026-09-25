package com.example.pxrioverde.domain.usecase

import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketConstants
import com.example.pxrioverde.model.analytics.DashboardData
import com.example.pxrioverde.model.analytics.KmMetric
import com.example.pxrioverde.model.analytics.TicketMetric
import com.example.pxrioverde.model.analytics.TicketGroupMetric
import com.example.pxrioverde.model.analytics.PurchaseMetric
import com.example.pxrioverde.repository.TicketRepository
import com.example.pxrioverde.repository.TripRepository
import com.example.pxrioverde.repository.PurchaseRepository
import com.example.pxrioverde.ui.admin.components.Appointment
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit

class GetAnalyticsDashboardUseCase(
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val purchaseRepository: PurchaseRepository
) {
    suspend operator fun invoke(month: Int? = null, year: Int? = null): DashboardData = coroutineScope {
        // 1. Buscar dados de forma otimizada e paralela
        val ticketCountsDeferred = async { ticketRepository.getTicketCounts() }
        val kmMetricsDeferred = async { tripRepository.getKmMetrics() }
        val ticketMetricsDeferred = async { ticketRepository.getTicketMetrics() }
        val allTicketsDeferred = async { ticketRepository.getAllTickets(page = 0, pageSize = 50) }
        
        // Buscamos apenas os últimos 50 agendamentos e trips (paginado)
        val bookingsDeferred = async { tripRepository.getAllBookings(page = 0, pageSize = 50) }
        val tripsDeferred = async { tripRepository.getAllTrips(page = 0, pageSize = 50) }
        val carsDeferred = async { tripRepository.getAllCars() }
        val purchasesDeferred = async { purchaseRepository.getAllRequests() }

        val (pendingTickets, resolvedTickets) = ticketCountsDeferred.await()
        val kmMetricsResult = kmMetricsDeferred.await()
        val ticketMetricsResult = ticketMetricsDeferred.await()
        val tickets = allTicketsDeferred.await()
        val bookings = bookingsDeferred.await()
        val trips = tripsDeferred.await()
        val cars = carsDeferred.await()
        val allPurchases = purchasesDeferred.await()

        val carsById = cars.associateBy { it.id }

        // 2. Processamento inteligente de performance por grupo
        val gobahTickets = ticketMetricsResult.filter { TicketConstants.isGobah(it.sector) }
        val tiTickets = ticketMetricsResult.filter { !TicketConstants.isGobah(it.sector) }

        val gobahPerformance = TicketGroupMetric(
            name = "Gobah",
            resolved = gobahTickets.sumOf { it.resolvedCount },
            pending = gobahTickets.sumOf { it.totalTickets } - gobahTickets.sumOf { it.resolvedCount }
        )

        val tiPerformance = TicketGroupMetric(
            name = "T.I Interno",
            resolved = tiTickets.sumOf { it.resolvedCount },
            pending = tiTickets.sumOf { it.totalTickets } - tiTickets.sumOf { it.resolvedCount }
        )

        // 3. Processamento de agendamentos com busca O(1)
        val tripsByBookingId = trips.groupBy { it.bookingId }
        val appointments = bookings.map { booking ->
            val trip = tripsByBookingId[booking.id]?.lastOrNull()
            val vehicle = carsById[booking.carId]
            val km = if (trip?.endKm != null) {
                (trip.endKm - trip.startKm).coerceAtLeast(0.0).toInt()
            } else if (booking.finalKm != null && booking.initialKm != null) {
                (booking.finalKm - booking.initialKm).coerceAtLeast(0.0).toInt()
            } else 0
            val actualStart = trip?.startTime?.let { epoch ->
                Instant.fromEpochMilliseconds(epoch).toLocalDateTime(TimeZone.currentSystemDefault())
                    .time.toString().take(5)
            }
            val actualEnd = trip?.endTime?.let { epoch ->
                Instant.fromEpochMilliseconds(epoch).toLocalDateTime(TimeZone.currentSystemDefault())
                    .time.toString().take(5)
            }
            
            Appointment(
                id = booking.id ?: "",
                userName = booking.userName,
                userAvatarUrl = booking.userAvatarUrl,
                date = booking.date,
                startTime = actualStart ?: booking.startTime ?: booking.scheduledStartTime,
                endTime = actualEnd ?: booking.endTime ?: booking.scheduledEndTime ?: "--:--",
                destination = trip?.destination ?: booking.destination ?: "Destino não informado",
                kmTraveled = km,
                isFinished = booking.status.equals("finalizado", ignoreCase = true),
                photoUrls = trip?.photoUrls ?: emptyList(),
                endPhotoUrls = trip?.endPhotoUrls ?: emptyList(),
                vehicleName = vehicle?.name ?: "Veículo não identificado"
            )
        }

        // 4. Processamento de Compras (Senior Logic)
        val filteredPurchases = if (month != null && year != null) {
            allPurchases.filter { 
                val dt = Instant.fromEpochMilliseconds(it.createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
                (dt.month.ordinal + 1) == month && dt.year == year
            }
        } else allPurchases

        val approvedPurchases = filteredPurchases.filter { it.status == com.example.pxrioverde.model.PurchaseStatus.APROVADO || it.status == com.example.pxrioverde.model.PurchaseStatus.COMPRADO }
        val totalApprovedValue = approvedPurchases.sumOf { it.approvedValue ?: 0.0 }
        
        val purchaseMetrics = approvedPurchases.groupBy { it.department }
            .map { (dept, list) ->
                PurchaseMetric(
                    sector = dept,
                    totalApproved = list.sumOf { it.approvedValue ?: 0.0 },
                    requestCount = list.size
                )
            }.sortedByDescending { it.totalApproved }

        // 5. Gerar métricas de KM mesclando com meses vazios
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val kmMetrics = months.map { monthName ->
            kmMetricsResult.find { it.month.contains(monthName, ignoreCase = true) } 
                ?: KmMetric(monthName, 0.0)
        }

        DashboardData(
            kmMetrics = kmMetrics,
            ticketMetrics = ticketMetricsResult,
            tiPerformance = tiPerformance,
            gobahPerformance = gobahPerformance,
            appointments = appointments,
            allTickets = tickets,
            pendingTicketsCount = pendingTickets,
            resolvedTicketsCount = resolvedTickets,
            availableVehicles = cars.map { it.name }.sorted(),
            purchaseMetrics = purchaseMetrics,
            totalApprovedValue = totalApprovedValue,
            allPurchases = allPurchases
        )
    }
}
