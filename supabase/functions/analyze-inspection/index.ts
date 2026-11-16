// Edge Function: analyze-inspection
// Recebe { trip_id }, confere se a viagem é do usuário logado, baixa as 8 fotos,
// pede a comparação ao Gemini e salva o resultado em inspection_reports.
// A chave do Gemini fica SÓ aqui (secret GEMINI_API_KEY), nunca no app.
import { createClient } from "jsr:@supabase/supabase-js@2";
import { encodeBase64 } from "jsr:@std/encoding@1/base64";

// O Google parou de liberar o gemini-2.5-flash para chaves novas; o atual é o 3.8 Flash.
const GEMINI_MODEL = "gemini-3.8-flash";


// O app captura na ordem Frente, Traseira, Esquerda, Direita; o prompt pede Frente, Esquerda, Traseira, Direita.
const ANGLES = ["Frente", "Lateral Esquerda", "Traseira", "Lateral Direita"];
const CAPTURE_TO_PROMPT = [0, 2, 1, 3];

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { ...cors, "Content-Type": "application/json" } });

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "Use POST" }, 405);

  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  const geminiKey = Deno.env.get("GEMINI_API_KEY");
  if (!geminiKey) return json({ error: "GEMINI_API_KEY não configurada" }, 500);

  // 1. Quem está chamando? A própria função confere o login (por isso o "Verify JWT"
  // do painel pode ficar DESLIGADO, que é o exigido pelas chaves novas do Supabase).
  const admin = createClient(supabaseUrl, serviceKey);
  const jwt = (req.headers.get("Authorization") ?? "").replace(/^Bearer\s+/i, "");
  if (!jwt) return json({ error: "Não autenticado" }, 401);
  const { data: userData, error: userError } = await admin.auth.getUser(jwt);
  if (userError || !userData.user) return json({ error: `Não autenticado: ${userError?.message ?? ""}` }, 401);

  // wait = true: espera a análise terminar antes de responder (usado pelo gestor
  // no "Analisar agora" e no "Analisar todas", para ir uma viagem por vez).
  const { trip_id, wait } = await req.json().catch(() => ({}));
  if (!trip_id || typeof trip_id !== "string") return json({ error: "trip_id obrigatório" }, 400);

  // 2. A viagem existe? Quem pode pedir: o motorista dono da viagem OU um gestor.
  const { data: trip, error: tripError } = await admin
    .from("trips")
    .select("id, user_id, car_id, start_time, photo_urls, end_photo_urls")
    .eq("id", trip_id)
    .maybeSingle();
  if (tripError || !trip) return json({ error: "Viagem não encontrada" }, 404);
  if (trip.user_id !== userData.user.id) {
    const { data: profile } = await admin
      .from("profiles")
      .select("role")
      .eq("id", userData.user.id)
      .maybeSingle();
    const isManager = ["ADMIN", "SUPERVISOR", "ENCARREGADO"].includes(String(profile?.role ?? ""));
    if (!isManager) return json({ error: "Sem permissão para analisar esta viagem" }, 403);
  }

  const initial: string[] = trip.photo_urls ?? [];
  const final: string[] = trip.end_photo_urls ?? [];
  if (initial.length !== 4 || final.length !== 4) {
    return json({ error: "A viagem precisa de 4 fotos iniciais e 4 finais" }, 422);
  }

  // Passo 5: a viagem ANTERIOR do mesmo carro (a última que terminou antes desta começar).
  // Comparando o FIM dela com o INÍCIO desta, sabemos se algo aconteceu com o carro entre as duas.
  let previousFinal: string[] = [];
  if (trip.car_id && trip.start_time) {
    const { data: prev } = await admin
      .from("trips")
      .select("id, end_photo_urls, end_time")
      .eq("car_id", trip.car_id)
      .neq("id", trip.id)
      .lt("end_time", trip.start_time)
      .order("end_time", { ascending: false })
      .limit(1)
      .maybeSingle();
    if (prev?.end_photo_urls?.length === 4) previousFinal = prev.end_photo_urls;
  }

  // Grava o relatório. Se o banco recusar, LANÇA o erro (antes ele era ignorado em silêncio).
  const saveReport = async (fields: Record<string, unknown>) => {
    const { error } = await admin.from("inspection_reports").upsert(
      {
        trip_id: trip.id,
        car_id: trip.car_id,
        user_id: trip.user_id,
        analyzed_at: Date.now(),
        ...fields,
      },
      { onConflict: "trip_id" },
    );
    if (error) throw new Error(`Banco recusou salvar o relatório: ${error.message}`);
  };

  // 3. Registra "em análise" na hora, para o admin já ver a vistoria.
  try {
    await saveReport({
      houve_novo_dano: false,
      resumo_geral: "Análise automática em andamento. Revise as fotos se necessário.",
      danos_encontrados: [],
      analysis_failed: true,
    });
  } catch (e) {
    console.error(String(e));
    return json({ error: String(e) }, 500);
  }

  // 4. A análise continua em segundo plano; o app recebe a resposta na hora.
  let failureReason = "";
  const work = (async (): Promise<boolean> => {
    try {
      const result = await analyze(admin, initial, final, previousFinal, geminiKey);
      await saveReport({
        houve_novo_dano: !!result.houve_novo_dano,
        resumo_geral: result.resumo_geral ?? "",
        danos_encontrados: result.danos_encontrados ?? [],
        // Fotos que não servem (ex.: não é um carro) contam como "revisar", nunca como "limpo"
        analysis_failed: !!result.precisa_revisao,
      });
      return true;
    } catch (e) {
      console.error(`Falha na análise da viagem ${trip.id}:`, e);
      failureReason = e instanceof Error ? e.message : String(e);
      try {
        await saveReport({
          houve_novo_dano: false,
          resumo_geral: "Análise automática indisponível. Revise as fotos manualmente.",
          danos_encontrados: [],
          analysis_failed: true,
        });
      } catch (saveError) {
        console.error(String(saveError));
      }
      return false;
    }
  })();

  // Gestor pediu para esperar: responde só quando terminar (uma viagem por vez).
  if (wait === true) {
    const ok = await work;
    return json(
      ok ? { status: "concluida", trip_id: trip.id } : { status: "falhou", trip_id: trip.id, motivo: failureReason.slice(0, 500) },
      ok ? 200 : 502,
    );
  }

  // deno-lint-ignore no-explicit-any
  const runtime = (globalThis as any).EdgeRuntime;
  if (runtime?.waitUntil) runtime.waitUntil(work);
  else await work;

  return json({ status: "em_analise", trip_id: trip.id }, 202);
});

// deno-lint-ignore no-explicit-any
async function downloadPhoto(admin: any, raw: string): Promise<Uint8Array> {
  const value = raw.trim();
  if (value.startsWith("http://") || value.startsWith("https://")) {
    const res = await fetch(value);
    if (!res.ok) throw new Error(`Foto indisponível (${res.status}): ${value}`);
    return new Uint8Array(await res.arrayBuffer());
  }
  const { data, error } = await admin.storage.from("trip_photos").download(value.replace(/^trip_photos\//, ""));
  if (error || !data) throw new Error(`Foto não encontrada no storage: ${value}`);
  return new Uint8Array(await data.arrayBuffer());
}

// deno-lint-ignore no-explicit-any
async function analyze(admin: any, initial: string[], final: string[], previousFinal: string[], geminiKey: string) {
  const [initialBytes, finalBytes, previousBytes] = await Promise.all([
    Promise.all(initial.map((u) => downloadPhoto(admin, u))),
    Promise.all(final.map((u) => downloadPhoto(admin, u))),
    // Se a foto da viagem anterior não baixar, seguimos sem essa comparação
    previousFinal.length === 4
      ? Promise.all(previousFinal.map((u) => downloadPhoto(admin, u))).catch(() => [] as Uint8Array[])
      : Promise.resolve([] as Uint8Array[]),
  ]);
  const hasPrevious = previousBytes.length === 4;

  // UM pedido ao Gemini por viagem (antes eram 4 a 8). O plano gratuito do Gemini
  // permite poucos pedidos por dia, então isso multiplica quantas viagens cabem no limite.
  const result = await analyzeTrip(initialBytes, finalBytes, hasPrevious ? previousBytes : null, geminiKey);
  const porAngulo = new Map((result.angulos ?? []).map((a) => [a.angulo, a]));

  const todos = ANGLES.flatMap((angle, i) =>
    (porAngulo.get(angle)?.danos ?? []).map((d) => ({
      angulo: angle,
      tipo_dano: d.tipo_dano,
      descricao: d.descricao,
      gravidade: d.gravidade,
      confianca: clamp01(d.confianca),
      // Índice da foto (ordem de captura) onde o quadrado deve ser desenhado:
      // dano desta viagem -> foto FINAL; dano entre viagens -> foto INICIAL.
      foto_index: CAPTURE_TO_PROMPT[i],
      caixa: normalizeBox(d.caixa),
      origem: hasPrevious && d.origem === "entre_viagens" ? "entre_viagens" : "esta_viagem",
    }))
  );
  const danos = todos.filter((d) => d.origem === "esta_viagem");
  const entreViagens = todos.filter((d) => d.origem === "entre_viagens");

  // Só conta o que a IA tem pelo menos 60% de certeza.
  const confirmados = danos.filter((d) => d.confianca >= CONFIDENCE_THRESHOLD);
  const entreConfirmados = entreViagens.filter((d) => d.confianca >= CONFIDENCE_THRESHOLD);
  const fotosRuins = ANGLES.map((angle) => {
    const a = porAngulo.get(angle);
    if (!a) return `${angle} (sem resposta da IA)`;
    return a.foto_adequada === false ? `${angle} (${a.motivo_foto_inadequada || "foto inadequada"})` : null;
  }).filter(Boolean);

  const partes: string[] = [];
  if (confirmados.length > 0) {
    partes.push(`${confirmados.length} possível(is) dano(s) novo(s): ${confirmados.map((d) => `${d.tipo_dano} na ${d.angulo}`).join("; ")}.`);
  } else {
    partes.push("Nenhum dano novo com evidência clara.");
  }
  const duvidosos = danos.length - confirmados.length;
  if (duvidosos > 0) partes.push(`${duvidosos} indício(s) com baixa confiança (provável reflexo, sujeira ou ângulo).`);
  if (fotosRuins.length > 0) partes.push(`Revisar manualmente: ${fotosRuins.join(", ")}.`);
  if (entreConfirmados.length > 0) {
    partes.push(`Atenção: ${entreConfirmados.length} dano(s) já estavam no carro quando este motorista o pegou (surgiram depois da viagem anterior): ${entreConfirmados.map((d) => `${d.tipo_dano} na ${d.angulo}`).join("; ")}.`);
  }

  return {
    houve_novo_dano: confirmados.length > 0,
    resumo_geral: partes.join(" "),
    // Ordena do mais provável para o menos provável
    danos_encontrados: todos.sort((a, b) => b.confianca - a.confianca),
    // Foto ruim ou dano entre viagens precisam de olho humano
    precisa_revisao: fotosRuins.length > 0 || entreConfirmados.length > 0,
  };
}

const CONFIDENCE_THRESHOLD = 0.6;

type Dano = {
  tipo_dano: string;
  descricao: string;
  gravidade: string;
  confianca: number;
  caixa?: number[];
  origem?: string;
};
type AngleResult = { angulo: string; foto_adequada: boolean; motivo_foto_inadequada?: string; danos: Dano[] };
type TripResult = { angulos: AngleResult[] };

// Caixa no padrão do Gemini: [ymin, xmin, ymax, xmax] de 0 a 1000. Devolve de 0 a 1, ou null se vier inválida.
function normalizeBox(box: unknown): number[] | null {
  if (!Array.isArray(box) || box.length !== 4) return null;
  const v = box.map((n) => Math.max(0, Math.min(1000, Number(n))) / 1000);
  if (v.some((n) => Number.isNaN(n))) return null;
  const [ymin, xmin, ymax, xmax] = v;
  if (ymax <= ymin || xmax <= xmin) return null;
  return [ymin, xmin, ymax, xmax];
}

const clamp01 = (n: unknown) => Math.max(0, Math.min(1, typeof n === "number" ? n : Number(n) || 0));

// Formato fixo: o Gemini é obrigado a responder exatamente nesta estrutura (um item por lado do carro).
const TRIP_SCHEMA = {
  type: "OBJECT",
  properties: {
    angulos: {
      type: "ARRAY",
      items: {
        type: "OBJECT",
        properties: {
          angulo: { type: "STRING", enum: ANGLES },
          foto_adequada: { type: "BOOLEAN", description: "As fotos deste lado mostram o carro com nitidez e enquadramento suficientes para comparar?" },
          motivo_foto_inadequada: { type: "STRING", description: "Se foto_adequada for false, explique em poucas palavras. Senão, vazio." },
          danos: {
            type: "ARRAY",
            items: {
              type: "OBJECT",
              properties: {
                tipo_dano: { type: "STRING", description: "Ex.: Amassado, Arranhão, Trinca, Peça quebrada, Peça faltando" },
                descricao: { type: "STRING", description: "Onde exatamente está (ex.: porta traseira, perto da maçaneta) e como é" },
                gravidade: { type: "STRING", enum: ["Leve", "Moderado", "Grave"] },
                confianca: { type: "NUMBER", description: "De 0 a 1: quanta certeza de que é dano NOVO e real (não reflexo, sujeira ou ângulo)" },
                origem: { type: "STRING", enum: ["esta_viagem", "entre_viagens"] },
                caixa: {
                  type: "ARRAY",
                  items: { type: "NUMBER" },
                  description: "Retângulo em volta do dano: [ymin, xmin, ymax, xmax] de 0 a 1000. Na foto FIM se origem=esta_viagem; na foto INÍCIO se origem=entre_viagens.",
                },
              },
              required: ["tipo_dano", "descricao", "gravidade", "confianca", "origem"],
            },
          },
        },
        required: ["angulo", "foto_adequada", "danos"],
      },
    },
  },
  required: ["angulos"],
};

const anglePrompt = (angle: string, hasPrevious: boolean) => `Você é um vistoriador especialista em lataria de veículos de frota corporativa.
Você vai receber fotos de UM lado do veículo: ${angle}. Estas são as fotos:
- Foto INÍCIO: tirada quando o motorista pegou o carro.
- Foto FIM: tirada quando o motorista devolveu o carro.${hasPrevious ? `
- Foto ANTERIOR: tirada no fim da viagem anterior deste mesmo carro (outro motorista).` : ""}
Tarefa:
1. Danos novos desta viagem: aparecem na foto FIM e NÃO aparecem na foto INÍCIO. Use origem = "esta_viagem" e a caixa na foto FIM.${hasPrevious ? `
2. Danos entre viagens: aparecem na foto INÍCIO e NÃO aparecem na foto ANTERIOR. Use origem = "entre_viagens" e a caixa na foto INÍCIO.` : ""}
Regras:
- Compare somente fotos do MESMO lado.
- Danos que já existiam na foto mais antiga NÃO são novos. Não os liste.
- Reflexos, sombras, sujeira, poeira, água, gotas e diferença de luz ou de ângulo NÃO são danos.
- Para cada dano, dê "confianca" de 0 a 1. Use valores abaixo de 0.5 quando houver dúvida (ex.: pode ser reflexo).
- Se alguma foto de um lado estiver escura, tremida, cortada ou mostrar outra coisa, marque foto_adequada = false e explique.
- Para cada dano, informe "caixa": o retângulo em volta dele no formato [ymin, xmin, ymax, xmax] de 0 a 1000.
- Responda com um único objeto para este lado (${angle}). Sem dano, use "danos": [].
Responda em português.`;

// Um pedido por LADO do carro, os 4 ao mesmo tempo. Cada pedido leva só 2 ou 3 fotos,
// então responde rápido (antes era 1 pedido com 12 fotos e estourava o tempo).
// Se um lado falhar, ele vira "sem resposta da IA" (revisão manual) e os outros seguem.
async function analyzeTrip(
  initial: Uint8Array[],
  final: Uint8Array[],
  previous: Uint8Array[] | null,
  geminiKey: string,
): Promise<TripResult> {
  const settled = await Promise.allSettled(
    ANGLES.map((angle, i) => analyzeAngle(angle, CAPTURE_TO_PROMPT[i], initial, final, previous, geminiKey)),
  );
  const ok = settled.filter((s): s is PromiseFulfilledResult<AngleResult> => s.status === "fulfilled");
  if (ok.length === 0) {
    const first = settled[0] as PromiseRejectedResult;
    throw first.reason instanceof Error ? first.reason : new Error(String(first.reason));
  }
  for (const s of settled) if (s.status === "rejected") console.error("Lado falhou:", s.reason);
  return { angulos: ok.map((s) => s.value) };
}

async function analyzeAngle(
  angle: string,
  captureIndex: number,
  initial: Uint8Array[],
  final: Uint8Array[],
  previous: Uint8Array[] | null,
  geminiKey: string,
): Promise<AngleResult> {
  const img = (bytes: Uint8Array) => ({ inline_data: { mime_type: "image/jpeg", data: encodeBase64(bytes) } });
  const parts: Record<string, unknown>[] = [{ text: anglePrompt(angle, previous !== null) }];
  if (previous) parts.push({ text: `--- ${angle} - Foto ANTERIOR ---` }, img(previous[captureIndex]));
  parts.push({ text: `--- ${angle} - Foto INÍCIO ---` }, img(initial[captureIndex]));
  parts.push({ text: `--- ${angle} - Foto FIM ---` }, img(final[captureIndex]));

  const requestBody = JSON.stringify({
    contents: [{ role: "user", parts }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: TRIP_SCHEMA.properties.angulos.items,
      temperature: 0.1,
      // Raciocínio "baixo": comparar fotos não precisa de muito, e gasta menos crédito.
      thinkingConfig: { thinkingLevel: "low" },
    },
  });

  // 503 (Google sobrecarregado): espera e tenta de novo (5s e 15s).
  // 429 de LIMITE DIÁRIO não adianta repetir: só gastaria tempo.
  const delays = [5_000, 15_000];
  for (let attempt = 0; ; attempt++) {
    const res = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json", "x-goog-api-key": geminiKey },
        body: requestBody,
        signal: AbortSignal.timeout(60_000),
      },
    );
    const body = await res.json().catch(() => ({}));
    const message: string = body?.error?.message ?? JSON.stringify(body);
    const dailyQuota = res.status === 429 && /per day|PerDay|free_tier/i.test(message);
    if (dailyQuota) {
      throw new Error(`Gemini 429: limite diário do plano gratuito do Gemini atingido. ${message}`);
    }
    if ((res.status === 503 || res.status === 429) && attempt < delays.length) {
      console.warn(`Gemini ${res.status}, nova tentativa em ${delays[attempt] / 1000}s`);
      await new Promise((r) => setTimeout(r, delays[attempt]));
      continue;
    }
    if (!res.ok) throw new Error(`Gemini ${res.status}: ${message}`);

    const text: string = body?.candidates?.[0]?.content?.parts?.map((p: { text?: string }) => p.text ?? "").join("") ?? "";
    const parsed = JSON.parse(text) as AngleResult;
    return { ...parsed, angulo: angle, danos: parsed.danos ?? [] };
  }
}