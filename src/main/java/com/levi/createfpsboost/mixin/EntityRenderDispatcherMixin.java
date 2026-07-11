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
 * Skips rendering of configured entity types beyond a configurable distance. Vanilla
 * (Entity.shouldRenderAtSqrDistance) already culls every entity by hitbox size, so this
 * only matters for types whose native cull distance is large enough to improve on -
 * see Config.SMALL_ENTITIES for which types that applies to and why. Entities in
 * virtual/preview levels are never touched.
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
