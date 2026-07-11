package com.levi.createfpsboost;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runtime state shared between the config, the adaptive engine and the mixins.
 * Mixin-facing methods only read volatile snapshots, so the render-thread hot
 * path never touches the config library or synchronizes.
 */
public final class Perf {
    private static volatile boolean dirty = true;

    private static volatile boolean adaptiveOn = false;
    private static volatile int targetFps = 45;
    private static volatile float minQuality = 0.35f;
    private static volatile boolean adaptRenderDistance = false;
    private static volatile int minRenderDistanceCfg = 0;
    private static volatile boolean adaptEntityScale = false;
    private static volatile boolean hasLodMod = false;

    private static volatile boolean beOn = false;
    private static volatile float beBaseFactor = 1f;
    private static volatile Map<BlockEntityType<?>, Integer> beOverrides = Map.of();
    private static volatile boolean beBudgetOn = false;

    private static volatile boolean entOn = false;
    private static volatile int smallEntityDist = 64;
    private static volatile Set<EntityType<?>> smallEntities = Set.of();

    private static volatile boolean partOn = false;
    private static volatile int particleCapBase = 6000;

    private static volatile boolean itemLodOn = false;
    private static volatile int itemLodDistance = 24;

    /** 1.0 = full vanilla visuals; adaptively lowered towards minQuality under load. */
    private static volatile float autoQuality = 1f;
    /** Manual override from /createfpsboost quality <pct>; <= 0 means automatic. */
    private static volatile float forcedQuality = -1f;

    /** Per-frame block entity render budget; MAX_VALUE = unlimited. */
    private static volatile int beBudget = Integer.MAX_VALUE;
    private static int beRenderedThisFrame; // render thread only

    /** Effective render distance clamp in chunks; MAX_VALUE = untouched. */
    private static volatile int rdLimit = Integer.MAX_VALUE;
    private static long rdLastChange;
    private static long lowFpsSince = -1;
    private static long goodFpsSince = -1;

    /** Ignore low FPS right after world join / render distance changes (loading stutter). */
    private static long graceUntil;
    private static boolean wasInLevel;

    private static double lastViewScaleApplied = Double.NaN;
    private static boolean viewScaleManaged;

    /** Set by ItemEntityRenderer mixin around each distant item render (render thread only). */
    public static boolean itemRenderReduce;

    // Live counters for the current one-second window (render thread only).
    private static long culledBlockEntities;
    private static long culledEntities;
    private static long droppedParticles;
    private static final Map<BlockEntityType<?>, long[]> culledTypes = new IdentityHashMap<>();
    // Snapshot of the last completed window, shown by /createfpsboost.
    private static volatile long statBE;
    private static volatile long statEnt;
    private static volatile long statPart;
    private static volatile int lastFps;

    private static int frames;
    private static long windowStart = System.nanoTime();

    private Perf() {
    }

    /** Called when the config (re)loads; snapshots are rebuilt lazily on the next frame. */
    public static void invalidate() {
        dirty = true;
    }

    private static void resolveIfNeeded() {
        if (!dirty) {
            return;
        }
        try {
            adaptiveOn = Config.ADAPTIVE_ENABLED.get();
            targetFps = Config.TARGET_FPS.get();
            minQuality = (float) (double) Config.MIN_QUALITY.get();
            adaptRenderDistance = Config.SCALE_RENDER_DISTANCE.get();
            minRenderDistanceCfg = Config.MIN_RENDER_DISTANCE.get();
            adaptEntityScale = Config.SCALE_ENTITY_DISTANCE.get();
            hasLodMod = ModList.get().isLoaded("distanthorizons") || ModList.get().isLoaded("voxy");

            beBaseFactor = Config.BE_DISTANCE_PERCENT.get() / 100f;
            Map<BlockEntityType<?>, Integer> overrides = new IdentityHashMap<>();
            for (String entry : Config.BE_OVERRIDES.get()) {
                int eq = entry.lastIndexOf('=');
                if (eq <= 0) {
                    continue;
                }
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(0, eq).trim());
                if (id == null) {
                    continue;
                }
                int distance;
                try {
                    distance = Integer.parseInt(entry.substring(eq + 1).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(id)
                        .ifPresent(type -> overrides.put(type, Math.max(0, distance)));
            }
            beOverrides = overrides;
            beOn = Config.BE_CULLING_ENABLED.get();
            beBudgetOn = Config.BE_BUDGET_ENABLED.get();

            smallEntityDist = Config.SMALL_ENTITY_DISTANCE.get();
            Set<EntityType<?>> small = Collections.newSetFromMap(new IdentityHashMap<>());
            for (String entry : Config.SMALL_ENTITIES.get()) {
                ResourceLocation id = ResourceLocation.tryParse(entry.trim());
                if (id == null) {
                    continue;
                }
                BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(small::add);
            }
            smallEntities = small;
            entOn = Config.ENTITY_CULLING_ENABLED.get();

            particleCapBase = Config.MAX_PARTICLES.get();
            partOn = Config.PARTICLE_CAP_ENABLED.get();

            itemLodOn = Config.ITEM_LOD_ENABLED.get();
            itemLodDistance = Config.ITEM_LOD_DISTANCE.get();

            dirty = false;
        } catch (IllegalStateException e) {
            // Config file not loaded yet; every feature stays off until it is.
        }
    }

    /** Once-per-frame bookkeeping: budget reset, FPS window, adaptive quality and levers. */
    static void onFrame() {
        beRenderedThisFrame = 0;
        frames++;
        long now = System.nanoTime();
        long elapsed = now - windowStart;
        if (elapsed < 1_000_000_000L) {
            return;
        }
        int fps = (int) Math.round(frames * 1_000_000_000.0 / elapsed);
        frames = 0;
        windowStart = now;
        lastFps = fps;
        statBE = culledBlockEntities;
        statEnt = culledEntities;
        statPart = droppedParticles;
        culledBlockEntities = 0;
        culledEntities = 0;
        droppedParticles = 0;

        resolveIfNeeded();

        Minecraft mc = Minecraft.getInstance();
        boolean inLevel = mc.level != null;
        if (inLevel && !wasInLevel) {
            graceUntil = now + 8_000_000_000L; // world-join loading stutter is not real load
        }
        wasInLevel = inLevel;

        if (!inLevel) {
            autoQuality = 1f;
            rdLimit = Integer.MAX_VALUE;
            lowFpsSince = -1;
            goodFpsSince = -1;
            beBudget = Integer.MAX_VALUE;
            applyViewScale(mc, 1f);
            return;
        }

        if (!adaptiveOn) {
            autoQuality = 1f;
        } else if (!mc.isPaused() && now >= graceUntil) {
            if (fps < targetFps - 3) {
                float step = fps < targetFps - 15 ? 0.15f : 0.08f;
                autoQuality = Math.max(minQuality, autoQuality - step);
            } else if (fps > targetFps + 10) {
                autoQuality = Math.min(1f, autoQuality + 0.03f);
            }
        }

        float q = quality();
        beBudget = (beBudgetOn && q < 0.995f) ? Math.max(400, (int) (3000 * q * q)) : Integer.MAX_VALUE;
        applyViewScale(mc, Math.max(0.5f, Math.min(1f, q + 0.15f)));
        updateRenderDistance(mc, now, fps, q);
    }

    /**
     * Scales the engine's global entity view distance (what the vanilla "Entity Distance"
     * option feeds) at runtime. Never touches the saved option; once fully restored we
     * stop writing so other mods keep ownership of the value.
     */
    private static void applyViewScale(Minecraft mc, float scale) {
        double user = mc.options.entityDistanceScaling().get();
        double effective = (adaptEntityScale && scale < 0.995f) ? user * scale : user;
        if (!viewScaleManaged && Math.abs(effective - user) < 1e-4) {
            return;
        }
        viewScaleManaged = true;
        if (Double.isNaN(lastViewScaleApplied) || Math.abs(effective - lastViewScaleApplied) > 1e-4) {
            Entity.setViewScale(effective);
            lastViewScaleApplied = effective;
        }
        if (Math.abs(effective - user) < 1e-4) {
            viewScaleManaged = false;
        }
    }

    /**
     * Temporarily clamps the effective render distance during *sustained* overload.
     * Changing it makes Sodium/vanilla rebuild the terrain graph, so this is heavily
     * dampened: engage after 20s continuously low, one chunk per minute, restore one
     * chunk per minute after 45s continuously good. With DH/Voxy the LOD terrain
     * fills whatever the vanilla ring gives up.
     */
    private static void updateRenderDistance(Minecraft mc, long now, int fps, float q) {
        if (!adaptRenderDistance) {
            rdLimit = Integer.MAX_VALUE;
            return;
        }
        int base = mc.options.renderDistance().get();
        int floor = minRenderDistanceCfg > 0
                ? minRenderDistanceCfg
                : (hasLodMod ? Math.max(6, base / 2) : Math.max(8, base * 3 / 4));
        floor = Math.min(floor, base);

        boolean low = fps < targetFps - 3 && q <= 0.75f;
        boolean good = fps > targetFps + 10 && q >= 0.9f;
        if (low) {
            if (lowFpsSince < 0) {
                lowFpsSince = now;
            }
            goodFpsSince = -1;
        } else if (good) {
            if (goodFpsSince < 0) {
                goodFpsSince = now;
            }
            lowFpsSince = -1;
        } else {
            lowFpsSince = -1;
            goodFpsSince = -1;
        }

        int current = rdLimit == Integer.MAX_VALUE ? base : Math.min(rdLimit, base);
        boolean cooledDown = now - rdLastChange > 60_000_000_000L;
        if (lowFpsSince > 0 && now - lowFpsSince > 20_000_000_000L && cooledDown && current > floor) {
            rdLimit = current - 1;
            rdLastChange = now;
            graceUntil = now + 12_000_000_000L; // let the terrain rebuild settle
        } else if (goodFpsSince > 0 && now - goodFpsSince > 45_000_000_000L && cooledDown
                && rdLimit != Integer.MAX_VALUE) {
            int next = current + 1;
            rdLimit = next >= base ? Integer.MAX_VALUE : next;
            rdLastChange = now;
            graceUntil = now + 12_000_000_000L;
        }
    }

    /** Restore every runtime lever; called on logout and world switches. */
    public static void onWorldUnload() {
        autoQuality = 1f;
        rdLimit = Integer.MAX_VALUE;
        beBudget = Integer.MAX_VALUE;
        lowFpsSince = -1;
        goodFpsSince = -1;
        applyViewScale(Minecraft.getInstance(), 1f);
    }

    // --- Mixin-facing accessors -------------------------------------------

    /** Effective quality: manual lock if set, otherwise the adaptive value. */
    public static float quality() {
        float forced = forcedQuality;
        return forced > 0f ? forced : autoQuality;
    }

    public static void setForcedQuality(float value) {
        forcedQuality = value;
    }

    public static boolean beCulling() {
        return beOn;
    }

    /** Combined static + adaptive scale applied to each renderer's vanilla view distance. */
    public static float beScale() {
        return beBaseFactor * quality();
    }

    public static Integer beOverride(BlockEntityType<?> type) {
        return beOverrides.get(type);
    }

    public static boolean beBudgetExceeded() {
        return beRenderedThisFrame >= beBudget;
    }

    public static void noteBlockEntityRendered() {
        beRenderedThisFrame++;
    }

    /** Render distance clamp in chunks; Integer.MAX_VALUE when inactive. */
    public static int renderDistanceLimit() {
        return rdLimit;
    }

    /**
     * Render distance limit for this entity type, or -1 if this mod does not touch it.
     * Floor is 8, well below vanilla's own native cull distance for every type in the
     * default list (~32 blocks), so adaptive scaling can actually go tighter than
     * vanilla instead of asymptoting at a value vanilla already reaches on its own.
     */
    public static double smallEntityLimit(EntityType<?> type) {
        if (!entOn || !smallEntities.contains(type)) {
            return -1;
        }
        return Math.max(8f, smallEntityDist * quality());
    }

    /** Current particle budget, or -1 when disabled. Shrinks quadratically with quality. */
    public static int particleCap() {
        if (!partOn) {
            return -1;
        }
        float q = quality();
        return Math.max(512, (int) (particleCapBase * q * q));
    }

    /** True when a dropped item this far away should render one model instead of up to five. */
    public static boolean shouldReduceItem(double distSq) {
        if (!itemLodOn) {
            return false;
        }
        double d = itemLodDistance * Math.max(0.5f, quality());
        return distSq > d * d;
    }

    public static void noteBlockEntityCulled(BlockEntityType<?> type) {
        culledBlockEntities++;
        culledTypes.computeIfAbsent(type, t -> new long[1])[0]++;
    }

    public static void noteEntityCulled() {
        culledEntities++;
    }

    public static void noteParticleDropped() {
        droppedParticles++;
    }

    public static void resetStats() {
        culledTypes.clear();
    }

    public static Component statusReport() {
        Minecraft mc = Minecraft.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Create FPS Boost: %d fps | quality %d%%", lastFps, Math.round(quality() * 100)));
        if (forcedQuality > 0f) {
            sb.append(" (locked)");
        } else if (!adaptiveOn) {
            sb.append(" (adaptive off)");
        }
        if (rdLimit != Integer.MAX_VALUE && mc.level != null) {
            sb.append(String.format(" | render distance %d→%d", mc.options.renderDistance().get(), rdLimit));
        }
        if (beBudget != Integer.MAX_VALUE) {
            sb.append(String.format(" | block entity budget %d/frame", beBudget));
        }
        sb.append(String.format(" | last second: %d block entities, %d entities culled, %d particles skipped",
                statBE, statEnt, statPart));
        return Component.literal(sb.toString());
    }

    /** Top block entity types culled since world join — the pack's worst offenders. */
    public static Component reportTopCulled() {
        if (culledTypes.isEmpty()) {
            return Component.literal("Create FPS Boost: nothing culled yet. Stats accumulate while you play.");
        }
        List<Map.Entry<BlockEntityType<?>, long[]>> entries = new ArrayList<>(culledTypes.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        StringBuilder sb = new StringBuilder("Create FPS Boost - most culled block entity renderers:");
        int shown = 0;
        for (Map.Entry<BlockEntityType<?>, long[]> e : entries) {
            if (shown++ >= 10) {
                break;
            }
            ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(e.getKey());
            sb.append(String.format("%n  %s - %,d culls", id != null ? id : "unknown", e.getValue()[0]));
        }
        return Component.literal(sb.toString());
    }
}
