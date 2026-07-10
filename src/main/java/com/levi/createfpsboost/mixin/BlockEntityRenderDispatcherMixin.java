package com.levi.createfpsboost.mixin;

import com.levi.createfpsboost.Perf;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Distance-culls block entity renderers and enforces the adaptive per-frame render
 * budget. With default config (100%) and healthy FPS this never cancels anything
 * except explicit per-type overrides; under load the adaptive quality scale shrinks
 * every renderer's vanilla view distance proportionally, so beacons (256) keep
 * rendering much farther than gauges (64). Because Minecraft iterates block entities
 * roughly near-to-far by section, the budget culls the farthest renderers first.
 *
 * Only block entities that belong to the live client level are touched - ponder
 * scenes, schematic previews and other virtual levels pass through untouched.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow
    public Camera camera;

    @Shadow
    public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity);

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void cfb$cullDistantBlockEntities(BlockEntity blockEntity, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (blockEntity == null || blockEntity.getLevel() != Minecraft.getInstance().level) {
            return;
        }
        if (Perf.beBudgetExceeded()) {
            Perf.noteBlockEntityCulled(blockEntity.getType());
            ci.cancel();
            return;
        }
        if (!Perf.beCulling()) {
            Perf.noteBlockEntityRendered();
            return;
        }
        Camera cam = this.camera;
        if (cam == null) {
            Perf.noteBlockEntityRendered();
            return;
        }
        float scale = Perf.beScale();
        Integer override = Perf.beOverride(blockEntity.getType());
        if (override == null && scale >= 0.995f) {
            Perf.noteBlockEntityRendered();
            return; // vanilla path, nothing to do
        }

        double limit;
        if (override != null) {
            limit = override * Perf.quality();
        } else {
            BlockEntityRenderer<BlockEntity> renderer = this.getRenderer(blockEntity);
            if (renderer == null) {
                return;
            }
            limit = renderer.getViewDistance() * scale;
        }

        BlockPos pos = blockEntity.getBlockPos();
        Vec3 c = cam.getPosition();
        double dx = c.x - (pos.getX() + 0.5);
        double dy = c.y - (pos.getY() + 0.5);
        double dz = c.z - (pos.getZ() + 0.5);
        if (dx * dx + dy * dy + dz * dz > limit * limit) {
            Perf.noteBlockEntityCulled(blockEntity.getType());
            ci.cancel();
        } else {
            Perf.noteBlockEntityRendered();
        }
    }
}
