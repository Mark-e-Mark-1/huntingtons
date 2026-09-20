import type { Route, ViewId } from "./types";

const VIEWS = new Set<ViewId>([
  "home",
  "overview",
  "treatments",
  "research",
  "resources",
  "glossary",
  "about",
  "disclaimer",
]);

export function parseRoute(): Route {
  const hash = location.hash.replace(/^#/, "");
  const [rawPath, rawQuery] = hash.split("?");
  const params = new URLSearchParams(rawQuery || "");
  const parts = (rawPath || "").split("/").filter(Boolean);
  const first = (parts[0] || "home") as ViewId;
  const view = VIEWS.has(first) ? first : "home";
  return { view, query: params.get("q") ?? "" };
}

export function hrefFor(view: ViewId, query = ""): string {
  if (view === "home") return "#/";
  const q = query.trim();
  return q ? `#/${view}?q=${encodeURIComponent(q)}` : `#/${view}`;
}

export function navigate(view: ViewId, query = ""): void {
  const href = hrefFor(view, query);
  if (location.hash === href || location.hash === href.slice(1)) {
    window.dispatchEvent(new HashChangeEvent("hashchange"));
    return;
  }
  location.hash = href.slice(1);
}

export function onRouteChange(fn: () => void): void {
  window.addEventListener("hashchange", fn);
}

export function titleFor(view: ViewId): string {
  switch (view) {
    case "overview":
      return "What is Huntington’s Disease";
    case "treatments":
      return "Current Treatments";
    case "research":
      return "Latest Research";
    case "resources":
      return "Resources & Support";
    case "glossary":
      return "Glossary";
    case "about":
      return "About & Disclaimer";
    case "disclaimer":
      return "Disclaimer";
    default:
      return "Huntington's Disease";
  }
}
