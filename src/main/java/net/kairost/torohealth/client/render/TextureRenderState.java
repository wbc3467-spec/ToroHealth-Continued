package net.kairost.torohealth.client.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.kairost.torohealth.ToroHealth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


//modified on BillboardParticleSubmittable
public class TextureRenderState extends QuadParticleRenderState implements ParticleGroupRenderState {
    private static final int INITIAL_BUFFER_MAX_LENGTH = 1024;
    private static final int BUFFER_FLOAT_FIELDS = 14;
    private static final int BUFFER_INT_FIELDS = 2;
    private final Map<SingleQuadParticle.Layer, TextureRenderState.Storage> particles = new HashMap();
    private int particleCount;

    public void add(
        SingleQuadParticle.Layer layer,
        float x,
        float y,
        float z,
        float width,
        float height,
        float rotationX,
        float rotationY,
        float rotationZ,
        float rotationW,
        float size,
        float minU,
        float maxU,
        float minV,
        float maxV,
        int color,
        int brightness
    ) {
        this.particles.computeIfAbsent(layer, ignored -> new TextureRenderState.Storage()).add(x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness);
        this.particleCount++;
    }

    @Override
    public void clear() {
        this.particles.values().forEach(Storage::clear);
        this.particleCount = 0;
    }

    public boolean isEmpty() {
        return this.particleCount == 0;
    }

    public void buildLayer(final SingleQuadParticle.Layer layer, final VertexConsumer bufferBuilder) {
        TextureRenderState.Storage storage = (TextureRenderState.Storage)this.particles.get(layer);
        ToroHealth.LOGGER.info("rendering texture");
        if (storage != null) {
            storage.forEachParticle((x, y, z, width, height, xRot, yRot, zRot, wRot, scale, u0, u1, v0, v1, color, lightCoords) -> this.renderRotatedTexture(bufferBuilder, x, y, z, width, height, xRot, yRot, zRot, wRot, scale, u0, u1, v0, v1, color, lightCoords));
            ToroHealth.LOGGER.info("rendering texture");
        }
    }

    protected void renderRotatedTexture(
        VertexConsumer vertexConsumer,
        float x,
        float y,
        float z,
        float width,
        float height,
        float rotationX,
        float rotationY,
        float rotationZ,
        float rotationW,
        float size,
        float minU,
        float maxU,
        float minV,
        float maxV,
        int color,
        int brightness
    ) {
        Quaternionf quaternionf = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, width, 0.0f, size, maxU, maxV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, width, height, size, maxU, minV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, 0.0f, height, size, minU, minV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, 0.0f, 0.0f, size, minU, maxV, color, brightness);
    }

    private void renderVertex(
        VertexConsumer vertexConsumer,
        Quaternionf rotation,
        float x,
        float y,
        float z,
        float localX,
        float localY,
        float size,
        float maxU,
        float maxV,
        int color,
        int brightness
    ) {
        Vector3f vector3f = new Vector3f(localX, localY, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(maxU, maxV).setColor(color).setLight(brightness);
    }

    @Override
    public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (this.particleCount > 0) {
            submitNodeCollector.submitQuadParticleGroup(this);
        }
    }


    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface Consumer {
        void consume(
            float x,
            float y,
            float z,
            float width,
            float height,
            float rotationX,
            float rotationY,
            float rotationZ,
            float rotationW,
            float size,
            float minU,
            float maxU,
            float minV,
            float maxV,
            int color,
            int brightness
        );
    }

    @Environment(EnvType.CLIENT)
    private static class Storage {
        private int maxVertices = INITIAL_BUFFER_MAX_LENGTH;
        private float[] floatData = new float[BUFFER_FLOAT_FIELDS * INITIAL_BUFFER_MAX_LENGTH];
        private int[] intData = new int[BUFFER_INT_FIELDS * INITIAL_BUFFER_MAX_LENGTH];
        private int nextVertexIndex;

        private Storage() {
        }

        public void add(
            float x,
            float y,
            float z,
            float width,
            float height,
            float rotationX,
            float rotationY,
            float rotationZ,
            float rotationW,
            float size,
            float minU,
            float maxU,
            float minV,
            float maxV,
            int color,
            int brightness
        ) {
            if (this.nextVertexIndex >= this.maxVertices) {
                this.grow();
            }

            int i = this.nextVertexIndex * BUFFER_FLOAT_FIELDS;
            this.floatData[i++] = x;
            this.floatData[i++] = y;
            this.floatData[i++] = z;
            this.floatData[i++] = width;
            this.floatData[i++] = height;
            this.floatData[i++] = rotationX;
            this.floatData[i++] = rotationY;
            this.floatData[i++] = rotationZ;
            this.floatData[i++] = rotationW;
            this.floatData[i++] = size;
            this.floatData[i++] = minU;
            this.floatData[i++] = maxU;
            this.floatData[i++] = minV;
            this.floatData[i] = maxV;
            i = this.nextVertexIndex * BUFFER_INT_FIELDS;
            this.intData[i++] = color;
            this.intData[i] = brightness;
            this.nextVertexIndex++;
        }

        public void forEachParticle(TextureRenderState.Consumer vertexConsumer) {
            for (int i = 0; i < this.nextVertexIndex; i++) {
                int j = i * BUFFER_FLOAT_FIELDS;
                int k = i * BUFFER_INT_FIELDS;
                vertexConsumer.consume(
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j],
                    this.intData[k++],
                    this.intData[k]
                );
            }
        }

        public void clear() {
            this.nextVertexIndex = 0;
        }

        private void grow() {
            this.maxVertices *= 2;
            this.floatData = Arrays.copyOf(this.floatData, this.maxVertices * BUFFER_FLOAT_FIELDS);
            this.intData = Arrays.copyOf(this.intData, this.maxVertices * BUFFER_INT_FIELDS);
        }

        public int count() {
            return this.nextVertexIndex;
        }
    }
}
