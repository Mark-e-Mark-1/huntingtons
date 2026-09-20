import type { ResearchItem } from "./types";

const BASE_URL = "https://clinicaltrials.gov/api/v2/studies";
const PAGE_SIZE = 25;
const MAX_ITEMS = 12;

const FIELDS = [
  "NCTId",
  "BriefTitle",
  "OfficialTitle",
  "OverallStatus",
  "Phase",
  "StartDate",
  "LastUpdatePostDate",
  "BriefSummary",
  "LeadSponsorName",
  "InterventionName",
  "Condition",
].join(",");

interface StudyJson {
  protocolSection?: {
    identificationModule?: { nctId?: string; briefTitle?: string };
    statusModule?: {
      overallStatus?: string;
      lastUpdatePostDateStruct?: { date?: string };
    };
    descriptionModule?: { briefSummary?: string };
    designModule?: { phases?: string[] };
    sponsorCollaboratorsModule?: { leadSponsor?: { name?: string } };
    conditionsModule?: { conditions?: string[] };
    armsInterventionsModule?: { interventions?: Array<{ name?: string }> };
  };
}

function humanStatus(raw: string): string {
  switch (raw.toUpperCase()) {
    case "RECRUITING":
      return "Recruiting";
    case "NOT_YET_RECRUITING":
      return "Not yet recruiting";
    case "ENROLLING_BY_INVITATION":
      return "Enrolling by invitation";
    case "ACTIVE_NOT_RECRUITING":
      return "Active, not recruiting";
    case "COMPLETED":
      return "Completed";
    default:
      return raw.replace(/_/g, " ").toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }
}

function humanPhase(raw: string): string {
  switch (raw.toUpperCase()) {
    case "NA":
      return "Not applicable";
    case "EARLY_PHASE1":
      return "Early Phase 1";
    case "PHASE1":
      return "Phase 1";
    case "PHASE2":
      return "Phase 2";
    case "PHASE3":
      return "Phase 3";
    case "PHASE4":
      return "Phase 4";
    default:
      return raw.replace(/_/g, " ");
  }
}

function splitSentences(text: string): string[] {
  const cleaned = text.replace(/\n/g, " ").replace(/\s+/g, " ").trim();
  if (!cleaned) return [];
  const pieces = cleaned.split(/(?<=[.!?])\s+/);
  return pieces.length ? pieces : [cleaned.slice(0, 400)];
}

function isOffTopic(title: string, conditions: string[]): boolean {
  const t = title.toLowerCase();
  if (t.includes("chatbot") || t.includes("llm-based")) return true;
  if (t.includes("scattered rare disease")) return true;
  if (t.includes("criminal")) return true;
  if (t.includes("non-invasive prenatal") && !t.includes("huntington")) return true;
  const haystack = [...conditions, title].join(" ").toLowerCase();
  return !haystack.includes("huntington");
}

export function parseStudies(payload: { studies?: StudyJson[] }): ResearchItem[] {
  const items: ResearchItem[] = [];
  for (const study of payload.studies ?? []) {
    const protocol = study.protocolSection;
    if (!protocol) continue;
    const ident = protocol.identificationModule ?? {};
    const nctId = ident.nctId?.trim() ?? "";
    const title = ident.briefTitle?.trim() ?? "";
    if (!nctId || !title) continue;
    const conditions = protocol.conditionsModule?.conditions ?? [];
    if (isOffTopic(title, conditions)) continue;

    const status = protocol.statusModule ?? {};
    const date =
      status.lastUpdatePostDateStruct?.date?.trim() ||
      new Date().toISOString().slice(0, 10);
    const overallStatus = humanStatus(status.overallStatus?.trim() ?? "");
    const phases = (protocol.designModule?.phases ?? []).map(humanPhase).join(", ");
    const sponsor = protocol.sponsorCollaboratorsModule?.leadSponsor?.name?.trim() ?? "";
    const interventions = (protocol.armsInterventionsModule?.interventions ?? [])
      .map((item) => item.name?.trim() ?? "")
      .filter((name) => name && !name.toLowerCase().includes("placebo"));
    const brief = protocol.descriptionModule?.briefSummary?.trim() ?? "";
    const sentences = splitSentences(brief).filter(Boolean).slice(0, 3);
    let meta = `ClinicalTrials.gov lists this study as ${overallStatus || "a registered record"}`;
    if (phases) meta += ` (${phases})`;
    meta += ".";
    if (sponsor) meta += ` Lead sponsor: ${sponsor}.`;
    if (interventions.length) {
      meta += ` Interventions named in the record: ${interventions.slice(0, 4).join(", ")}.`;
    }
    sentences.push(meta);
    sentences.push(
      "A listing here is a public registration, not proof that the approach works, is safe for you, or is available outside a trial.",
    );
    items.push({
      id: nctId,
      title,
      summary: sentences.join(" ").trim(),
      source: "ClinicalTrials.gov",
      sourceUrl: `https://clinicaltrials.gov/study/${nctId}`,
      date,
      kind: "clinical_trial",
    });
  }
  return items.sort((a, b) => b.date.localeCompare(a.date)).slice(0, MAX_ITEMS);
}

export async function fetchTrialItems(): Promise<ResearchItem[]> {
  const params = new URLSearchParams({
    "query.cond": "Huntington Disease",
    "filter.overallStatus":
      "RECRUITING,NOT_YET_RECRUITING,ENROLLING_BY_INVITATION,ACTIVE_NOT_RECRUITING",
    sort: "LastUpdatePostDate:desc",
    pageSize: String(PAGE_SIZE),
    countTotal: "true",
    fields: FIELDS,
  });
  // Simple GET only — no custom headers — so the browser stays CORS-safelisted.
  // ClinicalTrials.gov API v2 sends Access-Control-Allow-Origin: *.
  const response = await fetch(`${BASE_URL}?${params.toString()}`);
  if (!response.ok) {
    throw new Error(`ClinicalTrials.gov returned HTTP ${response.status}`);
  }
  const body = (await response.json()) as { studies?: StudyJson[] };
  const items = parseStudies(body);
  if (!items.length) {
    throw new Error("ClinicalTrials.gov returned no HD studies");
  }
  return items;
}

export function nowIso(): string {
  return new Date().toISOString().replace(/\.\d{3}Z$/, "Z");
}
