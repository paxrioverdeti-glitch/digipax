// Edge Function: analyze-inspection
// Recebe { trip_id }, valida a viagem, baixa as fotos e grava inspection_reports.
import { createClient } from "jsr:@supabase/supabase-js@2";
import {
  ANGLES,
  buildInspectionResult,
  CAPTURE_TO_ANGLE_INDEX,
  compareDamageAngle,
  mapDamageAiResponse,
} from "./damage-ai.ts";

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

  // Verify JWT and permissions here: this function remains the only app-facing boundary.
  const admin = createClient(supabaseUrl, serviceKey);
  const jwt = (req.headers.get("Authorization") ?? "").replace(/^Bearer\s+/i, "");
  if (!jwt) return json({ error: "Não autenticado" }, 401);
  const { data: userData, error: userError } = await admin.auth.getUser(jwt);
  if (userError || !userData.user) return json({ error: `Não autenticado: ${userError?.message ?? ""}` }, 401);

  const { trip_id, wait } = await req.json().catch(() => ({}));
  if (!trip_id || typeof trip_id !== "string") return json({ error: "trip_id obrigatório" }, 400);

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

  let failureReason = "";
  const work = (async (): Promise<boolean> => {
    try {
      const result = await analyze(
        admin,
        initial,
        final,
        previousFinal,
        Deno.env.get("DAMAGE_AI_BASE_URL"),
        Deno.env.get("DAMAGE_AI_API_TOKEN"),
      );
      await saveReport({
        houve_novo_dano: result.houve_novo_dano,
        resumo_geral: result.resumo_geral,
        danos_encontrados: result.danos_encontrados,
        analysis_failed: result.precisa_revisao,
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
    const res = await fetch(value, { signal: AbortSignal.timeout(30_000) });
    if (!res.ok) throw new Error(`Foto indisponível (${res.status}): ${value}`);
    return new Uint8Array(await res.arrayBuffer());
  }
  const { data, error } = await admin.storage.from("trip_photos").download(value.replace(/^trip_photos\//, ""));
  if (error || !data) throw new Error(`Foto não encontrada no storage: ${value}`);
  return new Uint8Array(await data.arrayBuffer());
}

// deno-lint-ignore no-explicit-any
async function analyze(
  admin: any,
  initial: string[],
  final: string[],
  previousFinal: string[],
  baseUrl: string | undefined,
  apiToken: string | undefined,
) {
  if (!baseUrl?.trim()) throw new Error("DAMAGE_AI_BASE_URL não configurada.");
  const [initialBytes, finalBytes, previousBytes] = await Promise.all([
    Promise.all(initial.map((url) => downloadPhoto(admin, url))),
    Promise.all(final.map((url) => downloadPhoto(admin, url))),
    previousFinal.length === 4
      ? Promise.all(previousFinal.map((url) => downloadPhoto(admin, url))).catch(() => [] as Uint8Array[])
      : Promise.resolve([] as Uint8Array[]),
  ]);
  const hasPrevious = previousBytes.length === 4;

  const perAngle = await Promise.all(
    ANGLES.map(async (angle, index) => {
      const photoIndex = CAPTURE_TO_ANGLE_INDEX[index];
      const response = await compareDamageAngle(
        baseUrl,
        apiToken,
        angle,
        initialBytes[photoIndex],
        finalBytes[photoIndex],
        hasPrevious ? previousBytes[photoIndex] : undefined,
      );
      return mapDamageAiResponse(response, angle, photoIndex, hasPrevious);
    }),
  );
  return buildInspectionResult(perAngle.flat());
}
