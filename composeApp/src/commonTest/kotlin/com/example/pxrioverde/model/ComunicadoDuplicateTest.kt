package com.example.pxrioverde.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ComunicadoDuplicateTest {

    private fun saida(exit: String, date: String = "14/11/2026") = ComunicadoState(
        type = AbsenceType.SAIDA_ANTECIPADA, date = date, exitTime = exit
    )

    private fun retirada(exit: String, ret: String, date: String = "14/11/2026") = ComunicadoState(
        type = AbsenceType.RETIRADA, date = date, exitTime = exit, returnTime = ret
    )

    @Test
    fun casoDaGabryella_mesmaSaidaNoMesmoDiaEhDuplicata() {
        assertEquals(saida("17:00").occurrenceKey(), saida("17:00").occurrenceKey())
    }

    @Test
    fun saidaEmOutroHorarioNaoEhDuplicata() {
        assertNotEquals(saida("17:00").occurrenceKey(), saida("16:00").occurrenceKey())
    }

    @Test
    fun saidaEmOutroDiaNaoEhDuplicata() {
        assertNotEquals(saida("17:00").occurrenceKey(), saida("17:00", "15/11/2026").occurrenceKey())
    }

    @Test
    fun duasRetiradasNoMesmoDiaEmHorariosDiferentesSaoPermitidas() {
        assertNotEquals(
            retirada("09:00", "10:00").occurrenceKey(),
            retirada("15:00", "16:00").occurrenceKey()
        )
    }

    @Test
    fun retiradaIgualEhDuplicata() {
        assertEquals(retirada("09:00", "10:00").occurrenceKey(), retirada("09:00", "10:00").occurrenceKey())
    }

    @Test
    fun tiposDiferentesNoMesmoDiaNaoSaoDuplicata() {
        val falta = ComunicadoState(type = AbsenceType.FALTA, date = "14/11/2026")
        assertNotEquals(falta.occurrenceKey(), saida("17:00").occurrenceKey())
    }

    @Test
    fun duasFaltasNoMesmoDiaSaoDuplicataMesmoComDiasDiferentesDigitados() {
        val a = ComunicadoState(type = AbsenceType.FALTA, date = "14/11/2026", missingDays = "1")
        val b = ComunicadoState(type = AbsenceType.FALTA, date = "14/11/2026", missingDays = "1 dia")
        assertEquals(a.occurrenceKey(), b.occurrenceKey())
    }

    @Test
    fun espacosSobrandoNaoEscondemDuplicata() {
        assertEquals(saida("17:00").occurrenceKey(), saida(" 17:00 ", " 14/11/2026 ").occurrenceKey())
    }

    @Test
    fun atrasoEhComparadoPorPrevistaEEfetiva() {
        val a = ComunicadoState(type = AbsenceType.ATRASO, date = "14/11/2026", expectedTime = "08:00", effectiveTime = "08:30")
        val b = a.copy(effectiveTime = "09:00")
        assertNotEquals(a.occurrenceKey(), b.occurrenceKey())
        assertEquals(a.occurrenceKey(), a.copy().occurrenceKey())
    }

    @Test
    fun trocaDeHorarioEhComparadaPorOriginalENovo() {
        val a = ComunicadoState(type = AbsenceType.TROCA_HORARIO, date = "14/11/2026", originalTime = "08:00", newTime = "09:00")
        assertNotEquals(a.occurrenceKey(), a.copy(newTime = "10:00").occurrenceKey())
    }

    @Test
    fun motivoDiferenteNaoEvitaDuplicata() {
        assertEquals(
            saida("17:00").copy(reason = "Consulta").occurrenceKey(),
            saida("17:00").copy(reason = "Detran").occurrenceKey()
        )
    }

    @Test
    fun comunicadoRecusadoNaoContaComoDuplicata() {
        val recusado = saida("17:00").copy(adminFeedback = AdminFeedback(isAuthorized = false))
        assertTrue(recusado.isRejected)
    }

    @Test
    fun comunicadoAprovadoOuPendenteContaComoDuplicata() {
        assertFalse(saida("17:00").isRejected)
        assertFalse(saida("17:00").copy(adminFeedback = AdminFeedback(isAuthorized = true)).isRejected)
        assertFalse(saida("17:00").copy(adminFeedback = AdminFeedback(isAuthorized = null)).isRejected)
    }
}
