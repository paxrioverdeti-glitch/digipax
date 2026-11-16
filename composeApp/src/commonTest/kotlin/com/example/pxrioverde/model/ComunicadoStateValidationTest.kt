package com.example.pxrioverde.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComunicadoStateValidationTest {

    private fun base(type: AbsenceType) = ComunicadoState(
        type = type,
        targetApproverId = "sup-1",
        date = "10/10/2026",
        reason = "Consulta"
    )

    @Test
    fun retiradaCompletaNaoTemPendencias() {
        val s = base(AbsenceType.RETIRADA).copy(exitTime = "17:00", returnTime = "18:00")
        assertTrue(s.missingRequiredFields().isEmpty())
    }

    @Test
    fun retiradaNaoExigeHorasFaltosas() {
        val s = base(AbsenceType.RETIRADA).copy(exitTime = "17:00", returnTime = "18:00", missingHours = "")
        assertTrue(s.missingRequiredFields().isEmpty())
    }

    @Test
    fun casoDaMariaClara_semDataEhBloqueado() {
        val s = base(AbsenceType.RETIRADA).copy(date = "", exitTime = "17:00", returnTime = "18:00")
        assertEquals(listOf("data da ocorrência"), s.missingRequiredFields())
    }

    @Test
    fun dataSoComEspacosContaComoVazia() {
        val s = base(AbsenceType.FALTA).copy(date = "   ", missingDays = "1")
        assertEquals(listOf("data da ocorrência"), s.missingRequiredFields())
    }

    @Test
    fun semMotivoEhBloqueado() {
        val s = base(AbsenceType.FALTA).copy(missingDays = "1", reason = "  ")
        assertEquals(listOf("motivo"), s.missingRequiredFields())
    }

    @Test
    fun semSupervisorEhBloqueado() {
        val s = base(AbsenceType.FALTA).copy(missingDays = "1", targetApproverId = null)
        assertEquals(listOf("supervisor"), s.missingRequiredFields())
    }

    @Test
    fun atrasoExigeHoraPrevistaEEfetiva() {
        assertEquals(
            listOf("hora prevista", "hora efetiva"),
            base(AbsenceType.ATRASO).missingRequiredFields()
        )
    }

    @Test
    fun saidaAntecipadaExigeSaidaEHorasFaltas() {
        assertEquals(
            listOf("hora de saída", "horas faltas"),
            base(AbsenceType.SAIDA_ANTECIPADA).missingRequiredFields()
        )
    }

    @Test
    fun retiradaSemHorariosPedeSaidaERetorno() {
        assertEquals(
            listOf("hora de saída", "hora de retorno"),
            base(AbsenceType.RETIRADA).missingRequiredFields()
        )
    }

    @Test
    fun faltaExigeDias() {
        assertEquals(listOf("dias/horas faltosas"), base(AbsenceType.FALTA).missingRequiredFields())
    }

    @Test
    fun trocaDeHorarioExigeOriginalENovo() {
        assertEquals(
            listOf("hora original", "novo horário"),
            base(AbsenceType.TROCA_HORARIO).missingRequiredFields()
        )
    }

    @Test
    fun camposDeOutroTipoNaoContam() {
        // Sobra de hora de retorno de uma Retirada não completa um Atraso.
        val s = base(AbsenceType.ATRASO).copy(returnTime = "18:00")
        assertEquals(listOf("hora prevista", "hora efetiva"), s.missingRequiredFields())
    }

    @Test
    fun formularioVazioListaTudoDoTipoPadrao() {
        assertEquals(
            listOf("supervisor", "data da ocorrência", "hora prevista", "hora efetiva", "motivo"),
            ComunicadoState().missingRequiredFields()
        )
    }
}
