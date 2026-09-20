import { loadResearchCache } from "./storage";
import type { Catalog } from "./types";

let catalog: Catalog | null = null;

export async function loadCatalog(): Promise<Catalog> {
  const response = await fetch("./baseline.json", { cache: "no-cache" });
  if (!response.ok) {
    throw new Error(`Could not load educational content (HTTP ${response.status})`);
  }
  const data = (await response.json()) as Catalog;
  if (!data.overview?.sections?.length || !data.treatments?.sections?.length) {
    throw new Error("Educational content is missing required sections");
  }
  const cached = loadResearchCache();
  if (cached) {
    data.generatedAt = cached.fetchedAt;
    data.research = { ...data.research, trialItems: cached.trialItems };
  }
  catalog = data;
  return data;
}

export function getCatalog(): Catalog {
  if (!catalog) throw new Error("Catalog is not loaded");
  return catalog;
}

export function applyTrialRefresh(fetchedAt: string, trialItems: Catalog["research"]["trialItems"]): Catalog {
  const current = getCatalog();
  current.generatedAt = fetchedAt;
  current.research = { ...current.research, trialItems };
  catalog = current;
  return current;
}
