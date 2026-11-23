package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resposta da função banco-horas-app (Plataforma DP). Só existe em memória: nunca gravar em disco,
 * cache ou log (é dado pessoal do trabalhador).
 * situacao: "ativo" ou "sem_vinculo".
 */
@Serializable
data class BancoHoras(
    val situacao: String = "sem_vinculo",
    @SerialName("saldo_minutos") val saldoMinutos: Int? = null,
    @SerialName("saldo_texto") val saldoTexto: String? = null,
    @SerialName("periodo_inicio") val periodoInicio: String? = null,
    @SerialName("periodo_fim") val periodoFim: String? = null,
    @SerialName("atualizado_em") val atualizadoEm: String? = null,
    @SerialName("cpf_mascarado") val cpfMascarado: String? = null,
    // Opcionais (só o ciclo de fechamento, de periodo_inicio a periodo_fim). saldo_minutos/saldo_texto é o ACUMULADO.
    @SerialName("trabalhadas_texto") val trabalhadasTexto: String? = null,
    @SerialName("previstas_texto") val previstasTexto: String? = null,
    @SerialName("saldo_periodo_texto") val saldoPeriodoTexto: String? = null,
    // Opcionais: saldo do fechamento so pelas batidas e diferenca lancada pelo DP (calculada, nao e lista de lancamentos).
    @SerialName("batidas_texto") val batidasTexto: String? = null,
    @SerialName("lancado_dp_texto") val lancadoDpTexto: String? = null,
    // Dia 25 em que o ciclo de fechamento (26 a 25) termina, "aaaa-mm-dd". Null em dado antigo.
    @SerialName("fechamento_em") val fechamentoEm: String? = null
) {
    val ativo: Boolean get() = situacao == "ativo"
}

/** Erro já com texto em português simples, pronto para mostrar na tela. */
class BancoHorasErro(val mensagem: String) : Exception(mensagem)
