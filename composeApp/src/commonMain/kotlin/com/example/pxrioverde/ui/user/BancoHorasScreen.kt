package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.BancoHoras
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.util.CommonBackHandler
import com.example.pxrioverde.viewmodel.BancoHorasState
import com.example.pxrioverde.viewmodel.BancoHorasViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

private val Verde = Color(0xFF1B4332)
private val VerdeClaro = Color(0xFF2D6A4F)
private val Vermelho = Color(0xFFB3261E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BancoHorasScreen(
    viewModel: BancoHorasViewModel,
    onBack: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) { viewModel.carregar() }
    // Saldo some da memória ao sair da tela.
    DisposableEffect(Unit) { onDispose { viewModel.limpar() } }
    CommonBackHandler(enabled = true) { onBack() }

    Scaffold(
        topBar = {
            Surface(color = Verde, shadowElevation = 4.dp) {
                TopAppBar(
                    windowInsets = WindowInsets.statusBars,
                    title = { Text("Meu banco de horas", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(CorporateIcons.Back, "Voltar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Verde)
                )
            }
        },
        containerColor = Color(0xFFF5F7F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val e = estado) {
                is BancoHorasState.Carregando -> {
                    Spacer(Modifier.height(80.dp))
                    CircularProgressIndicator(color = Verde)
                    Spacer(Modifier.height(16.dp))
                    Text("Buscando seu saldo...", color = Color.Gray)
                }
                is BancoHorasState.Erro -> {
                    Cartao {
                        Text(
                            e.mensagem,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF333333)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.carregar() },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeClaro)
                        ) { Text("Tentar de novo", fontWeight = FontWeight.Bold) }
                    }
                }
                is BancoHorasState.Pronto -> Saldo(e.dados)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Só você vê este saldo. Ele vem do sistema de ponto da empresa e é usado apenas para esta consulta.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Cartao(conteudo: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = conteudo
        )
    }
}

@Composable
private fun Saldo(dados: BancoHoras) {
    Cartao {
        if (!dados.ativo) {
            Icon(CorporateIcons.Clock, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "Seu banco de horas ainda não foi liberado. Fale com o DP.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF333333)
            )
            return@Cartao
        }

        val minutos = dados.saldoMinutos
        Icon(CorporateIcons.Clock, null, tint = corDoSaldo(minutos), modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))

        if (minutos == null) {
            Text(
                "Ainda não conseguimos ler seu saldo. Tente mais tarde.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF333333)
            )
        } else {
            val (frase, destaque) = fraseDoSaldo(minutos)
            Text(frase, fontSize = 18.sp, color = Color(0xFF333333), textAlign = TextAlign.Center)
            if (destaque.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    destaque,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = corDoSaldo(minutos),
                    textAlign = TextAlign.Center
                )
            }
        }

        val ate = dataCurta(dados.periodoFim)
        if (minutos != null && ate != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Saldo acumulado até $ate (soma dos fechamentos anteriores e deste)",
                fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
            )
        }
        val fechamento = dataDiaMes(dados.fechamentoEm)
        val inicioCiclo = dataDiaMes(dados.periodoInicio)
        val periodo = textoPeriodo(dados.periodoInicio, dados.periodoFim)
        // Só mostra o quadro quando os três valores vieram com número (ex.: "152h30"); nunca "h" ou "-h" sozinho.
        fun valido(t: String?) = t != null && t.any { it.isDigit() }
        if (minutos != null && periodo != null &&
            valido(dados.trabalhadasTexto) && valido(dados.previstasTexto) && valido(dados.saldoPeriodoTexto)
        ) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F7F6), RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text(
                    if (fechamento != null) "Neste fechamento" else "Neste período",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333)
                )
                Text(
                    if (fechamento != null && inicioCiclo != null) "Período de fechamento: $inicioCiclo a $fechamento"
                    else periodo.removePrefix("Período: "),
                    fontSize = 13.sp, color = Color.Gray
                )
                val ateCurto = dataDiaMes(dados.periodoFim)
                if (ateCurto != null) {
                    Text(
                        if (fechamento != null) "Fechamento em $fechamento · atualizado até $ateCurto" else "Atualizado até $ateCurto",
                        fontSize = 13.sp, color = Color.Gray
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinhaMes("Horas trabalhadas", dados.trabalhadasTexto)
                LinhaMes("Horas previstas", dados.previstasTexto)
                val lancado = if (valido(dados.batidasTexto) && valido(dados.lancadoDpTexto)) dados.lancadoDpTexto else null
                if (lancado != null) LinhaMes("Pelas suas batidas", dados.batidasTexto)
                if (lancado != null) LinhaMes("Lan\u00e7ado pelo DP (ex.: saldo inicial, feriado trabalhado, ajustes)", lancado)
                LinhaMes("Saldo deste fechamento", dados.saldoPeriodoTexto)
                if (lancado != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "O DP acrescentou horas al\u00e9m das que voc\u00ea bateu. O sistema de ponto n\u00e3o informa o motivo de cada lan\u00e7amento; os detalhes ficam com o DP.",
                        fontSize = 13.sp, color = Color.Gray
                    )
                }
            }
        } else if (periodo != null) {
            Spacer(Modifier.height(16.dp))
            Text(periodo, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
        if (minutos != null && minutos < 0) {
            Spacer(Modifier.height(16.dp))
            PorqueNegativo()
        }
        val atualizado = textoAtualizado(dados.atualizadoEm)
        if (atualizado != null) {
            Spacer(Modifier.height(4.dp))
            Text(atualizado, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
        if (!dados.cpfMascarado.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("Cadastro: ${dados.cpfMascarado}", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "O saldo é calculado pelo sistema de ponto. Se algo parecer errado, fale com o DP.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LinhaMes(titulo: String, valor: String?) {
    if (valor == null) return
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titulo, fontSize = 14.sp, color = Color(0xFF333333))
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
    }
}

/** Texto neutro: não afirma falta e não culpa ninguém. */
@Composable
private fun PorqueNegativo() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(CorporateIcons.Idea, null, tint = Color(0xFF8D6E00), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Por que meu saldo está negativo?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Este número não significa que você faltou. O saldo é calculado pelo sistema de ponto e pode incluir dias de meses anteriores. " +
                "Também pode haver um ponto ou um ajuste que ainda não foi tratado, como uma compensação, folga ou atestado. " +
                "Procure o DP para conferir junto com você.",
            fontSize = 14.sp, color = Color(0xFF333333)
        )
    }
}

private fun corDoSaldo(minutos: Int?): Color = when {
    minutos == null -> Color.Gray
    minutos < 0 -> Vermelho
    else -> VerdeClaro
}

/** Devolve (frase, valor em destaque). Ex.: ("Você tem", "12h30 a favor"). */
internal fun fraseDoSaldo(minutos: Int): Pair<String, String> = when {
    minutos > 0 -> "Você tem" to "${formatarHoras(minutos)} a favor"
    minutos < 0 -> "Seu saldo acumulado está negativo em" to "-${formatarHoras(minutos)}"
    else -> "Seu banco de horas está zerado." to ""
}

/** 750 -> "12h30"; 720 -> "12h"; 45 -> "45min". Usa o valor absoluto. */
internal fun formatarHoras(minutos: Int): String {
    val total = abs(minutos)
    val h = total / 60
    val m = total % 60
    return when {
        h == 0 -> "${m}min"
        m == 0 -> "${h}h"
        else -> "${h}h${m.toString().padStart(2, '0')}"
    }
}

private fun dataCurta(iso: String?): String? = runCatching {
    val d = LocalDate.parse(iso!!.take(10))
    "${d.dayOfMonth.toString().padStart(2, '0')}/${d.monthNumber.toString().padStart(2, '0')}/${d.year}"
}.getOrNull()

/** "2026-11-25" -> "25/11" (sem ano; o dia escrito, sem fuso). */
private fun dataDiaMes(iso: String?): String? = runCatching {
    val d = LocalDate.parse(iso!!.take(10))
    "${d.dayOfMonth.toString().padStart(2, '0')}/${d.monthNumber.toString().padStart(2, '0')}"
}.getOrNull()

private fun textoPeriodo(inicio: String?, fim: String?): String? {
    val i = dataCurta(inicio)
    val f = dataCurta(fim)
    return if (i != null && f != null) "Período: $i a $f" else null
}

private fun textoAtualizado(iso: String?): String? = runCatching {
    val dt = Instant.parse(iso!!).toLocalDateTime(TimeZone.currentSystemDefault())
    val dia = "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
    val hora = "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    "Atualizado em $dia às $hora"
}.getOrNull()
