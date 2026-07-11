# Changelog

## 1.1.0

Correctness pass, prompted by a Modrinth review rejection flagging that some described
features were redundant with existing vanilla behavior. Verified against decompiled
game source; the reviewer was right about several claims.

- **Removed** dropped items, snowballs, eggs, ender pearls, potions and XP bottles from
  the default entity-culling list — vanilla's own hitbox-based cull (`Entity.shouldRenderAtSqrDistance`)
  already hides these before this mod's mechanism could ever improve on it (a dropped
  item is already gone past ~16 blocks with no mods at all). The default list now only
  contains entity types with a large enough native vanilla distance for this to matter
  (item frames, XP orbs, arrows).
- Lowered the entity-culling floor from 16 to 8 blocks so adaptive scaling can actually
  go tighter than vanilla's own native distance for the remaining types, instead of
  asymptoting at a value vanilla already reaches on its own.
- Corrected the block-entity distance-culling description: at 100% and with no
  overrides, this changes nothing over vanilla's own `BlockEntityRenderer.shouldRender()`
  cull — it only does real work below 100% quality or with an override tighter than a
  renderer's own vanilla default.
- Corrected the "near-to-far, free LOD" claim for the block-entity render budget: it's
  an approximation based on Minecraft's own section occlusion-graph traversal order, not
  a guaranteed per-object distance sort.
- Removed the "pixel-identical" claim from the item model LOD feature — collapsing up to
  5 rendered copies to 1 is a real, visible simplification, not an identical result.
- Reworded the adaptive entity/render-distance features to be explicit that they
  automate existing vanilla sliders rather than adding new rendering capability.
- All documentation (README, Modrinth listing text, in-game config comments) rewritten
  to match.

## 1.0.0

Initial public release.

- Adaptive quality engine: measures real FPS every second, only scales down when you're below target, restores automatically. 100% vanilla rendering at/above target FPS.
- Block-entity distance culling with per-renderer-relative scaling (beacons keep their 256-block range while gauges use their normal 64) and per-type overrides.
- Adaptive per-frame block-entity render budget — a free LOD system that yields the farthest renderers first under sustained load.
- Small-entity culling for dropped items, XP orbs, arrows, and item frames beyond a configurable distance.
- Item model LOD: distant dropped-item stacks render a single model instead of up to five overlapping copies.
- Adaptive global entity view distance, applied at runtime without touching your saved options.
- Adaptive effective render distance for sustained overload, heavily dampened to avoid triggering repeated terrain rebuilds; detects Distant Horizons/Voxy to size its floor.
- Global particle budget with a smooth probabilistic thinning ramp instead of a hard cutoff.
- `/createfpsboost` (alias `/cfb`) commands: live status, `report` (top culled block-entity types), `gpu` (render-stack diagnostics: GPU/driver info, Flywheel backend, Iris shader state, VRAM, heap size, duplicate-LOD-mod detection), `quality <percent>|auto`, `resetstats`.
- Full config screen (Mods menu) and `config/createfpsboost-client.toml`.
