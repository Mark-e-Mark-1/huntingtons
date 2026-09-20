import type { ResearchItem } from "./types";

const DISCLAIMER_KEY = "huntingtons-disclaimer-accepted";
const CACHE_KEY = "huntingtons-research-cache";

export function hasAcceptedDisclaimer(): boolean {
  return localStorage.getItem(DISCLAIMER_KEY) === "1";
}

export function acceptDisclaimer(): void {
  localStorage.setItem(DISCLAIMER_KEY, "1");
}

export function loadResearchCache(): { fetchedAt: string; trialItems: ResearchItem[] } | null {
  const raw = localStorage.getItem(CACHE_KEY);
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as { fetchedAt?: string; trialItems?: ResearchItem[] };
    if (!parsed.fetchedAt || !Array.isArray(parsed.trialItems) || !parsed.trialItems.length) {
      return null;
    }
    return { fetchedAt: parsed.fetchedAt, trialItems: parsed.trialItems };
  } catch {
    return null;
  }
}

export function saveResearchCache(fetchedAt: string, trialItems: ResearchItem[]): void {
  localStorage.setItem(CACHE_KEY, JSON.stringify({ fetchedAt, trialItems, source: "ClinicalTrials.gov API v2" }));
}
