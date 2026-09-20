import { applyTrialRefresh, getCatalog } from "./catalog";
import { hrefFor, parseRoute, titleFor } from "./router";
import { saveResearchCache } from "./storage";
import { fetchTrialItems, nowIso } from "./trials";
import type {
  Catalog,
  EducationPage,
  EducationSection,
  ResearchItem,
  ViewId,
} from "./types";

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function formatDate(raw: string): string {
  const iso = raw.length > 10 ? raw : `${raw}T00:00:00Z`;
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return raw;
  return new Intl.DateTimeFormat("en-US", { dateStyle: "medium" }).format(date);
}

function nl2p(text: string): string {
  return text
    .split(/\n{2,}/)
    .map((block) => `<p>${escapeHtml(block).replace(/\n/g, "<br />")}</p>`)
    .join("");
}

function showToast(message: string): void {
  const toast = document.getElementById("toast");
  if (!toast) return;
  toast.textContent = message;
  toast.hidden = false;
  window.setTimeout(() => {
    toast.hidden = true;
  }, 5200);
}

function glossaryChips(terms: string[]): string {
  if (!terms.length) return "";
  return `<div class="chips">${terms
    .slice(0, 4)
    .map(
      (term) =>
        `<a class="chip" href="${hrefFor("glossary", term)}">Glossary: ${escapeHtml(term)}</a>`,
    )
    .join("")}</div>`;
}

function sectionCard(section: EducationSection): string {
  return `<article class="card">
    <h2>${escapeHtml(section.title)}</h2>
    <p>${escapeHtml(section.body)}</p>
    ${glossaryChips(section.glossaryTerms ?? [])}
  </article>`;
}

function sourceList(sources: EducationPage["sources"]): string {
  if (!sources.length) return "";
  return `<section class="stack">
    <h2>Sources</h2>
    ${sources
      .map(
        (source) =>
          `<a class="text-link" href="${escapeHtml(source.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(source.label)}</a>`,
      )
      .join("")}
  </section>`;
}

function callout(text: string): string {
  if (!text) return "";
  return `<aside class="callout" role="note">${escapeHtml(text)}</aside>`;
}

function researchCard(item: ResearchItem): string {
  return `<article class="card">
    <h3>${escapeHtml(item.title)}</h3>
    <p class="meta">${escapeHtml(formatDate(item.date))}</p>
    <p>${escapeHtml(item.summary)}</p>
    <p class="muted">Source: ${escapeHtml(item.source)}</p>
    <a class="text-link" href="${escapeHtml(item.sourceUrl)}" target="_blank" rel="noopener noreferrer">Open source</a>
  </article>`;
}

function topBar(view: ViewId, refreshing: boolean): string {
  const showBack = view !== "home" && view !== "disclaimer";
  const back = showBack
    ? `<a class="icon-btn" href="${hrefFor("home")}" aria-label="Back">←</a>`
    : "";
  return `<header class="topbar">
    <div class="topbar-inner${showBack ? "" : " no-back"}">
      ${back}
      <h1 class="brand"><a href="${hrefFor("home")}">${escapeHtml(titleFor(view === "home" ? "home" : view))}</a></h1>
      <div class="actions">
        <a class="icon-btn" href="${hrefFor("disclaimer")}" aria-label="Disclaimer">⚖</a>
        <button class="icon-btn" type="button" data-refresh ${refreshing ? "disabled" : ""} aria-label="Refresh latest research">↻</button>
      </div>
    </div>
  </header>`;
}

function home(catalog: Catalog): string {
  return `<main id="main" class="page">
    <h2 class="display">Huntington's Disease</h2>
    <p class="lede">A plain-language companion for patients, families, and caregivers. Educational only — not a clinical tool.</p>
    <a class="btn primary" href="${hrefFor("overview")}">What is Huntington’s Disease</a>
    <a class="btn primary" href="${hrefFor("treatments")}">Current Treatments</a>
    <a class="btn primary" href="${hrefFor("research")}">Latest Research and Cutting Edge Treatments</a>
    <a class="btn outline" href="${hrefFor("resources")}">Resources &amp; Support</a>
    <a class="btn outline" href="${hrefFor("glossary")}">Glossary</a>
    <a class="btn outline" href="${hrefFor("about")}">About &amp; Disclaimer</a>
    <p class="footer-stamp">Content updated: ${escapeHtml(formatDate(catalog.generatedAt))}</p>
  </main>`;
}

function education(page: EducationPage, lastReviewed: string, disclaimer?: string): string {
  return `<main id="main" class="page">
    ${disclaimer ? callout(disclaimer) : ""}
    ${page.callout ? callout(page.callout) : ""}
    ${page.intro ? `<p class="lede">${escapeHtml(page.intro)}</p>` : ""}
    ${page.sections.map(sectionCard).join("")}
    ${sourceList(page.sources)}
    <p class="muted">Education last reviewed: ${escapeHtml(formatDate(lastReviewed))}</p>
    <a class="text-link" href="${hrefFor("glossary")}">Open Glossary</a>
  </main>`;
}

function research(catalog: Catalog): string {
  const page = catalog.research;
  const cards = [...page.notableDevelopments, ...page.trialItems].sort((a, b) =>
    b.date.localeCompare(a.date),
  );
  return `<main id="main" class="page">
    ${callout(catalog.disclaimer.shortText)}
    <h2>How to read this page</h2>
    <p>${escapeHtml(page.howToRead)}</p>
    <h2>Pipeline themes</h2>
    ${page.pipelineThemes.map(sectionCard).join("")}
    <h2>Clinical trials primer</h2>
    <p>${escapeHtml(page.clinicalTrialsPrimer)}</p>
    <a class="btn primary" href="${escapeHtml(page.exploreTrialsUrl)}" target="_blank" rel="noopener noreferrer">${escapeHtml(page.exploreTrialsLabel)}</a>
    ${callout(page.askSpecialist)}
    <h2>Dated records (newest first)</h2>
    <p class="muted">Fetched or packaged: ${escapeHtml(formatDate(catalog.generatedAt))}</p>
    ${cards.map(researchCard).join("")}
  </main>`;
}

function resources(catalog: Catalog): string {
  return `<main id="main" class="page">
    <p class="lede">${escapeHtml(catalog.resources.intro)}</p>
    ${catalog.resources.items
      .map(
        (item) => `<article class="card">
        ${item.category ? `<p class="meta">${escapeHtml(item.category)}</p>` : ""}
        <h2>${escapeHtml(item.title)}</h2>
        <p>${escapeHtml(item.description)}</p>
        <a class="text-link" href="${escapeHtml(item.url)}" target="_blank" rel="noopener noreferrer">Open in browser</a>
      </article>`,
      )
      .join("")}
  </main>`;
}

function glossary(catalog: Catalog, query: string): string {
  const q = query.trim().toLowerCase();
  const terms = catalog.glossary.filter(
    (term) =>
      !q ||
      term.term.toLowerCase().includes(q) ||
      term.definition.toLowerCase().includes(q),
  );
  return `<main id="main" class="page">
    <label class="search-label" for="glossary-q">Search A–Z terms</label>
    <input id="glossary-q" class="search" type="search" value="${escapeHtml(query)}" placeholder="Search A–Z terms" />
    <p class="muted">Short definitions only. These are teaching notes, not a diagnosis.</p>
    ${terms
      .map(
        (term) => `<article class="card">
        <h2>${escapeHtml(term.term)}</h2>
        <p>${escapeHtml(term.definition)}</p>
      </article>`,
      )
      .join("")}
  </main>`;
}

function about(catalog: Catalog): string {
  return `<main id="main" class="page">
    <h2>Purpose</h2>
    <p>${escapeHtml(catalog.about.purpose)}</p>
    <h2>Medical disclaimer</h2>
    ${nl2p(catalog.disclaimer.fullText)}
    <a class="text-link" href="${hrefFor("disclaimer")}">Read the full disclaimer screen</a>
    <h2>How this app is sourced</h2>
    <p>${escapeHtml(catalog.about.sourcing)}</p>
    <h2>Refresh behavior</h2>
    <p>${escapeHtml(catalog.about.refresh)}</p>
    <h2>Version</h2>
    <p>Web and Android share content package ${escapeHtml(catalog.about.version)}. Content updated: ${escapeHtml(formatDate(catalog.generatedAt))}.</p>
  </main>`;
}

function disclaimer(catalog: Catalog, accepted: boolean): string {
  return `<main id="main" class="page">
    <h2>Please read this before using the app</h2>
    ${nl2p(catalog.disclaimer.fullText)}
    <button class="btn primary" type="button" data-accept>${accepted ? "I understand — back to Home" : "I understand — continue"}</button>
  </main>`;
}

export function renderApp(root: HTMLElement, refreshing = false): void {
  const catalog = getCatalog();
  const route = parseRoute();
  const body = (() => {
    switch (route.view) {
      case "overview":
        return education(catalog.overview, catalog.educationLastReviewed);
      case "treatments":
        return education(catalog.treatments, catalog.educationLastReviewed, catalog.disclaimer.shortText);
      case "research":
        return research(catalog);
      case "resources":
        return resources(catalog);
      case "glossary":
        return glossary(catalog, route.query);
      case "about":
        return about(catalog);
      case "disclaimer":
        return disclaimer(catalog, true);
      default:
        return home(catalog);
    }
  })();
  root.innerHTML = `${topBar(route.view, refreshing)}${body}`;
  document.title =
    route.view === "home" ? "Huntington's Disease" : `${titleFor(route.view)} · Huntington's`;

  const search = root.querySelector<HTMLInputElement>("#glossary-q");
  if (search) {
    search.addEventListener("input", () => {
      const next = search.value;
      const href = hrefFor("glossary", next);
      if (location.hash !== href && location.hash !== href.slice(1)) {
        history.replaceState(null, "", href);
      }
      const page = root.querySelector("main");
      if (!page) return;
      const catalogNow = getCatalog();
      const q = next.trim().toLowerCase();
      const terms = catalogNow.glossary.filter(
        (term) =>
          !q ||
          term.term.toLowerCase().includes(q) ||
          term.definition.toLowerCase().includes(q),
      );
      const cards = page.querySelectorAll("article.card");
      cards.forEach((card) => card.remove());
      page.insertAdjacentHTML("beforeend", terms.map((term) => `<article class="card">
        <h2>${escapeHtml(term.term)}</h2>
        <p>${escapeHtml(term.definition)}</p>
      </article>`).join(""));
    });
  }
}

export async function refreshResearch(root: HTMLElement): Promise<void> {
  renderApp(root, true);
  try {
    const items = await fetchTrialItems();
    const fetchedAt = nowIso();
    saveResearchCache(fetchedAt, items);
    applyTrialRefresh(fetchedAt, items);
    renderApp(root, false);
    showToast("Latest research updated from ClinicalTrials.gov.");
  } catch (error) {
    renderApp(root, false);
    const detail = error instanceof Error ? error.message : "Refresh failed";
    showToast(`Could not refresh. Showing last saved content. (${detail})`);
  }
}
