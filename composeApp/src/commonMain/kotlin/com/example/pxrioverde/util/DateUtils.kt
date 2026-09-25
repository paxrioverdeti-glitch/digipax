package com.example.pxrioverde.util

import kotlinx.datetime.*

/**
 * Utilitário para formatação de datas em KMP.
 */
object DateUtils {
    /**
     * Formata uma String ISO 8601 (ex: 2026-06-19T18:43:26.609085+00:00)
     * para o formato brasileiro: dd/MM/yyyy às HH:mm.
     */
    fun formatIsoDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            // Instant.parse no kotlinx-datetime lida com o formato ISO 8601
            val instant = Instant.parse(isoString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            
            "$day/$month/$year às $hour:$minute"
        } catch (e: Exception) {
            isoString // Fallback para a string original em caso de erro
        }
    }

    /**
     * Converte uma data simples (yyyy-MM-dd) para o padrão brasileiro (dd/MM/yyyy).
     */
    fun formatSimpleDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        return try {
            if (dateString.contains("-")) {
                val parts = dateString.split("-")
                if (parts.size == 3) {
                    val year = parts[0]
                    val month = parts[1]
                    val day = parts[2]
                    "$day/$month/$year"
                } else dateString
            } else dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
