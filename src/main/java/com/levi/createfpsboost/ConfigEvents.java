package com.levi.createfpsboost;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = CreateFpsBoost.MODID, value = Dist.CLIENT)
public final class ConfigEvents {
    private ConfigEvents() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (CreateFpsBoost.MODID.equals(event.getConfig().getModId())) {
            Perf.invalidate();
        }
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        if (CreateFpsBoost.MODID.equals(event.getConfig().getModId())) {
            Perf.invalidate();
        }
    }
}
