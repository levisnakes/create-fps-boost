### Create FPS Boost

An adaptive, client-side performance mod built for large **Create**-based modpacks — no server component, safe on any server, works in any pack (Create is not a hard dependency).

Big Create packs already ship Sodium, Lithium, ModernFix, FerriteCore, ImmediatelyFast, EntityCulling, C2ME and Krypton — the generic optimizations are taken. What's left is pack-specific: hundreds of block-entity renderers (gauges, displays, funnels, stock tickers...), item/entity floods from belts and factories, and particle storms from steam and weather. Create FPS Boost targets exactly that, and does it **adaptively** — nothing changes at healthy FPS.

#### How it works

A single adaptive quality engine measures your real FPS every second. **At or above your target FPS (default 45), everything renders 100% vanilla.** Only when the game actually struggles does it scale down, and it recovers automatically the moment FPS comes back. A world-join grace period stops loading stutter from ever triggering it.

- 🎛️ **Block-entity distance culling** — scales each renderer's *own* vanilla view distance, so beacon beams (256 blocks) always stay proportionally visible while gauges (64 blocks) shrink first. Per-type overrides supported.
- ⚙️ **Adaptive render budget** — caps block-entity renders per frame under load. Minecraft draws them near-to-far, so the farthest ones yield first — a free LOD system.
- 🎯 **Small-entity culling** — dropped items, XP orbs, arrows, and item frames stop rendering past 64 blocks, where they're sub-pixel anyway.
- 📦 **Item model LOD** — distant item stacks render 1 model instead of up to 5 overlapping copies. Pixel-identical, always on.
- 🌍 **Adaptive entity/render distance** — under sustained overload, gently scales the engine's live entity view distance and effective render distance. Runtime-only — your saved options are **never** modified. Detects Distant Horizons/Voxy so LOD terrain can fill the horizon.
- ✨ **Particle budget** — a global cap with a smooth probabilistic ramp instead of a hard cutoff, so factory/weather particle storms can't tank your frame rate.

All changes are `@Inject`-only mixins — nothing is overwritten, so it stacks safely with Sodium, Flywheel, Iris shaders, and EntityCulling.

#### Commands

- `/createfpsboost` (or `/cfb`) — live FPS, quality %, and what's active right now
- `/createfpsboost report` — your pack's top 10 most-culled block-entity types, perfect for tuning per-type overrides
- `/createfpsboost gpu` — render-stack diagnostics: which GPU you're actually rendering on, Flywheel backend, Iris shader state, VRAM, Java heap sizing, and warnings for common misconfigurations (e.g. running two LOD mods at once)
- `/createfpsboost quality <10-100>` / `quality auto` — manually lock or release the adaptive quality level

#### Configuration

Every threshold is configurable in-game via **Mods → Create FPS Boost → Config**, or directly in `config/createfpsboost-client.toml`.

#### Compatibility

Client-side only. Does nothing on the server, changes nothing in `options.txt`, and is safe to keep installed when joining any server, modded or vanilla.
