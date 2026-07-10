# Publishing checklist — Create FPS Boost 1.0.0

Everything below is prepped and ready. This is a copy-paste walkthrough for
https://modrinth.com/dashboard/projects → **Create a project**.

## 1. Basic info

| Field | Value |
|---|---|
| Project name | `Create FPS Boost` |
| Project type | `Mod` |
| Slug (URL) | `create-fps-boost` (auto-fills from name — leave it) |
| Summary (short, ~150 chars) | `Adaptive client-side FPS booster built for big Create modpacks. Culls distant renderers and particles only when your FPS actually needs it.` |
| Visibility | Draft while setting up → Public/Listed when ready |

## 2. Description

Open [MODRINTH_DESCRIPTION.md](MODRINTH_DESCRIPTION.md) and paste its **entire contents**
into Modrinth's description editor (it's already formatted for Modrinth's markdown —
headers, bold, emoji bullets, all supported as-is).

## 3. Icon

Upload [icon.png](icon.png) (1024×1024) as the project icon.

## 4. Categories

Select: **Optimization**. Optionally also **Utility** (for the diagnostics commands).

## 5. Environment

- Client side: **Required**
- Server side: **Unsupported**

(It's a client-only render mod — this combination is what tells Modrinth to show the
"client only" badge and stops it being suggested for server-only installs.)

## 6. Links (optional)

Leave Issues/Source/Wiki blank unless you create a GitHub repo for this project. If you
do, come back and fill those in later — they're editable any time after publishing.

## 7. License

- License: **LGPL-3.0-only** (search "GNU Lesser General Public License v3.0" in
  Modrinth's license picker — it's in their standard list)
- The full license text is already in [LICENSE](LICENSE) in this repo for reference.

## 8. Create the first version

Click **Create version** on the new project:

| Field | Value |
|---|---|
| Version number | `1.0.0` |
| Version title | `1.0.0` |
| Release channel | `Release` |
| Game versions | `1.21.1` |
| Loaders | `NeoForge` |
| Changelog | Paste the `## 1.0.0` section from [CHANGELOG.md](CHANGELOG.md) |
| File | [build/libs/createfpsboost-1.0.0.jar](build/libs/createfpsboost-1.0.0.jar) |

Modrinth will auto-detect the mod metadata from the jar (name, version, dependencies) —
double check it matches the table above, then hit **Publish**.

## 9. After publishing

- The project starts in review for Modrinth's mod review queue (usually resolves within
  a day). It stays visible to you in the meantime.
- Future releases: bump `mod_version` in [gradle.properties](gradle.properties), add a new
  section to `CHANGELOG.md`, run `./gradlew build`, and upload the new jar as a new version
  under the same project — no need to repeat steps 1–7.

## If you'd rather I drive the browser

I can also fill out this form live in Chrome via computer-use while you stay logged in
and click the final **Publish** button yourself — just say the word and make sure you're
logged into Modrinth in your default browser first.
