export const DAMAGE_AI_TIMEOUT_MS = 45_000;
export const CONFIDENCE_THRESHOLD = 0.6;

export const ANGLES = ["Frente", "Lateral Esquerda", "Traseira", "Lateral Direita"] as const;
export const CAPTURE_TO_ANGLE_INDEX = [0, 2, 1, 3] as const;

type DamageAiDamage = {
  damage_type: string;
  description: string;
  severity: "minor" | "moderate" | "severe";
  confidence: number;
  origin: "this_trip" | "between_trips";
  bbox: [number, number, number, number];
};

type ReportDamage = {
  angulo: string;
  tipo_dano: string;
  descricao: string;
  gravidade: string;
  confianca: number;
  foto_index: number;
  caixa: number[];
  origem: "esta_viagem" | "entre_viagens";
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function mapDamageType(value: string): string {
  const normalized = value.trim().toLocaleLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    .replace(/[\s-]+/g, "_");
  const labels: Record<string, string> = {
    crack: "Trinca",
    crash: "Dano por colisão",
    dent: "Amassado",
    amassado: "Amassado",
    dislocated_part: "Peça deslocada",
    glass_shatter: "Vidro estilhaçado",
    lamp_broken: "Farol quebrado",
    no_part: "Peça faltando",
    paint_damage: "Dano na pintura",
    rub: "Raspão",
    scratch: "Arranhão",
    arranhao: "Arranhão",
    tire_flat: "Pneu furado",
    broken: "Peça quebrada",
    broken_part: "Peça quebrada",
    "peca_quebrada": "Peça quebrada",
    missing: "Peça faltando",
    missing_part: "Peça faltando",
    "peca_faltando": "Peça faltando",
    trinca: "Trinca",
  };
  return labels[normalized] ?? (value.trim() ? value.trim()[0].toLocaleUpperCase() + value.trim().slice(1) : "Dano");
}

function validateDamage(value: unknown, angle: string): DamageAiDamage {
  if (!isRecord(value)) throw new Error(`Resposta inválida do serviço de análise (${angle}): dano inválido.`);
  const { damage_type, description, severity, confidence, origin, bbox } = value;
  if (typeof damage_type !== "string" || !damage_type.trim() || typeof description !== "string") {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): tipo ou descrição inválidos.`);
  }
  if (severity !== "minor" && severity !== "moderate" && severity !== "severe") {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): gravidade inválida.`);
  }
  if (typeof confidence !== "number" || !Number.isFinite(confidence) || confidence < 0 || confidence > 1) {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): confiança inválida.`);
  }
  if (origin !== "this_trip" && origin !== "between_trips") {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): origem inválida.`);
  }
  if (
    !Array.isArray(bbox) || bbox.length !== 4 ||
    bbox.some((coordinate) => typeof coordinate !== "number" || !Number.isFinite(coordinate) || coordinate < 0 || coordinate > 1)
  ) {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): caixa inválida.`);
  }
  const [ymin, xmin, ymax, xmax] = bbox as number[];
  if (ymax <= ymin || xmax <= xmin) {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): caixa inválida.`);
  }
  return { damage_type, description, severity, confidence, origin, bbox: [ymin, xmin, ymax, xmax] };
}

export function mapDamageAiResponse(
  value: unknown,
  angle: string,
  photoIndex: number,
  hasPrevious: boolean,
): ReportDamage[] {
  if (!isRecord(value)) throw new Error(`Resposta inválida do serviço de análise (${angle}).`);
  if (
    value.demo === true || value.demo_mode === true || value.is_demo === true ||
    value.mode === "demo" || value.status === "demo"
  ) {
    throw new Error(`O serviço de análise de danos respondeu em modo demo (${angle}).`);
  }
  if (value.angle !== angle || !Array.isArray(value.damages)) {
    throw new Error(`Resposta inválida do serviço de análise (${angle}): ângulo ou lista de danos inválidos.`);
  }

  return value.damages.map((rawDamage) => {
    const damage = validateDamage(rawDamage, angle);
    if (damage.origin === "between_trips" && !hasPrevious) {
      throw new Error(`Resposta inválida do serviço de análise (${angle}): dano entre viagens sem foto anterior.`);
    }
    return {
      angulo: angle,
      tipo_dano: mapDamageType(damage.damage_type),
      descricao: damage.description,
      gravidade: damage.severity === "minor" ? "Leve" : damage.severity === "moderate" ? "Moderado" : "Grave",
      confianca: damage.confidence,
      foto_index: photoIndex,
      // The report renderer consumes [ymin, xmin, ymax, xmax] in normalized coordinates.
      caixa: damage.bbox,
      origem: damage.origin === "between_trips" ? "entre_viagens" : "esta_viagem",
    };
  });
}

export function buildInspectionResult(damages: ReportDamage[]) {
  const thisTrip = damages.filter((damage) => damage.origem === "esta_viagem");
  const betweenTrips = damages.filter((damage) => damage.origem === "entre_viagens");
  const confirmed = thisTrip.filter((damage) => damage.confianca >= CONFIDENCE_THRESHOLD);
  const confirmedBetween = betweenTrips.filter((damage) => damage.confianca >= CONFIDENCE_THRESHOLD);

  const summary: string[] = [];
  if (confirmed.length > 0) {
    summary.push(
      `${confirmed.length} possível(is) dano(s) novo(s): ${
        confirmed.map((damage) => `${damage.tipo_dano} na ${damage.angulo}`).join("; ")
      }.`,
    );
  } else {
    summary.push("Nenhum dano novo com evidência clara.");
  }
  const uncertain = thisTrip.length - confirmed.length;
  if (uncertain > 0) summary.push(`${uncertain} indício(s) com baixa confiança (provável reflexo, sujeira ou ângulo).`);
  if (confirmedBetween.length > 0) {
    summary.push(
      `Atenção: ${confirmedBetween.length} dano(s) já estavam no carro quando este motorista o pegou (surgiram depois da viagem anterior): ${
        confirmedBetween.map((damage) => `${damage.tipo_dano} na ${damage.angulo}`).join("; ")
      }.`,
    );
  }

  return {
    houve_novo_dano: confirmed.length > 0,
    resumo_geral: summary.join(" "),
    danos_encontrados: damages.sort((a, b) => b.confianca - a.confianca),
    precisa_revisao: confirmedBetween.length > 0,
  };
}

export function damageAiEndpoint(baseUrl: string): string {
  const configured = baseUrl.trim().replace(/\/+$/, "");
  if (!configured) throw new Error("DAMAGE_AI_BASE_URL não configurada.");
  let url: URL;
  try {
    url = new URL(configured);
  } catch {
    throw new Error("DAMAGE_AI_BASE_URL inválida.");
  }
  if (url.protocol !== "http:" && url.protocol !== "https:") {
    throw new Error("DAMAGE_AI_BASE_URL deve usar HTTP ou HTTPS.");
  }
  if (url.protocol === "http:" && !isLocalHost(url.hostname)) {
    throw new Error("DAMAGE_AI_BASE_URL deve usar HTTPS fora de loopback ou rede local.");
  }
  return `${configured}/compare`;
}

function isLocalHost(hostname: string): boolean {
  const host = hostname.toLowerCase().replace(/^\[|\]$/g, "");
  if (host === "localhost" || host.endsWith(".localhost") || host.endsWith(".local") || host === "::1") return true;
  if (host.includes(":")) return host.startsWith("fc") || host.startsWith("fd") || host.startsWith("fe80:");

  const octets = host.split(".").map(Number);
  if (octets.length !== 4 || octets.some((octet) => !Number.isInteger(octet) || octet < 0 || octet > 255)) {
    return false;
  }
  const [first, second] = octets;
  return first === 10 ||
    first === 127 ||
    (first === 172 && second >= 16 && second <= 31) ||
    (first === 192 && second === 168) ||
    (first === 169 && second === 254);
}

export async function compareDamageAngle(
  baseUrl: string,
  apiToken: string | undefined,
  angle: string,
  initialImage: Uint8Array,
  finalImage: Uint8Array,
  previousImage: Uint8Array | undefined,
  fetcher: typeof fetch = fetch,
): Promise<unknown> {
  const endpoint = damageAiEndpoint(baseUrl);
  const form = new FormData();
  form.append("angle", angle);
  form.append("initial_image", new Blob([initialImage.slice().buffer as ArrayBuffer], { type: "image/jpeg" }), "initial.jpg");
  form.append("final_image", new Blob([finalImage.slice().buffer as ArrayBuffer], { type: "image/jpeg" }), "final.jpg");
  if (previousImage) {
    form.append("previous_image", new Blob([previousImage.slice().buffer as ArrayBuffer], { type: "image/jpeg" }), "previous.jpg");
  }

  const headers = new Headers();
  if (apiToken?.trim()) headers.set("Authorization", `Bearer ${apiToken.trim()}`);
  let response: Response;
  try {
    response = await fetcher(endpoint, {
      method: "POST",
      headers,
      body: form,
      signal: AbortSignal.timeout(DAMAGE_AI_TIMEOUT_MS),
    });
  } catch (error) {
    const reason = error instanceof Error ? error.message : String(error);
    throw new Error(`Falha ao chamar o serviço de análise (${endpoint}): ${reason}`);
  }
  if (!response.ok) {
    const body = (await response.text()).slice(0, 500);
    throw new Error(`Serviço de análise recusou a solicitação (${response.status}) em ${endpoint}: ${body}`);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error(`Resposta JSON inválida do serviço de análise em ${endpoint}.`);
  }
  return body;
}
