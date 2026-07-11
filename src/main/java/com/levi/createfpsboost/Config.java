package com.levi.createfpsboost;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ADAPTIVE_ENABLED;
    public static final ModConfigSpec.IntValue TARGET_FPS;
    public static final ModConfigSpec.DoubleValue MIN_QUALITY;
    public static final ModConfigSpec.BooleanValue SCALE_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue MIN_RENDER_DISTANCE;
    public static final ModConfigSpec.BooleanValue SCALE_ENTITY_DISTANCE;

    public static final ModConfigSpec.BooleanValue BE_CULLING_ENABLED;
    public static final ModConfigSpec.IntValue BE_DISTANCE_PERCENT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BE_OVERRIDES;
    public static final ModConfigSpec.BooleanValue BE_BUDGET_ENABLED;

    public static final ModConfigSpec.BooleanValue ITEM_LOD_ENABLED;
    public static final ModConfigSpec.IntValue ITEM_LOD_DISTANCE;

    public static final ModConfigSpec.BooleanValue ENTITY_CULLING_ENABLED;
    public static final ModConfigSpec.IntValue SMALL_ENTITY_DISTANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SMALL_ENTITIES;

    public static final ModConfigSpec.BooleanValue PARTICLE_CAP_ENABLED;
    public static final ModConfigSpec.IntValue MAX_PARTICLES;

    private Config() {
    }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment(
                "Adaptive quality engine: watches your real FPS and only gets aggressive when the game struggles.",
                "At or above the target FPS the game renders 100% vanilla - nothing is culled beyond your base settings.")
                .push("adaptive");
        ADAPTIVE_ENABLED = b
                .comment("Enable adaptive scaling. When off, only the static settings below apply.")
                .define("enabled", true);
        TARGET_FPS = b
                .comment("FPS you want to hold. Below this the mod gradually shrinks render distances and particle budget.")
                .defineInRange("targetFps", 45, 10, 240);
        MIN_QUALITY = b
                .comment("Hard floor for adaptive scaling. 0.35 = distances never drop below 35% of normal, no matter how low FPS gets.")
                .defineInRange("minQuality", 0.35, 0.1, 1.0);
        SCALE_RENDER_DISTANCE = b
                .comment(
                        "Automates the vanilla Render Distance slider during sustained low FPS (20s+ continuously below",
                        "target): temporarily reduces the EFFECTIVE render distance, the same value the slider controls.",
                        "This does not add a new capability - it just adjusts the existing option for you so you don't",
                        "have to. Runtime-only: your saved option is never modified, and it restores itself when FPS",
                        "recovers. With Distant Horizons or Voxy installed, LOD terrain fills the horizon so the visual",
                        "change is much less noticeable. Changes are rare and heavily dampened (one chunk per minute)",
                        "because lowering render distance triggers a background terrain rebuild.")
                .define("scaleRenderDistance", true);
        MIN_RENDER_DISTANCE = b
                .comment(
                        "Lowest effective render distance (chunks) the engine may reach. 0 = auto:",
                        "half your render distance (min 6) when a LOD mod is installed, otherwise 75% (min 8).")
                .defineInRange("minRenderDistance", 0, 0, 32);
        SCALE_ENTITY_DISTANCE = b
                .comment(
                        "Automates the vanilla Entity Distance slider under load (the same engine value it controls).",
                        "Like the render distance option above, this adjusts an existing vanilla setting rather than",
                        "adding new capability. Runtime-only with a 50% floor; your saved option is never modified.")
                .define("scaleEntityDistance", true);
        b.pop();

        b.comment(
                "Block entity renderer culling (Create gauges, displays, funnels, signs, chests...).",
                "Vanilla ALREADY culls every block entity renderer against its own getViewDistance() (64 blocks",
                "for most, 256 for beacons) - see BlockEntityRenderer.shouldRender(). At 100% and with no overrides",
                "this section changes nothing over vanilla; it only starts culling once it goes below 100%, or a",
                "per-type override is tighter than that renderer's own vanilla default.")
                .push("blockEntities");
        BE_CULLING_ENABLED = b
                .comment("Master switch for this extra block entity distance culling.")
                .define("enabled", true);
        BE_DISTANCE_PERCENT = b
                .comment("Render distance as a percent of each renderer's OWN vanilla view distance (see comment above).")
                .defineInRange("distancePercent", 100, 25, 100);
        BE_OVERRIDES = b
                .comment(
                        "Absolute per-type distance overrides, format \"modid:block_entity=distance\". Only useful where",
                        "the number is BELOW that renderer's vanilla default (64 for most types, verify with a mod like",
                        "Jade/HWYLA if unsure). Defaults cull sign text beyond 48 blocks, tighter than vanilla's 64 default",
                        "(the sign board itself is part of the chunk mesh and always renders regardless).")
                .defineListAllowEmpty("overrides",
                        List.of("minecraft:sign=48", "minecraft:hanging_sign=48"),
                        () -> "modid:block_entity=48",
                        o -> o instanceof String);
        BE_BUDGET_ENABLED = b
                .comment(
                        "Under sustained load, caps how many block entities render per frame. Sections are visited in",
                        "roughly camera-outward order (Minecraft's own occlusion graph), so this tends to drop the most",
                        "distant sections' renderers first - an approximation, not a guaranteed per-object distance sort.",
                        "Inactive at full quality.")
                .define("adaptiveBudget", true);
        b.pop();

        b.comment(
                "Dropped item stacks render up to 5 offset copies of their model for large counts. Beyond this",
                "distance the offset is barely noticeable, so only 1 copy is drawn - fewer item draws on loaded belts.",
                "This is a visible simplification (not pixel-identical to vanilla), always applied regardless of FPS.")
                .push("items");
        ITEM_LOD_ENABLED = b
                .comment("Master switch for the item model LOD.")
                .define("enabled", true);
        ITEM_LOD_DISTANCE = b
                .comment("Distance in blocks beyond which item stacks render a single model.")
                .defineInRange("lodDistance", 24, 8, 128);
        b.pop();

        b.comment(
                "Vanilla ALREADY culls every entity by size: Entity.shouldRenderAtSqrDistance() hides an entity",
                "beyond (avgHitboxSize * 64 * entityDistanceScaling) blocks. A 0.25-wide dropped item is already",
                "invisible past ~16 blocks with no mods at all - this section can never improve on that, so only",
                "entity types with a LARGER native distance are listed here by default (item frames, arrows and",
                "XP orbs default to ~32 vanilla blocks). This only tightens things further during sustained low FPS.")
                .push("entities");
        ENTITY_CULLING_ENABLED = b
                .comment("Master switch for this extra entity distance culling.")
                .define("enabled", true);
        SMALL_ENTITY_DISTANCE = b
                .comment(
                        "Distance in blocks beyond which the listed entity types stop rendering, before adaptive scaling.",
                        "Only has an effect where it is tighter than that type's own vanilla native cull distance.")
                .defineInRange("smallEntityDistance", 48, 8, 256);
        SMALL_ENTITIES = b
                .comment(
                        "Entity types this applies to. Only types whose vanilla native cull distance is large enough",
                        "for this to matter are listed by default (see comment above) - dropped items, snowballs, eggs,",
                        "ender pearls, potions and XP bottles are already culled by vanilla before this could help, so",
                        "they are intentionally NOT included. Never add players, mobs you fight, or Create contraptions/trains.")
                .defineListAllowEmpty("smallEntities",
                        List.of(
                                "minecraft:experience_orb",
                                "minecraft:arrow",
                                "minecraft:spectral_arrow",
                                "minecraft:item_frame",
                                "minecraft:glow_item_frame"),
                        () -> "minecraft:item_frame",
                        o -> o instanceof String);
        b.pop();

        b.comment(
                "Total particle budget. Vanilla allows 16384 PER render type, which factory explosions and",
                "weather mods happily saturate. New particles beyond the cap are simply not spawned.")
                .push("particles");
        PARTICLE_CAP_ENABLED = b
                .comment("Master switch for the particle budget.")
                .define("enabled", true);
        MAX_PARTICLES = b
                .comment("Maximum simultaneous particles. The adaptive engine shrinks this quadratically under load (floor 512).")
                .defineInRange("maxParticles", 6000, 512, 16384);
        b.pop();

        SPEC = b.build();
    }
}
