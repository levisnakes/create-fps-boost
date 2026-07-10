package com.levi.createfpsboost;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = CreateFpsBoost.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Post event) {
        Perf.onFrame();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Perf.onWorldUnload();
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("createfpsboost")
                .then(Commands.literal("report").executes(ctx -> {
                    ctx.getSource().sendSystemMessage(Perf.reportTopCulled());
                    return 1;
                }))
                .then(Commands.literal("gpu").executes(ctx -> {
                    ctx.getSource().sendSystemMessage(GpuDoctor.run());
                    return 1;
                }))
                .then(Commands.literal("resetstats").executes(ctx -> {
                    Perf.resetStats();
                    ctx.getSource().sendSystemMessage(Component.literal("Create FPS Boost: culling stats reset."));
                    return 1;
                }))
                .then(Commands.literal("quality")
                        .then(Commands.literal("auto").executes(ctx -> {
                            Perf.setForcedQuality(-1f);
                            ctx.getSource().sendSystemMessage(
                                    Component.literal("Create FPS Boost: quality back to automatic."));
                            return 1;
                        }))
                        .then(Commands.argument("percent", IntegerArgumentType.integer(10, 100))
                                .executes(ctx -> {
                                    int pct = IntegerArgumentType.getInteger(ctx, "percent");
                                    Perf.setForcedQuality(pct / 100f);
                                    ctx.getSource().sendSystemMessage(Component.literal(
                                            "Create FPS Boost: quality locked at " + pct
                                                    + "%. Use /createfpsboost quality auto to release."));
                                    return 1;
                                })))
                .executes(ctx -> {
                    ctx.getSource().sendSystemMessage(Perf.statusReport());
                    return 1;
                }));
        event.getDispatcher().register(Commands.literal("cfb")
                .redirect(event.getDispatcher().getRoot().getChild("createfpsboost")));
    }
}
