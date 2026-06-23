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
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.mixin.accessor.LivingEntityAccessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements BarStateAccessor{
    @Shadow
    public abstract float getHealth();

    @Unique
    private BarState barState;

    @Override
    public BarState torohealth$getBarState() {
        if (this.barState == null) {
            this.barState = BarState.create((LivingEntity) (Object) this);
        }
        return barState;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void torohealth$tick(CallbackInfo info) {
        if (this.barState != null) {
            this.barState.tick();
            return;
        }
        this.barState = BarState.create((LivingEntity) (Object) this);
    }

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("TAIL"))
    private void torohealth$onTrackedData(EntityDataAccessor<?> accessor, CallbackInfo callbackInfo) {
        if (this.barState == null) {
            return;
        }
        if (accessor.equals(LivingEntityAccessor.getHealthData())) {
            this.barState.updateHealth(this.getHealth());
            if (this.barState.health != this.barState.lastHealth) {
                this.barState.handleHealthChange();
                // create healthChangeParticle
                if (this.barState.healthChangeLast != 0 && ToroHealth.getConfig().particleOptions.showParticle && ToroHealth.getConfig().enabled && EntityUtil.getSquaredDistanceToCamera((LivingEntity) (Object) this) < ToroHealth.getConfig().particleOptions.particleDistanceSquared) {
                    Vec3 entityLocation = this.position();
                    this.level().addAlwaysVisibleParticle(ToroHealth.HEALTH_CHANGE, true, entityLocation.x, entityLocation.y + this.getBbHeight() / 2, entityLocation.z, Double.longBitsToDouble(this.barState.healthChangeLast & 0xFFFFFFFFL), 0, 0);
                }
            }
        }
    }
}
