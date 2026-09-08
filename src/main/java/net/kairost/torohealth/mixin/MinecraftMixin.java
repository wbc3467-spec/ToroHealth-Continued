package net.kairost.torohealth.mixin;

import net.kairost.torohealth.config.ToroHealthConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.util.RayTrace;


@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Final
    @Shadow
    private DeltaTracker.Timer deltaTracker;


    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pick(F)V", shift = At.Shift.AFTER))
    private void torohealth$preRenderWorld(boolean advanceGameTime, CallbackInfo info) {
        if (ToroHealthConfig.CONFIG.enabled.get()) {
            float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
            LivingEntity entity = RayTrace.getEntityInCrosshair(tickDelta, (float) Math.max(ToroHealthConfig.CONFIG.hudOptions.hudDistance.get(), ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarDistance.get()));
            ToroHealth.setTargetedEntity(entity);
            if (ToroHealth.toroHealthHud != null) {
                ToroHealth.toroHealthHud.setEntity(entity);
            }
        }
    }
}
