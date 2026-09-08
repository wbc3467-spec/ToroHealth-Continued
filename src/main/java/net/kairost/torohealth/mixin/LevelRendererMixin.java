package net.kairost.torohealth.mixin;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.particle.TextRenderEntry;
import net.kairost.torohealth.client.particle.TextRenderQueue;


@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Final
    @Shadow
    private LevelRenderState levelRenderState;

    @Final
    @Shadow
    private SubmitNodeStorage submitNodeStorage;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lorg/joml/Matrix4fc;)V"))
    private void torohealth$render_RenderTextParticles(
        GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci
    ) {
        for (TextRenderEntry entry : TextRenderQueue.consume()) {
            ToroHealth.textParticleRenderer.render(entry.text(), levelRenderState.cameraRenderState.orientation, entry.x(), entry.y(), entry.z(), entry.u(), entry.v(), entry.color(), submitNodeStorage, entry.light());
        }
    }
}