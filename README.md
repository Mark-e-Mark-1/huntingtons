# Huntingtons

Educational companion about **Huntington’s disease** for patients, families,
and caregivers. Warm, plain U.S. English. **Not** a clinical tool: it does
not diagnose HD, interpret genetic tests, recommend medicines or doses, or
give personalized medical advice. For emergencies, use local emergency
services (in the United States, 911).

Two ways to use the same content:

| Surface | Who it’s for |
| --- | --- |
| **Web / PWA** | Anyone with a browser — also at GitHub Pages |
| **Android APK** | Mark’s Google Pixel (sideload) |

Package: `com.markemcallister.huntingtons`. Repo folder stays `huntingtons`.
No accounts, ads, or analytics.

Education pages are packaged JSON and stay readable offline. The network is
used only when you tap **Refresh**.

## Open in a browser (public)

After GitHub Pages is enabled (one-time, below), the public URL is:

**https://mark-e-mark-1.github.io/huntingtons/**

On a phone, use the browser menu **Add to Home Screen** for the PWA.

## What it does

Same information architecture on web and Android:

- **Home:** title, one-liner, then three primary buttons in this order —
  What is Huntington’s Disease, Current Treatments, Latest Research and
  Cutting Edge Treatments — plus Resources & Support, Glossary, and
  About & Disclaimer. The footer shows **Content updated:** from the last
  successful refresh or the packaged baseline stamp.
- **What is Huntington’s Disease:** overview (HTT / CAG in plain language),
  genetics and inheritance, symptoms over time (descriptive, no self-score
  quiz), how common HD is, living with HD, and sources.
- **Current Treatments:** classes of approaches care teams commonly use —
  not a prescription and not a dose table. Callout: “Medication choices are
  individual — ask your clinician.”
- **Latest Research:** how to read research, pipeline themes, dated cards,
  a ClinicalTrials.gov explore link, and “Ask your specialist about trials.”
- **Resources & Support:** curated official links (mainly U.S.).
- **Glossary:** A–Z searchable short definitions.
- **About & Disclaimer:** purpose, medical disclaimer, sourcing, refresh,
  version.

A full disclaimer is required on first launch. It stays reachable from the
scale control and from About. Treatments and Research also show a short
reminder on the page.

## Shared content (do not fork copies)

Canonical JSON is [`content/baseline.json`](content/baseline.json)
(`contentVersion`, `generatedAt`). Ship it as plain `.json`, not `.gz` —
Android’s asset packer (`aapt2`) decompresses `*.gz` and strips the suffix,
which crashed Spelling Helper when the code opened the original `.gz` name.

- **Android** packages that folder as an asset (`assets.srcDir`).
- **Web** copies it into the Vite build as `baseline.json` on `dev` / `build`.

Edit `content/baseline.json` once; both surfaces pick it up on the next build.

## Refresh behavior

Tap **Refresh** in the top bar when you have a network connection.

- Calls the public
  [ClinicalTrials.gov Data API v2](https://clinicaltrials.gov/data-api/api)
  (`GET /api/v2/studies`, condition `Huntington Disease`). The API sends
  `Access-Control-Allow-Origin: *`, so the browser can call it directly
  (simple GET, no custom headers).
- Matching HD studies are saved locally (Android file cache, web
  `localStorage`) as dated cards. Education pages are **not** replaced.
- Failure keeps the last good snapshot and shows a short message.

A listing on ClinicalTrials.gov is a registration, not proof that an
approach works or is available outside a trial.

## Run the web app locally

Needs [Node.js LTS](https://nodejs.org) on PATH.

### Windows (double-click)

**Double-click `Start-Huntingtons-Web.bat`** in
`C:\Users\mcall\projects\huntingtons`.

The launcher `cd`s into `web\`, uses `npm.cmd` (so PowerShell execution
policy does not matter), installs `node_modules` if needed, builds, serves
`web/dist` at `http://127.0.0.1:4173/`, and opens your default browser.
Leave the black window open; close it to stop the server.

PowerShell-friendly:

```powershell
cd C:\Users\mcall\projects\huntingtons
.\Start-Huntingtons-Web.bat
```

### Manual

```bash
cd web
npm install
npm run dev
```

Then open the URL Vite prints (usually `http://localhost:5173`). For a phone
on the same Wi-Fi, use the **Network** URL.

```bash
cd web
npm run build
npm run preview
```

`web/dist/` is a static site. The PWA service worker caches the shell plus
`baseline.json` so education pages stay readable offline after the first visit.

## Publish on GitHub Pages (one-time dashboard click)

The workflow [`.github/workflows/pages.yml`](.github/workflows/pages.yml)
builds `web/` with `base: /huntingtons/` and deploys `web/dist` using the
official GitHub Pages actions. It runs on push to `main` and on
**workflow_dispatch**.

**One-time setup** (repo owner, in the GitHub website):

1. Open **https://github.com/Mark-e-Mark-1/huntingtons/settings/pages**
2. Under **Build and deployment → Source**, choose **GitHub Actions**
   (not “Deploy from a branch”).
3. If this branch is not `main` yet, either merge the PR or open the
   **Actions** tab → **Deploy GitHub Pages** → **Run workflow**.
4. Wait for the workflow to finish. The site is:

   **https://mark-e-mark-1.github.io/huntingtons/**

If Pages is still off, that URL will 404 until step 2 is done. Routes are
hash-based (`#/research`, `#/glossary?q=HTT`) so they work on project Pages
without a server rewrite.

## Install the Android app on a Pixel (sideload)

The Pixel APK is unchanged. A debug APK is at
[`artifacts/Huntingtons.apk`](artifacts/Huntingtons.apk).

1. On the Pixel open **Settings → About phone** and tap **Build number** seven
   times to enable developer options.
2. Open **Settings → System → Developer options** and turn on **USB debugging**.
3. Connect the Pixel and run:

```bash
adb devices
adb install -r artifacts/Huntingtons.apk
```

On Windows (PowerShell), after `git pull`:

```powershell
cd C:\Users\mcall\projects\huntingtons
adb install -r artifacts\Huntingtons.apk
```

If `adb` is not on your PATH, use the copy under the Android SDK, for example
`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.

`-r` reinstalls over a previous Huntingtons build (same package
`com.markemcallister.huntingtons`). You do not need to uninstall first.

Without adb: copy `artifacts/Huntingtons.apk` to the Pixel (Drive, USB file
transfer, or Messages) and open it.

## Build the Android app from source

Requires JDK 17+ and Android SDK platform 35.

```bash
export ANDROID_HOME=/path/to/android-sdk   # or set sdk.dir in local.properties
./gradlew assembleDebug testDebugUnitTest
```

On Windows:

```powershell
cd C:\Users\mcall\projects\huntingtons
.\gradlew.bat assembleDebug
```

`assembleDebug` copies the APK to `artifacts/Huntingtons.apk` (and to
`/opt/cursor/artifacts/` when that directory exists).

## Tests

Android unit tests parse the shared JSON and check required sections,
conservative treatment wording (no dose tables), glossary coverage, HTTPS
resources, and that the file is plain JSON, not gzip. They also parse a
sample ClinicalTrials.gov v2 payload and drop off-topic records.

## Medical disclaimer

Educational companion only. Not medical advice. Not for diagnosis, dosing, or
emergencies. See **About & Disclaimer** in the app.
