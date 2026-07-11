package com.levi.createfpsboost.mixin;

import com.levi.createfpsboost.Perf;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item model LOD: vanilla renders up to 5 offset copies of the model for large stacks.
 * Beyond the LOD distance a single model is rendered instead. This is a visible
 * simplification, not pixel-identical to vanilla - it trades the multi-copy "pile"
 * look at range for fewer draws on loaded belts. Always applied regardless of FPS.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    protected ItemEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void cfb$markDistantItem(ItemEntity entity, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        Camera cam = this.entityRenderDispatcher.camera;
        Perf.itemRenderReduce = cam != null
                && Perf.shouldReduceItem(entity.distanceToSqr(cam.getPosition()));
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void cfb$clearDistantItem(ItemEntity entity, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        Perf.itemRenderReduce = false;
    }

    @Inject(method = "getRenderedAmount(I)I", at = @At("HEAD"), cancellable = true)
    private static void cfb$singleModelWhenFar(int count, CallbackInfoReturnable<Integer> cir) {
        if (Perf.itemRenderReduce) {
            cir.setReturnValue(1);
        }
    }
}
