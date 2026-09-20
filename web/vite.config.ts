import { copyFileSync, mkdirSync } from "node:fs";
import { resolve } from "node:path";
import { defineConfig } from "vite";

function syncBaseline(): void {
  const src = resolve(__dirname, "../content/baseline.json");
  const destDir = resolve(__dirname, "public");
  mkdirSync(destDir, { recursive: true });
  copyFileSync(src, resolve(destDir, "baseline.json"));
}

export default defineConfig({
  // Project Pages URL is https://mark-e-mark-1.github.io/huntingtons/
  // Local preview keeps relative "./" so Vite's LAN URL still works.
  base: process.env.GITHUB_PAGES === "1" ? "/huntingtons/" : "./",
  publicDir: "public",
  plugins: [
    {
      name: "sync-baseline-json",
      buildStart() {
        syncBaseline();
      },
      configureServer() {
        syncBaseline();
      },
    },
  ],
  server: {
    host: true,
    port: 5173,
  },
  preview: {
    host: true,
    port: 4173,
  },
});
