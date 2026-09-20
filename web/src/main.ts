import { loadCatalog } from "./catalog";
import { navigate, onRouteChange, parseRoute } from "./router";
import { refreshResearch, renderApp } from "./render";
import { acceptDisclaimer, hasAcceptedDisclaimer } from "./storage";
import "./styles.css";

const found = document.getElementById("app");
if (!(found instanceof HTMLElement)) throw new Error("#app missing");
const root: HTMLElement = found;

function showGateOrPage(): void {
  if (!hasAcceptedDisclaimer() && parseRoute().view !== "disclaimer") {
    navigate("disclaimer");
    return;
  }
  renderApp(root);
}

loadCatalog()
  .then(() => {
    showGateOrPage();
    onRouteChange(showGateOrPage);
    document.addEventListener("click", (event) => {
      const target = (event.target as HTMLElement | null)?.closest?.("[data-refresh], [data-accept]");
      if (!(target instanceof HTMLElement)) return;
      if (target.hasAttribute("data-refresh")) {
        event.preventDefault();
        void refreshResearch(root);
      }
      if (target.hasAttribute("data-accept")) {
        event.preventDefault();
        acceptDisclaimer();
        navigate("home");
      }
    });
  })
  .catch((error: unknown) => {
    root.innerHTML = `<main class="page"><h1>Couldn’t load Huntington’s guide</h1><p>${
      error instanceof Error ? error.message : "Unknown error"
    }</p></main>`;
  });

if (import.meta.env.PROD && "serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("./sw.js").catch(() => undefined);
  });
}
