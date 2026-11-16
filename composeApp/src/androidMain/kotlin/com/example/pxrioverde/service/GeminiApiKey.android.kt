package com.example.pxrioverde.service

// A análise de vistoria agora roda no servidor (Edge Function "analyze-inspection").
// A chave do Gemini fica só nos Secrets do Supabase e não é mais embutida no app.
actual fun geminiApiKey(): String = ""
