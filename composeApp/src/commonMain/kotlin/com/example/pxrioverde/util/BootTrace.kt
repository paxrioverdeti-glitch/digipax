package com.example.pxrioverde.util

/**
 * Ponte de "migalhas de pao" para diagnostico de inicializacao.
 *
 * Em codigo compartilhado (commonMain) podemos marcar etapas da abertura do app.
 * No iPhone (iosMain) essas etapas sao guardadas e exibidas na tela de diagnostico
 * para rastrear travamentos de tela branca. Nas outras plataformas vira um no-op.
 */
expect fun bootTrace(etapa: String)
