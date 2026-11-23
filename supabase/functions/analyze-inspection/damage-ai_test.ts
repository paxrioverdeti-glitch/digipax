import {
  buildInspectionResult,
  compareDamageAngle,
  damageAiEndpoint,
  mapDamageAiResponse,
} from "./damage-ai.ts";

function assert(condition: unknown, message: string): asserts condition {
  if (!condition) throw new Error(message);
}

function assertEquals(actual: unknown, expected: unknown, message: string) {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(`${message}: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}

function rejects(action: () => unknown, expectedMessage: string) {
  try {
    action();
  } catch (error) {
    assert(error instanceof Error && error.message.includes(expectedMessage), `Expected error containing "${expectedMessage}".`);
    return;
  }
  throw new Error(`Expected error containing "${expectedMessage}".`);
}

Deno.test("maps the service response, origin, severity, photo index, and normalized YX box", () => {
  const mapped = mapDamageAiResponse({
    angle: "Lateral Esquerda",
    damages: [{
      damage_type: "dent",
      description: "Porta dianteira",
      severity: "moderate",
      confidence: 0.83,
      origin: "between_trips",
      bbox: [0.1, 0.2, 0.6, 0.8],
    }],
  }, "Lateral Esquerda", 2, true);

  assertEquals(mapped, [{
    angulo: "Lateral Esquerda",
    tipo_dano: "Amassado",
    descricao: "Porta dianteira",
    gravidade: "Moderado",
    confianca: 0.83,
    foto_index: 2,
    caixa: [0.1, 0.2, 0.6, 0.8],
    origem: "entre_viagens",
  }], "Mapped service damage");
});

Deno.test("maps a this-trip damage to the final photo with existing labels", () => {
  const mapped = mapDamageAiResponse({
    angle: "Frente",
    damages: [
      {
        damage_type: "scratch",
        description: "Para-choque",
        severity: "minor",
        confidence: 0.6,
        origin: "this_trip",
        bbox: [0.05, 0.1, 0.25, 0.4],
      },
      {
        damage_type: "crack",
        description: "Farol",
        severity: "severe",
        confidence: 0.91,
        origin: "this_trip",
        bbox: [0.3, 0.4, 0.7, 0.9],
      },
    ],
  }, "Frente", 0, false);

  assertEquals(mapped.map((damage) => [damage.tipo_dano, damage.gravidade, damage.origem]), [
    ["Arranhão", "Leve", "esta_viagem"],
    ["Trinca", "Grave", "esta_viagem"],
  ], "This-trip damage labels");
  assertEquals(mapped[0].foto_index, 0, "Damage is shown on final photo for the angle");
  assertEquals(buildInspectionResult(mapped).houve_novo_dano, true, "Threshold includes confidence 0.6");
});

Deno.test("accepts a valid empty result as no damage", () => {
  const result = buildInspectionResult(mapDamageAiResponse({
    angle: "Frente",
    damages: [],
  }, "Frente", 0, false));

  assertEquals(result.houve_novo_dano, false, "Empty result flag");
  assertEquals(result.resumo_geral, "Nenhum dano novo com evidência clara.", "Empty result summary");
  assertEquals(result.precisa_revisao, false, "Empty result review flag");
});

Deno.test("rejects malformed, demo, and impossible between-trip responses", () => {
  rejects(
    () => mapDamageAiResponse({ angle: "Frente", damages: [], demo: true }, "Frente", 0, false),
    "modo demo",
  );
  rejects(
    () => mapDamageAiResponse({
      angle: "Frente",
      damages: [{
        damage_type: "scratch",
        description: "Capô",
        severity: "minor",
        confidence: 0.9,
        origin: "this_trip",
        bbox: [0.7, 0.2, 0.1, 0.8],
      }],
    }, "Frente", 0, false),
    "caixa inválida",
  );
  rejects(
    () => mapDamageAiResponse({
      angle: "Frente",
      damages: [{
        damage_type: "scratch",
        description: "Capô",
        severity: "minor",
        confidence: 0.9,
        origin: "between_trips",
        bbox: [0.1, 0.2, 0.7, 0.8],
      }],
    }, "Frente", 0, false),
    "sem foto anterior",
  );
});

Deno.test("uses the configured /compare endpoint and only adds an optional token", async () => {
  let calledUrl = "";
  let requestInit: RequestInit | undefined;
  const fetcher: typeof fetch = async (input, init) => {
    calledUrl = String(input);
    requestInit = init;
    return Response.json({ angle: "Frente", damages: [] });
  };
  await compareDamageAngle(
    "https://damage.example/api/",
    "service-secret",
    "Frente",
    new Uint8Array([1]),
    new Uint8Array([2]),
    new Uint8Array([3]),
    fetcher,
  );

  assertEquals(calledUrl, "https://damage.example/api/compare", "Service endpoint");
  const headers = new Headers(requestInit?.headers);
  assertEquals(headers.get("Authorization"), "Bearer service-secret", "Optional auth header");
  const form = requestInit?.body as FormData;
  assertEquals(form.get("angle"), "Frente", "Multipart angle");
  assert(form.get("initial_image") instanceof Blob, "Initial image is multipart file data");
  assert(form.get("final_image") instanceof Blob, "Final image is multipart file data");
  assert(form.get("previous_image") instanceof Blob, "Previous image is optional multipart file data");

  const noTokenFetcher: typeof fetch = async (_input, init) => {
    assertEquals(new Headers(init?.headers).get("Authorization"), null, "No auth header without token");
    const formWithoutPrevious = init?.body as FormData;
    assertEquals(formWithoutPrevious.has("previous_image"), false, "Previous image omitted when unavailable");
    return Response.json({ angle: "Frente", damages: [] });
  };
  await compareDamageAngle("https://damage.example", undefined, "Frente", new Uint8Array([1]), new Uint8Array([2]), undefined, noTokenFetcher);
});

Deno.test("rejects service HTTP errors, invalid JSON, and timeout with endpoint context", async () => {
  const httpFailure: typeof fetch = async () => new Response("maintenance", { status: 503 });
  try {
    await compareDamageAngle("https://damage.example", undefined, "Frente", new Uint8Array(), new Uint8Array(), undefined, httpFailure);
    throw new Error("Expected an HTTP failure.");
  } catch (error) {
    assert(error instanceof Error && error.message.includes("503"), "HTTP status must be reported");
    assert(error.message.includes("https://damage.example/compare"), "HTTP failure must identify the endpoint");
    assert(error.message.includes("maintenance"), "HTTP response message must be reported");
  }

  const invalidJson: typeof fetch = async () => new Response("not-json", { status: 200 });
  try {
    await compareDamageAngle("https://damage.example", undefined, "Frente", new Uint8Array(), new Uint8Array(), undefined, invalidJson);
    throw new Error("Expected invalid JSON failure.");
  } catch (error) {
    assert(error instanceof Error && error.message.includes("JSON inválida"), "Invalid JSON must be reported");
  }

  const timeout: typeof fetch = async () => {
    throw new DOMException("request timed out", "TimeoutError");
  };
  try {
    await compareDamageAngle("https://damage.example", undefined, "Frente", new Uint8Array(), new Uint8Array(), undefined, timeout);
    throw new Error("Expected timeout failure.");
  } catch (error) {
    assert(error instanceof Error && error.message.includes("request timed out"), "Timeout reason must be reported");
    assert(error.message.includes("https://damage.example/compare"), "Timeout must identify the endpoint");
  }
});

Deno.test("validates the configured endpoint instead of inventing a URL", () => {
  rejects(() => damageAiEndpoint(""), "não configurada");
  rejects(() => damageAiEndpoint("file:///tmp/service"), "HTTP ou HTTPS");
  rejects(() => damageAiEndpoint("http://damage.example"), "deve usar HTTPS");
  assertEquals(damageAiEndpoint("https://damage.example/v1"), "https://damage.example/v1/compare", "Configured endpoint");
  assertEquals(damageAiEndpoint("http://localhost:8000"), "http://localhost:8000/compare", "Local development endpoint");
  assertEquals(damageAiEndpoint("http://127.0.0.1:8000"), "http://127.0.0.1:8000/compare", "Loopback endpoint");
  assertEquals(damageAiEndpoint("http://192.168.1.20:8000"), "http://192.168.1.20:8000/compare", "Private local endpoint");
});

Deno.test("maps all CarDamageDetector classes to readable Portuguese labels", () => {
  const classes = [
    ["crack", "Trinca"],
    ["crash", "Dano por colisão"],
    ["dent", "Amassado"],
    ["dislocated_part", "Peça deslocada"],
    ["glass_shatter", "Vidro estilhaçado"],
    ["lamp_broken", "Farol quebrado"],
    ["no_part", "Peça faltando"],
    ["rub", "Raspão"],
    ["scratch", "Arranhão"],
    ["tire_flat", "Pneu furado"],
  ];
  const mapped = classes.map(([damageType]) => mapDamageAiResponse({
    angle: "Frente",
    damages: [{
      damage_type: damageType,
      description: "Dano",
      severity: "minor",
      confidence: 0.8,
      origin: "this_trip",
      bbox: [0.1, 0.1, 0.5, 0.5],
    }],
  }, "Frente", 0, false)[0].tipo_dano);
  assertEquals(mapped, classes.map(([, label]) => label), "Translated detector class labels");
});
