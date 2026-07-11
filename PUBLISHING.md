# Publishing checklist — Create FPS Boost 1.1.0

> ⚠️ **Do not submit yet.** The original 1.0.0 submission was rejected by a Modrinth
> moderator with an explicit "do not resubmit this project." Before using any of the
> steps below, send the message in [MODRINTH_APPEAL.md](MODRINTH_APPEAL.md) through
> Modrinth's support channel and wait for a response. Only proceed if they say a
> corrected resubmission is acceptable. Submitting anyway, or re-submitting under a
> different project name/slug to route around the rejection, risks your account —
> not just this listing.

Everything below is prepped and ready for **if and when** you get a green light.
This is a copy-paste walkthrough for https://modrinth.com/dashboard/projects →
**Create a project**.

## 1. Basic info

| Field | Value |
|---|---|
| Project name | `Create FPS Boost` |
| Project type | `Mod` |
| Slug (URL) | `create-fps-boost` (auto-fills from name — leave it) |
| Summary (short, ~150 chars) | `Adaptive client-side helper for big Create modpacks. Tightens Minecraft's own existing culling further, only once your FPS actually needs it.` |
| Visibility | Draft while setting up → Public/Listed when ready |

## 2. Description

Open [MODRINTH_DESCRIPTION.md](MODRINTH_DESCRIPTION.md) and paste its **entire contents**
into Modrinth's description editor (it's already formatted for Modrinth's markdown —
headers, bold, bullets, all supported as-is). This version was rewritten after the
rejection to be precise about what's genuinely new vs. what automates or tightens
existing vanilla behavior — read it over once yourself before submitting.

## 3. Icon

Upload [icon.png](icon.png) (1024×1024) as the project icon.

## 4. Categories

Select: **Optimization**. Optionally also **Utility** (for the diagnostics commands).

## 5. Environment

- Client side: **Required**
- Server side: **Unsupported**

(It's a client-only render mod — this combination is what tells Modrinth to show the
"client only" badge and stops it being suggested for server-only installs.)

## 6. Links

- Source: `https://github.com/levisnakes/create-fps-boost`
- Issues: `https://github.com/levisnakes/create-fps-boost/issues`
- Wiki: leave blank

## 7. License

- License: **LGPL-3.0-only** (search "GNU Lesser General Public License v3.0" in
  Modrinth's license picker — it's in their standard list)
- The full license text is already in [LICENSE](LICENSE) in this repo for reference.

## 8. Create the first version

Click **Create version** on the new project:

| Field | Value |
|---|---|
| Version number | `1.1.0` |
| Version title | `1.1.0` |
| Release channel | `Release` |
| Game versions | `1.21.1` |
| Loaders | `NeoForge` |
| Changelog | Paste the `## 1.1.0` section from [CHANGELOG.md](CHANGELOG.md) |
| File | [build/libs/createfpsboost-1.1.0.jar](build/libs/createfpsboost-1.1.0.jar) |

Modrinth will auto-detect the mod metadata from the jar (name, version, dependencies) —
double check it matches the table above, then hit **Publish**.

## 9. After publishing

- The project starts in review for Modrinth's mod review queue. It stays visible to you
  in the meantime.
- Future releases: bump `mod_version` in [gradle.properties](gradle.properties), add a new
  section to `CHANGELOG.md`, run `./gradlew build`, and upload the new jar as a new version
  under the same project — no need to repeat steps 1–7.

## If you'd rather I drive the browser

I can also fill out this form live in Chrome via computer-use while you stay logged in
and click the final **Publish** button yourself — just say the word and make sure you're
logged into Modrinth in your default browser first. (Still subject to the gate above.)
