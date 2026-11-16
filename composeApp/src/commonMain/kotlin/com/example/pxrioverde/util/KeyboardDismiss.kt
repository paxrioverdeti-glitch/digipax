package com.example.pxrioverde.util

import androidx.compose.ui.Modifier

/**
 * Fecha o teclado ao tocar fora dos campos, SO no iPhone (que nao tem botao de voltar
 * para esconder o teclado). No Android e no desktop nao faz nada: o comportamento la
 * continua exatamente igual.
 *
 * Usado nas janelas que abrem por cima (AlertDialog), que tem uma raiz propria e por isso
 * nao recebem a regra aplicada na raiz do app do iPhone.
 */
expect fun Modifier.dismissKeyboardOnTapIos(): Modifier
