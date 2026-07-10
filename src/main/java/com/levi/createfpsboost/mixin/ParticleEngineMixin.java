package com.levi.createfpsboost.mixin;

import com.levi.createfpsboost.Perf;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Enforces a total particle budget. Vanilla only caps per render type (16384 each),
 * so factory explosions plus weather mods can pile up tens of thousands of live
 * particles. Approaching the budget, new particles are probabilistically thinned
 * (a smooth ramp instead of a visible cliff); at the cap they stop spawning.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Shadow
    @Final
    private Queue<Particle> particlesToAdd;

    @Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void cfb$capParticles(Particle particle, CallbackInfo ci) {
        int cap = Perf.particleCap();
        if (cap < 0) {
            return;
        }
        int count = this.particlesToAdd.size();
        for (Queue<Particle> queue : this.particles.values()) {
            count += queue.size();
        }
        if (count >= cap) {
            Perf.noteParticleDropped();
            ci.cancel();
            return;
        }
        int rampStart = cap * 3 / 5;
        if (count > rampStart) {
            float over = (count - rampStart) / (float) (cap - rampStart);
            if (ThreadLocalRandom.current().nextFloat() < over * 0.6f) {
                Perf.noteParticleDropped();
                ci.cancel();
            }
        }
    }
}
