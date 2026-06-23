package net.kairost.torohealth.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.client.render.InWorldBarRenderer;
import net.kairost.torohealth.config.ModConfig;

import java.util.Objects;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Final
    @Shadow
    private LevelRenderState levelRenderState;

    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private LevelRenderer levelRenderer;

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;extract(Lnet/minecraft/client/renderer/state/level/ParticlesRenderState;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;F)V"))
    private void torohealth$render_AddInWorldBarsToBatch(
        DeltaTracker deltaTracker,
        Camera camera,
        float deltaPaitialTick,
        CallbackInfo callbackInfo,
        @Local(ordinal = 0) Frustum frustum
    ) {
        Vec3 vec3d = camera.position();
        double d = vec3d.x();
        double e = vec3d.y();
        double f = vec3d.z();
        EntityRenderDispatcher entityRenderDispatcher = this.levelRenderer.entityRenderDispatcher();
        for (Entity entity : this.level.entitiesForRendering()) {
            int light = ModConfig.INSTANCE.inWorldBarOptions.inWorldBarLightMode.equals(ModConfig.InWorldBarLightMode.FULL_BRIGHT) ? LightCoordsUtil.FULL_BRIGHT : entityRenderDispatcher.getPackedLightCoords(entity, deltaPaitialTick);
            if (entityRenderDispatcher.shouldRender(entity, frustum, d, e, f) || entity.hasIndirectPassenger(Objects.requireNonNull(this.minecraft.player))) {
                InWorldBarRenderer.render(entity, camera, deltaPaitialTick, light, entityRenderDispatcher);
            }
        }
        this.levelRenderState.particlesRenderState.add(InWorldBarRenderer.getSubmittable());
    }
}
