package com.example.pxrioverde.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComunicadoCancelTest {

    private val pendente = ComunicadoState(
        type = AbsenceType.SAIDA_ANTECIPADA, date = "14/11/2026", exitTime = "17:00"
    )

    @Test
    fun comunicadoPendenteExibeBotaoDeCancelar() {
        assertTrue(pendente.canBeCancelled)
        assertFalse(pendente.isCancelled)
    }

    @Test
    fun comunicadoJaAvaliadoNaoPodeSerCancelado() {
        val avaliado = pendente.copy(adminFeedback = AdminFeedback(isAuthorized = true))
        assertFalse(avaliado.canBeCancelled)
    }

    @Test
    fun comunicadoRecusadoNaoPodeSerCancelado() {
        val recusado = pendente.copy(adminFeedback = AdminFeedback(isAuthorized = false))
        assertFalse(recusado.canBeCancelled)
    }

    @Test
    fun comunicadoJaCanceladoNaoPodeSerCanceladoDeNovo() {
        val cancelado = pendente.copy(cancelledAt = 1_763_000_000_000L)
        assertTrue(cancelado.isCancelled)
        assertFalse(cancelado.canBeCancelled)
    }

    @Test
    fun cancelamentoSemAvaliacaoNaoContaComoAvaliado() {
        val cancelado = pendente.copy(cancelledAt = 1L)
        assertNull(cancelado.adminFeedback)
    }

    // --- Leitura do que vem do banco ---

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun leituraDoBancoComCancelledAtPreenchido() {
        val raw = """{"userId":"u1","userName":"Gabryella","type":"Saída Antecipada",
            "date":"14/11/2026","createdAt":1,"cancelledAt":1763000000000}"""
        val parsed = json.decodeFromString<ComunicadoState>(raw)
        assertNotNull(parsed.cancelledAt)
        assertTrue(parsed.isCancelled)
    }

    @Test
    fun leituraDoBancoSemCancelledAtContinuaFuncionando() {
        // Banco ainda sem a migration, ou linha antiga: o campo simplesmente não vem.
        val raw = """{"userId":"u1","userName":"Gabryella","type":"Saída Antecipada",
            "date":"14/11/2026","createdAt":1}"""
        val parsed = json.decodeFromString<ComunicadoState>(raw)
        assertNull(parsed.cancelledAt)
        assertTrue(parsed.canBeCancelled)
    }

    @Test
    fun leituraComCancelledAtNuloContinuaPendente() {
        val raw = """{"userId":"u1","userName":"Gabryella","type":"Falta",
            "date":"14/11/2026","createdAt":1,"cancelledAt":null}"""
        val parsed = json.decodeFromString<ComunicadoState>(raw)
        assertFalse(parsed.isCancelled)
    }

    // --- Cancelado e reenvio ---

    @Test
    fun cancelamentoNaoMudaAChaveDeOcorrencia() {
        // A chave é igual; quem ignora os cancelados é o filtro do envio (isCancelled).
        assertEquals(pendente.occurrenceKey(), pendente.copy(cancelledAt = 1L).occurrenceKey())
    }

    @Test
    fun filtroDeDuplicataIgnoraCanceladosERecusados() {
        val existentes = listOf(
            pendente.copy(cancelledAt = 1L),
            pendente.copy(adminFeedback = AdminFeedback(isAuthorized = false))
        )
        val novo = pendente.copy()
        val duplicado = existentes.any { !it.isRejected && !it.isCancelled && it.occurrenceKey() == novo.occurrenceKey() }
        assertFalse(duplicado)
    }

    @Test
    fun filtroDeDuplicataAindaBarraPendenteAtivo() {
        val existentes = listOf(pendente.copy(cancelledAt = 1L), pendente.copy())
        val novo = pendente.copy()
        val duplicado = existentes.any { !it.isRejected && !it.isCancelled && it.occurrenceKey() == novo.occurrenceKey() }
        assertTrue(duplicado)
    }
}
