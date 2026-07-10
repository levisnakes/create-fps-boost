# Changelog

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
