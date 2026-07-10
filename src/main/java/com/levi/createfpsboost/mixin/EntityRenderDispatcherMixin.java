package com.levi.createfpsboost.mixin;

import com.levi.createfpsboost.Perf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips rendering of configured "small" entity types (dropped items, XP orbs,
 * arrows, item frames...) beyond a distance where they are sub-pixel anyway.
 * Entities in virtual/preview levels are never touched.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(
            method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void cfb$cullTinyDistantEntities(Entity entity, Frustum frustum,
            double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        double limit = Perf.smallEntityLimit(entity.getType());
        if (limit < 0 || entity.level() != Minecraft.getInstance().level) {
            return;
        }
        if (entity.distanceToSqr(camX, camY, camZ) > limit * limit) {
            Perf.noteEntityCulled();
            cir.setReturnValue(false);
        }
    }
}
