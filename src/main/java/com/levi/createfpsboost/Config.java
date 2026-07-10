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
                        "Temporarily reduce the EFFECTIVE render distance during sustained low FPS (20s+ continuously below target).",
                        "Runtime-only: your saved options are never modified, and it restores itself when FPS recovers.",
                        "With Distant Horizons or Voxy installed, LOD terrain fills the horizon so this is nearly invisible.",
                        "Changes are rare and heavily dampened (one chunk per minute) because they trigger a background terrain rebuild.")
                .define("scaleRenderDistance", true);
        MIN_RENDER_DISTANCE = b
                .comment(
                        "Lowest effective render distance (chunks) the engine may reach. 0 = auto:",
                        "half your render distance (min 6) when a LOD mod is installed, otherwise 75% (min 8).")
                .defineInRange("minRenderDistance", 0, 0, 32);
        SCALE_ENTITY_DISTANCE = b
                .comment(
                        "Scale the global entity render distance under load (the engine value behind vanilla's 'Entity Distance').",
                        "Runtime-only with a 50% floor; your saved option is never modified.")
                .define("scaleEntityDistance", true);
        b.pop();

        b.comment(
                "Block entity renderer culling (Create gauges, displays, funnels, signs, chests...).",
                "100% = exactly vanilla distances. The adaptive engine may scale this down further under load.")
                .push("blockEntities");
        BE_CULLING_ENABLED = b
                .comment("Master switch for block entity distance culling.")
                .define("enabled", true);
        BE_DISTANCE_PERCENT = b
                .comment("Base render distance as a percent of each renderer's vanilla view distance (most are 64 blocks, beacons 256).")
                .defineInRange("distancePercent", 100, 25, 100);
        BE_OVERRIDES = b
                .comment(
                        "Absolute per-type distance overrides, format \"modid:block_entity=distance\".",
                        "Defaults cull sign TEXT beyond 48 blocks (the sign board itself is part of the chunk and always renders).")
                .defineListAllowEmpty("overrides",
                        List.of("minecraft:sign=48", "minecraft:hanging_sign=48"),
                        () -> "modid:block_entity=48",
                        o -> o instanceof String);
        BE_BUDGET_ENABLED = b
                .comment(
                        "Under load, cap how many block entities render per frame. Minecraft iterates them roughly",
                        "near-to-far, so the farthest renderers yield first - a free LOD system. Inactive at full quality.")
                .define("adaptiveBudget", true);
        b.pop();

        b.comment(
                "Dropped item stacks render up to 5 copies of their model. Beyond this distance the copies",
                "overlap within a pixel, so only 1 is rendered - up to 80% fewer item draws on loaded belts.")
                .push("items");
        ITEM_LOD_ENABLED = b
                .comment("Master switch for the item model LOD.")
                .define("enabled", true);
        ITEM_LOD_DISTANCE = b
                .comment("Distance in blocks beyond which item stacks render a single model.")
                .defineInRange("lodDistance", 24, 8, 128);
        b.pop();

        b.comment(
                "Culling for entities that are sub-pixel specks at long range (dropped items, XP orbs, arrows, item frames).",
                "At the default 64 blocks these are physically invisible, so nothing is lost.")
                .push("entities");
        ENTITY_CULLING_ENABLED = b
                .comment("Master switch for small entity distance culling.")
                .define("enabled", true);
        SMALL_ENTITY_DISTANCE = b
                .comment("Distance in blocks beyond which the listed entity types stop rendering.")
                .defineInRange("smallEntityDistance", 64, 16, 256);
        SMALL_ENTITIES = b
                .comment("Entity types treated as 'small'. Never add players, mobs you fight, or Create contraptions/trains.")
                .defineListAllowEmpty("smallEntities",
                        List.of(
                                "minecraft:item",
                                "minecraft:experience_orb",
                                "minecraft:arrow",
                                "minecraft:spectral_arrow",
                                "minecraft:snowball",
                                "minecraft:egg",
                                "minecraft:ender_pearl",
                                "minecraft:potion",
                                "minecraft:experience_bottle",
                                "minecraft:item_frame",
                                "minecraft:glow_item_frame"),
                        () -> "minecraft:item",
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
