package com.levi.createfpsboost.mixin;

import com.levi.createfpsboost.Perf;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Clamps the EFFECTIVE render distance during sustained overload. This is the value
 * vanilla and Sodium both read every frame to size the terrain graph; the underlying
 * option is never written, so nothing ever persists to options.txt. Inactive
 * (Integer.MAX_VALUE) at healthy FPS.
 */
@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "getEffectiveRenderDistance()I", at = @At("RETURN"), cancellable = true)
    private void cfb$clampRenderDistance(CallbackInfoReturnable<Integer> cir) {
        int limit = Perf.renderDistanceLimit();
        if (limit != Integer.MAX_VALUE && cir.getReturnValueI() > limit) {
            cir.setReturnValue(limit);
        }
    }
}
