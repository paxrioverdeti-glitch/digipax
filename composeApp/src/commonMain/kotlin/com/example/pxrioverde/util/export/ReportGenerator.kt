package com.example.pxrioverde.util.export

import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.ui.admin.components.Appointment

object ReportGenerator {
    fun generateTicketsCsv(tickets: List<Ticket>): String {
        val sb = StringBuilder()
        sb.append("ID;Data;Titulo;Setor;Usuario;Status;Avaliacao;Comentario\n")
        
        tickets.forEach { ticket ->
            val date = ticket.createdAt?.substringBefore("T") ?: ""
            sb.append("${ticket.id};")
            sb.append("$date;")
            sb.append("${ticket.title.replace(";", ",")};")
            sb.append("${ticket.sector};")
            sb.append("${ticket.userName};")
            sb.append("${ticket.status};")
            sb.append("${ticket.rating ?: ""};")
            sb.append("${ticket.ratingComment?.replace(";", ",") ?: ""}\n")
        }
        return sb.toString()
    }

    fun generateTripsCsv(appointments: List<Appointment>): String {
        val sb = StringBuilder()
        sb.append("Data;Usuario;Veiculo;Destino;Inicio;Fim;KM Percorrido\n")
        
        appointments.forEach { app ->
            sb.append("${app.date};")
            sb.append("${app.userName};")
            sb.append("${app.vehicleName ?: ""};")
            sb.append("${app.destination.replace(";", ",")};")
            sb.append("${app.startTime};")
            sb.append("${app.endTime};")
            sb.append("${app.kmTraveled}\n")
        }
        return sb.toString()
    }
}
