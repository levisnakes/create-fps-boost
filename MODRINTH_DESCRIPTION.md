### Create FPS Boost

An adaptive, client-side helper for large **Create**-based modpacks — no server component, safe on any server, works in any pack (Create is not a hard dependency).

**What this mod is not:** it does not reimplement Minecraft's rendering pipeline, and it does not add culling where none existed. Minecraft already culls small entities by hitbox size (`Entity.shouldRenderAtSqrDistance`) and already culls block-entity renderers by a per-type view distance (`BlockEntityRenderer.shouldRender`). At healthy FPS, this mod changes nothing beyond what vanilla was already doing.

**What it actually adds:** it watches your real FPS every second and, only once the game is genuinely struggling, temporarily tightens those *existing* vanilla limits further than their defaults, automates the Render Distance / Entity Distance sliders down and back up for you, and enforces a genuinely new global particle budget (vanilla only caps particles per render layer at 16384, not across all particles combined). Everything restores automatically the moment FPS recovers, and a world-join grace period stops loading stutter from ever triggering it.

#### What's under the hood

- **Block-entity distance** — vanilla already culls each renderer at its own `getViewDistance()` (64 blocks for most types, 256 for beacons). This mod can scale that tighter under load, or via explicit per-type overrides that go below a renderer's own vanilla default (e.g. sign text at 48 blocks vs. vanilla's 64).
- **Adaptive render budget** — caps block-entity renders per frame under sustained load. Sections are visited in roughly camera-outward order (Minecraft's own occlusion graph), so distant sections tend to yield first — an approximation, not a guaranteed per-object sort.
- **Extra entity culling** — vanilla already culls entities by hitbox size; a 0.25-wide dropped item is already invisible past ~16 blocks with no mods at all. This only targets entity types whose *native* vanilla distance is large enough to meaningfully tighten (item frames, XP orbs, arrows) — dropped items are deliberately **not** in the default list, because vanilla already handles them better than this mechanism ever could.
- **Item model LOD** — vanilla renders up to 5 offset copies of the model for large item stacks; beyond a set distance this renders 1. This is a real, permanent visual simplification (not "pixel-identical"), always on regardless of FPS.
- **Adaptive entity/render distance** — under sustained overload, automates the same values the vanilla Entity Distance and Render Distance sliders control. Runtime-only, your saved options are never modified. Detects Distant Horizons/Voxy so LOD terrain can fill in the difference.
- **Particle budget** — a genuinely new global cap across all particle types, with a smooth probabilistic ramp instead of a hard cutoff, so factory/weather particle storms can't tank your frame rate.

All changes are `@Inject`-only mixins — nothing is overwritten, so it stacks safely with Sodium, Flywheel, Iris shaders, and EntityCulling.

#### Commands

- `/createfpsboost` (or `/cfb`) — live FPS, quality %, and what's active right now
- `/createfpsboost report` — your pack's top 10 most-culled block-entity types, useful for tuning per-type overrides
- `/createfpsboost gpu` — render-stack diagnostics: which GPU you're actually rendering on, Flywheel backend, Iris shader state, VRAM, Java heap sizing, and warnings for common misconfigurations (e.g. running two LOD mods at once)
- `/createfpsboost quality <10-100>` / `quality auto` — manually lock or release the adaptive quality level

#### Configuration

Every threshold is configurable in-game via **Mods → Create FPS Boost → Config**, or directly in `config/createfpsboost-client.toml`.

#### Compatibility

Client-side only. Does nothing on the server, changes nothing in `options.txt`, and is safe to keep installed when joining any server, modded or vanilla.
