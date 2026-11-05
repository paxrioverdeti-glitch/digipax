package com.example.pxrioverde

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import com.example.pxrioverde.di.initKoin
import com.example.pxrioverde.di.koinInitError
import kotlinx.coroutines.delay
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import platform.UIKit.UIViewController

/**
 * Captura global de erros no iOS.
 *
 * No Kotlin/Native (iPhone), uma excecao nao tratada dentro de uma corrotina em
 * segundo plano NAO e "engolida" como no Android: o runtime chama abort() e mata
 * o app inteiro (SIGABRT). Aqui registramos a mensagem + o rastro da ultima falha
 * para conseguir EXIBI-LA na tela em vez de ver apenas o crash silencioso.
 */
object IosCrashReporter {
    var lastError: String? = null

    fun record(throwable: Throwable) {
        if (lastError != null) return // mantem o PRIMEIRO erro, que costuma ser a causa raiz
        val tipo = throwable::class.simpleName ?: "Throwable"
        val msg = throwable.message ?: throwable.toString()
        val rastro = throwable.stackTraceToString().take(1800)
        lastError = "[$tipo] $msg\n\n$rastro"
        println("DEBUG: IosCrashReporter capturou: $tipo - $msg")
    }
}

/**
 * "Migalhas de pao" da inicializacao no iPhone.
 *
 * Vai guardando cada etapa da abertura do app. Se o app travar com tela branca,
 * a ULTIMA etapa registrada aqui aponta exatamente onde ele parou. A tela de
 * diagnostico mostra essa lista mesmo quando nao houve erro, para o usuario
 * conseguir enviar um print do ponto do travamento.
 */
object IosBootTrace {
    private val etapas = mutableListOf<String>()

    fun passo(etapa: String) {
        // Evita encher a lista com a mesma etapa repetida (os Composables redesenham
        // varias vezes). So registramos quando a etapa muda em relacao a ultima.
        if (etapas.lastOrNull() == etapa) return
        etapas.add(etapa)
        println("DEBUG: [boot] $etapa")
    }

    fun texto(): String =
        if (etapas.isEmpty()) "(nenhuma etapa registrada)"
        else etapas.mapIndexed { i, e -> "${i + 1}. $e" }.joinToString("\n")
}

/**
 * Instala o gancho nativo do Kotlin/Native que e chamado ANTES do abort().
 * Em vez de o app morrer calado, guardamos o erro para a tela de diagnostico.
 */
@OptIn(ExperimentalNativeApi::class)
private fun installCrashHook() {
    try {
        setUnhandledExceptionHook { throwable ->
            IosCrashReporter.record(throwable)
            // Nao relancamos: evitamos o abort() e deixamos a UI exibir o erro.
        }
    } catch (e: Throwable) {
        println("DEBUG: nao foi possivel instalar o hook de excecao: ${e.message}")
    }
}

@Composable
fun SafeApp() {
    // Mostra o erro do Koin (inicializacao de dependencias) OU qualquer erro
    // capturado globalmente, com rolagem para caber na tela. Reavalia a cada
    // segundo para pegar um erro que ocorra logo apos a abertura.
    var runtimeError by remember { mutableStateOf(IosCrashReporter.lastError) }
    var trace by remember { mutableStateOf(IosBootTrace.texto()) }

    LaunchedEffect(Unit) {
        repeat(20) {
            delay(500)
            // Atualiza o erro e as migalhas durante os primeiros 10 segundos,
            // para capturar uma falha que ocorra logo apos a abertura.
            if (runtimeError == null && IosCrashReporter.lastError != null) {
                runtimeError = IosCrashReporter.lastError
            }
            trace = IosBootTrace.texto()
        }
    }

    val errorToShow = koinInitError ?: runtimeError

    if (errorToShow != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(
                    text = "Falha ao iniciar o aplicativo",
                    color = Color.Red,
                    fontSize = 20.sp
                )
                Text(
                    text = "\nEtapas que rodaram antes da falha:\n$trace",
                    color = Color(0xFF444444),
                    fontSize = 12.sp
                )
                Text(
                    text = "\nEnvie um print desta tela ao suporte:\n\n$errorToShow",
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }
        }
    } else {
        App()
    }
}

fun MainViewController(): UIViewController {
    IosBootTrace.passo("MainViewController chamado")
    installCrashHook()
    IosBootTrace.passo("Hook de excecao instalado")
    try {
        initKoin()
        IosBootTrace.passo("initKoin concluido (erro Koin: ${koinInitError ?: "nenhum"})")
    } catch (e: Throwable) {
        IosBootTrace.passo("initKoin LANCOU excecao")
        IosCrashReporter.record(e)
    }
    IosBootTrace.passo("Criando ComposeUIViewController")
    return ComposeUIViewController { SafeApp() }
}
