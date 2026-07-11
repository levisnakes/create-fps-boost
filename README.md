# Create FPS Boost

Client-side adaptive helper for NeoForge 1.21.1, built for large Create-based modpacks
(works in any pack — Create is not a hard dependency).

## What it actually does

Minecraft already does a fair amount of distance-based culling on its own:
`Entity.shouldRenderAtSqrDistance` hides small entities by hitbox size (a dropped item is
already gone past ~16 blocks with zero mods installed), and
`BlockEntityRenderer.shouldRender` already culls every block-entity renderer against its
own `getViewDistance()` (64 blocks for most types, 256 for beacons). This mod does **not**
reinvent that — it watches your real FPS and, only once the game is actually struggling,
temporarily tightens those existing vanilla limits further, automates the Render
Distance / Entity Distance sliders down and back up for you, and applies a global
particle budget that vanilla doesn't have (vanilla only caps particles per render layer,
not across all of them combined). At or above your target FPS, everything behaves exactly
as vanilla already would — there is no "extra" optimization sitting there for free.

| Lever | What it's built on | When it actually changes anything |
|---|---|---|
| Block-entity distance | Vanilla's own per-renderer `getViewDistance()` cull, scaled tighter | Only below 100% quality, or an explicit override tighter than that renderer's own default (e.g. sign text at 48 vs vanilla's 64) |
| Block-entity budget | Per-frame render cap; sections are visited in roughly camera-outward order via Minecraft's own occlusion graph, so distant sections tend to drop first (an approximation, not a guaranteed sort) | Under sustained load only |
| Extra entity culling | Vanilla's own hitbox-based cull, tightened further. Only entity types whose vanilla native distance is large enough to matter are included (item frames, XP orbs, arrows — dropped items and similar are already culled by vanilla before this could help, and are deliberately *not* in the default list) | Meaningfully, once below ~66% quality; negligibly above that |
| Item model LOD | Vanilla renders up to 5 offset copies per large stack; this renders 1 beyond a set distance | Always — a real, permanent visual simplification, not "pixel-identical" |
| Entity/render distance automation | Runtime-only adjustment of the same values the vanilla Entity Distance / Render Distance sliders control | Under sustained load only; automation, not a new capability |
| Particle budget | A genuine global cap across all particle types (vanilla only caps per render layer at 16384) | Always, ramps in as the count approaches the cap |

Everything is `@Inject`-only mixins (no overwrites), so it stacks safely with Sodium,
EntityCulling, ImmediatelyFast, Flywheel, Iris shaders, and virtual worlds (Create
ponders / schematic previews are explicitly excluded).

## Commands

- `/createfpsboost` (alias `/cfb`) — live status: FPS, quality %, active levers, culled counts last second.
- `/createfpsboost report` — top 10 most-culled block entity types (your pack's worst offenders,
  useful input for per-type overrides).
- `/createfpsboost gpu` — render-stack diagnostics: GPU/driver in use, VRAM (NVIDIA), Flywheel
  backend, Iris shader state, Java heap size, and warnings for common misconfigurations
  (integrated-GPU rendering, duplicate LOD mods, undersized/oversized heap).
- `/createfpsboost quality <10-100>` / `/createfpsboost quality auto` — lock or release the quality level.
- `/createfpsboost resetstats` — clear the report counters.

## Installation

- Drop the jar in `mods/`. Client only — safe to keep installed when joining servers;
  it changes nothing server-side and never writes to `options.txt`.
- Config: `config/createfpsboost-client.toml`, or Mods → Create FPS Boost → Config in-game.

## Building from source

```
./gradlew build
```

Jar lands in `build/libs/`. Requires NeoForge 21.1.x, Java 21.

## License

LGPL-3.0-only — see [LICENSE](LICENSE).
