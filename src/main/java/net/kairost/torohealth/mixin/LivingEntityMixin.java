package net.kairost.torohealth.mixin;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealthParticles;
import net.kairost.torohealth.config.ToroHealthConfig;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.mixin.accessor.LivingEntityAccessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements BarStateAccessor{
    @Shadow
    public abstract float getHealth();

    @Unique
    private BarState toroHealth_Continued_forge$barState;

    @Override
    public BarState torohealth$getBarState() {
        if (this.toroHealth_Continued_forge$barState == null) {
            this.toroHealth_Continued_forge$barState = BarState.create((LivingEntity) (Object) this);
        }
        return toroHealth_Continued_forge$barState;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void torohealth$tick(CallbackInfo info) {
        if (this.toroHealth_Continued_forge$barState != null) {
            this.toroHealth_Continued_forge$barState.tick();
            return;
        }
        this.toroHealth_Continued_forge$barState = BarState.create((LivingEntity) (Object) this);
    }

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("TAIL"))
    private void torohealth$onTrackedData(EntityDataAccessor<?> accessor, CallbackInfo callbackInfo) {
        if (this.toroHealth_Continued_forge$barState == null) {
            return;
        }
        if (accessor.equals(LivingEntityAccessor.getHealthData())) {
            this.toroHealth_Continued_forge$barState.updateHealth(this.getHealth());
            if (this.toroHealth_Continued_forge$barState.health != this.toroHealth_Continued_forge$barState.lastHealth) {
                this.toroHealth_Continued_forge$barState.handleHealthChange();
                // create healthChangeParticle
                if (this.toroHealth_Continued_forge$barState.healthChangeLast != 0 && ToroHealthConfig.CONFIG.particleOptions.showParticle.get() && ToroHealthConfig.CONFIG.enabled.get() && EntityUtil.getSquaredDistanceToCamera((LivingEntity) (Object) this) < ToroHealthConfig.CONFIG.particleOptions.particleDistanceSquared) {
                    Vec3 entityLocation = this.position();
                    this.level().addAlwaysVisibleParticle(ToroHealthParticles.HEALTH_CHANGE.get(), true, entityLocation.x, entityLocation.y + this.getBbHeight() / 2, entityLocation.z, Double.longBitsToDouble(this.toroHealth_Continued_forge$barState.healthChangeLast & 0xFFFFFFFFL), 0, 0);
                }
            }
        }
    }
}
