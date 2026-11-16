package com.example.pxrioverde.ui.platform

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * "Voltar" do iPhone. O iPhone nao tem botao fisico de voltar: as telas registram aqui
 * o que fazer ao voltar (via CommonBackHandler) e o gesto de deslizar da borda esquerda
 * chama o registro mais recente que estiver ativo (a tela mais "de dentro").
 * Exclusivo do iOS: o Android continua usando o botao/gesto de voltar do proprio sistema.
 */
object IosBackDispatcher {
    class Entry(val isEnabled: () -> Boolean, val onBack: () -> Unit)

    private val entries = mutableListOf<Entry>()

    fun add(entry: Entry) {
        entries.add(entry)
    }

    fun remove(entry: Entry) {
        entries.remove(entry)
    }

    /** Executa o "voltar" da tela mais recente. Retorna false se nenhuma tela quis tratar. */
    fun dispatch(): Boolean {
        val entry = entries.lastOrNull { it.isEnabled() } ?: return false
        entry.onBack()
        return true
    }
}

/**
 * Gesto de voltar do iPhone: tocar perto da borda esquerda e arrastar para a direita.
 * So reage quando o arrasto e claramente horizontal; toques e rolagens normais passam direto.
 */
fun Modifier.iosEdgeSwipeBack(haptic: HapticFeedback? = null): Modifier = pointerInput(haptic) {
    val edgeWidth = 24.dp.toPx()
    val triggerDistance = 72.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        if (down.position.x > edgeWidth) return@awaitEachGesture

        var triggered = false
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break

            val dx = change.position.x - down.position.x
            val dy = abs(change.position.y - down.position.y)
            if (!triggered && dx > triggerDistance && dx > dy * 1.5f) {
                triggered = true
                // Vibracao leve, como nos apps da Apple, so quando alguma tela realmente voltou.
                if (IosBackDispatcher.dispatch()) {
                    haptic?.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                }
            }
            // Depois de voltar, o resto do arrasto nao chega na tela de baixo.
            if (triggered) change.consume()
        }
    }
}

/**
 * Fecha o teclado ao tocar fora dele (em area que nao e botao nem campo de texto).
 * Toques em botoes e campos continuam funcionando normalmente, porque eles consomem o toque.
 */
fun Modifier.iosDismissKeyboardOnTapOutside(focusManager: FocusManager): Modifier =
    pointerInput(focusManager) {
        detectTapGestures(onTap = { focusManager.clearFocus() })
    }
