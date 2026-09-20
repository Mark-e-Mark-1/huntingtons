# Huntingtons

Native Android app for a Google Pixel. A warm, plain-language educational
companion about **Huntington’s disease** for patients, families, and caregivers.

The launcher name is **Huntington's**. The package is
`com.markemcallister.huntingtons`. The git repo and folder stay `huntingtons`.

This is **not** a clinical tool. It does not diagnose HD, interpret genetic
tests, recommend medicines or doses, or give personalized medical advice. For
emergencies, use local emergency services (in the United States, 911).

Education pages are fully readable offline from packaged JSON. The network is
used only when you tap **Refresh**. There are no accounts, ads, or analytics.

## What it does

- **Home:** title, one-liner, then three primary buttons in this order —
  What is Huntington’s Disease, Current Treatments, Latest Research and
  Cutting Edge Treatments — plus Resources & Support, Glossary, and
  About & Disclaimer. The footer shows **Content updated:** from the last
  successful refresh or the packaged baseline stamp.
- **What is Huntington’s Disease:** overview (HTT / CAG in plain language),
  genetics and inheritance, symptoms over time (descriptive, no self-score
  quiz), how common HD is, living with HD, and sources.
- **Current Treatments:** classes of approaches care teams commonly use —
  not a prescription and not a dose table. Includes multidisciplinary care,
  supportive therapies, genetic-counseling pointers, and standard-of-care vs
  experimental. Callout: “Medication choices are individual — ask your clinician.”
- **Latest Research:** how to read research, pipeline themes (gene-lowering /
  ASO, small molecules, biomarkers), dated cards, a clinical-trials primer,
  and an Explore link that opens ClinicalTrials.gov in your browser.
  “Ask your specialist about trials.”
- **Resources & Support:** curated official links (HDSA-style orgs, caregiver
  help, trial finders, genetic counseling). Links are mainly U.S.-focused.
- **Glossary:** A–Z searchable short definitions. Education pages link
  jargon into the glossary.
- **About & Disclaimer:** purpose, medical disclaimer, sourcing model,
  refresh behavior, and version.

A full disclaimer is required on first launch. The same disclaimer stays
reachable from the scale icon on major screens and from About. Treatments and
Research also show a short reminder on the page.

## Refresh behavior

Tap the refresh icon in the top app bar when you have a network connection.

- The app calls the public
  [ClinicalTrials.gov Data API v2](https://clinicaltrials.gov/data-api/api)
  (`GET /api/v2/studies`, condition `Huntington Disease`, newest
  `LastUpdatePostDate` first).
- Matching HD studies are saved on the device as dated cards (title, 2–4
  sentence summary taken from the public record, source, date, open-source
  link). Education pages, glossary, and resources are **not** replaced.
- The home footer and research page show the fetch time.
- If the call fails (offline, timeout, empty result), the last good snapshot
  stays on screen and a snackbar explains that refresh did not work.

A listing on ClinicalTrials.gov is a registration. It is **not** proof that
an approach works, is safe for you, or is available outside a trial. This app
never invents trial results or approvals.

Packaged baseline JSON lives at
[`app/src/main/assets/baseline.json`](app/src/main/assets/baseline.json)
(`contentVersion`, `generatedAt`). Ship it as plain `.json`, not `.gz` —
Android’s asset packer (`aapt2`) decompresses `*.gz` and strips the suffix,
which crashed Spelling Helper when the code opened the original `.gz` name.

## Install on a Pixel (sideload)

A debug APK is at [`artifacts/Huntingtons.apk`](artifacts/Huntingtons.apk).

1. On the Pixel open **Settings → About phone** and tap **Build number** seven
   times to enable developer options.
2. Open **Settings → System → Developer options** and turn on **USB debugging**.
   You can also enable **Install via USB** if it appears.
3. Connect the Pixel with a USB cable and accept the debugging prompt.
4. From a computer with [Android platform-tools](https://developer.android.com/tools/releases/platform-tools)
   installed:

```bash
adb devices
adb install -r artifacts/Huntingtons.apk
```

On Windows (PowerShell), after `git pull` in this repo:

```powershell
cd C:\Users\mcall\projects\huntingtons
adb install -r artifacts\Huntingtons.apk
```

If `adb` is not on your PATH, use the copy under the Android SDK, for example
`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.

`-r` reinstalls over a previous Huntingtons build (same package
`com.markemcallister.huntingtons`). You do not need to uninstall first.

Wireless debugging also works: pair in **Developer options → Wireless
debugging**, then `adb connect <pixel-ip>:<port>` and install the same APK.

Without adb: copy `artifacts/Huntingtons.apk` to the Pixel (Drive, USB file
transfer, or Messages) and open it. Android will offer to install or update.

Open **Huntington's**, accept the disclaimer, then use the three primary
buttons. Try Refresh when you have a signal; education pages still work with
the radio off.

## Build from source

Requires JDK 17+ and Android SDK platform 35.

```bash
export ANDROID_HOME=/path/to/android-sdk   # or set sdk.dir in local.properties
./gradlew assembleDebug testDebugUnitTest
```

On Windows, after `git pull`:

```powershell
cd C:\Users\mcall\projects\huntingtons
.\gradlew.bat assembleDebug
```

`assembleDebug` copies the APK to `artifacts/Huntingtons.apk` (and to
`/opt/cursor/artifacts/` when that directory exists).

## Tests

Unit tests parse the bundled JSON and check required sections, conservative
treatment wording (no dose tables), glossary coverage, HTTPS resources, and
that the asset is plain JSON, not gzip. They also parse a sample
ClinicalTrials.gov v2 payload and drop off-topic records.

## Medical disclaimer

Educational companion only. Not medical advice. Not for diagnosis, dosing, or
emergencies. See **About & Disclaimer** in the app.
