package com.levi.createfpsboost;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import org.lwjgl.opengl.GL11;

/**
 * Live render-stack diagnostics for /createfpsboost gpu. Everything mod-specific is
 * probed via reflection with broad catches, so this mod keeps zero hard
 * dependencies and works in any pack.
 */
public final class GpuDoctor {
    private GpuDoctor() {
    }

    public static Component run() {
        StringBuilder sb = new StringBuilder("Create FPS Boost render-stack check:");

        String renderer = safeGlString(GL11.GL_RENDERER);
        String vendor = safeGlString(GL11.GL_VENDOR);
        String version = safeGlString(GL11.GL_VERSION);
        sb.append("\n GPU: ").append(renderer).append(" | ").append(vendor);
        sb.append("\n OpenGL: ").append(version);

        long[] vram = nvidiaVramKb();
        if (vram != null) {
            sb.append(String.format("%n VRAM: %,d MB free of %,d MB", vram[1] / 1024, vram[0] / 1024));
        }

        long maxHeapMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        sb.append(String.format("%n Java heap: %,d MB max", maxHeapMb));

        sb.append("\n\nFindings:");
        int findings = 0;

        String lowerRenderer = renderer.toLowerCase();
        if (lowerRenderer.contains("intel") || lowerRenderer.contains("microsoft basic")) {
            findings++;
            sb.append("\n [!] Rendering on what looks like an integrated GPU. If this PC has a dedicated GPU,"
                    + " set your Minecraft java to 'High performance' in Windows Settings > Display > Graphics.");
        }

        if (!ModList.get().isLoaded("sodium") && !ModList.get().isLoaded("embeddium")) {
            findings++;
            sb.append("\n [!] No Sodium/Embeddium detected - that is the single biggest renderer upgrade available.");
        }

        String flywheel = flywheelBackend();
        boolean hasCreate = ModList.get().isLoaded("create");
        if (hasCreate && flywheel != null) {
            sb.append("\n Flywheel backend: ").append(flywheel);
            String fw = flywheel.toLowerCase();
            if (fw.contains("off") || fw.contains("batching")) {
                findings++;
                sb.append("\n [!] Create's animation engine is on its slow path (").append(flywheel)
                        .append("). Set \"backend\" to \"flywheel:indirect\" (or instancing) in config/flywheel.json"
                                + " for a big FPS gain around contraptions.");
            }
        }

        Boolean shaders = shadersInUse();
        if (Boolean.TRUE.equals(shaders)) {
            sb.append("\n Iris shaders: ACTIVE");
            findings++;
            sb.append("\n [~] Shaders often spend 30-50% of frame time on the shadow pass."
                    + " Lowering Shadow Distance in the shader pack settings is the cheapest big win.");
        } else if (Boolean.FALSE.equals(shaders)) {
            sb.append("\n Iris shaders: installed, not active");
        }

        boolean dh = ModList.get().isLoaded("distanthorizons");
        boolean voxy = ModList.get().isLoaded("voxy");
        if (dh && voxy) {
            findings++;
            sb.append("\n [!] BOTH Distant Horizons and Voxy are loaded. They do the same job (LOD terrain)"
                    + " and each costs VRAM, CPU and frame time - disable one of them.");
        }

        if (maxHeapMb < 4096) {
            findings++;
            sb.append(String.format("%n [!] Only %,d MB heap for a pack this size. Allocate 6-8 GB in the"
                    + " launcher's Java settings (more is NOT better - long GC pauses).", maxHeapMb));
        } else if (maxHeapMb > 12288) {
            findings++;
            sb.append(String.format("%n [~] %,d MB heap is more than this pack needs and can cause GC stutter;"
                    + " 6-8 GB is the sweet spot.", maxHeapMb));
        }

        if (findings == 0) {
            sb.append("\n [OK] No rendering-stack problems detected. The GL chain is set up as well as it can be.");
        }
        return Component.literal(sb.toString());
    }

    private static String safeGlString(int name) {
        try {
            String s = GL11.glGetString(name);
            return s != null ? s : "unknown";
        } catch (Throwable t) {
            return "unavailable";
        }
    }

    /** NVIDIA-only GL_NVX_gpu_memory_info; returns {totalKb, freeKb} or null. */
    private static long[] nvidiaVramKb() {
        try {
            int total = GL11.glGetInteger(0x9047); // GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX
            int free = GL11.glGetInteger(0x9049); // GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX
            GL11.glGetError(); // clear INVALID_ENUM on non-NVIDIA drivers
            if (total > 0) {
                return new long[] { total, free };
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String flywheelBackend() {
        try {
            Class<?> manager = Class.forName("dev.engine_room.flywheel.api.backend.BackendManager");
            Object backend;
            try {
                backend = manager.getMethod("currentBackend").invoke(null);
            } catch (NoSuchMethodException e) {
                backend = manager.getMethod("getBackend").invoke(null);
            }
            return backend == null ? null : backend.getClass().getSimpleName();
        } catch (Throwable t) {
            return null;
        }
    }

    private static Boolean shadersInUse() {
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            return (Boolean) api.getMethod("isShaderPackInUse").invoke(instance);
        } catch (Throwable t) {
            return null;
        }
    }
}
