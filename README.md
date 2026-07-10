# Create FPS Boost

Client-side adaptive performance mod for NeoForge 1.21.1, built for large Create-based
modpacks (works in any pack — Create is not a hard dependency).

## Why it exists

Big Create packs already ship Sodium, Lithium, ModernFix, FerriteCore, ImmediatelyFast,
EntityCulling, C2ME and Krypton — the generic optimizations are taken. What still kills
FPS is pack-specific:

- **Hundreds of block-entity renderers** — gauges, displays, funnels, smart observers,
  stock tickers, train stations — all individually rendered every frame out to 64 blocks.
- **Item/entity floods** — belts and factories drop thousands of item entities, XP orbs
  and projectiles that are sub-pixel specks past ~50 blocks but still cost draws.
- **Particle storms** — vanilla caps particles at 16384 *per render layer*, so steam,
  weather mods and explosions can pile up tens of thousands of live particles.
- **Nothing adapts** — when FPS tanks in a mega-factory, every mod keeps rendering
  everything at full distance anyway.

## What it does

All levers are driven by one **adaptive quality engine** that measures real FPS every
second. At or above your target FPS (default 45) the game renders **100% vanilla —
nothing is culled** (except the always-on item LOD, which is pixel-identical). Below it,
quality scales down smoothly and recovers automatically. A world-join grace period keeps
loading stutter from triggering it.

| Lever | Mechanism | When active |
|---|---|---|
| Block-entity distance | Scales each renderer's *own* view distance (beacons keep their 256) + per-type overrides | Under load / overrides always |
| Block-entity budget | Per-frame render cap; vanilla iterates near→far, so the farthest yield first (free LOD) | Under load only |
| Small-entity culling | Items, XP orbs, arrows, item frames beyond 64 blocks (sub-pixel) | Always (distance shrinks under load) |
| Item model LOD | Stacks render 1 model instead of up to 5 beyond 24 blocks | Always (pixel-identical) |
| Global entity distance | Runtime `Entity.setViewScale`, floor 50%, options never written | Under load only |
| Effective render distance | Clamps `getEffectiveRenderDistance` (what Sodium reads); Distant Horizons/Voxy LODs fill the horizon; heavily dampened (engages after 20s sustained low FPS, 1 chunk/min, auto-restores) | Sustained overload only |
| Particle budget | Global cap 6000 with a probabilistic thinning ramp near the cap | Always (cap shrinks under load) |

Everything is `@Inject`-only mixins (no overwrites), so it stacks safely with Sodium,
EntityCulling, ImmediatelyFast, Flywheel, Iris shaders, and virtual worlds (Create
ponders / schematic previews are explicitly excluded from culling).

## Commands

- `/createfpsboost` (alias `/cfb`) — live status: FPS, quality %, active levers, culled counts last second.
- `/createfpsboost report` — top 10 most-culled block entity types (your pack's worst offenders,
  great input for per-type overrides).
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
