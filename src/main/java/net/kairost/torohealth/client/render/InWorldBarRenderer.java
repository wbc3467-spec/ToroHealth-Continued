package net.kairost.torohealth.client.render;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.util.Mth;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;
import net.kairost.torohealth.config.ToroHealthConfig;

public class InWorldBarRenderer {
    private static final int DARK_GRAY = 0xFF808080;
    private static final float SIZE = 0.025f;
    private static final int BAR_WIDTH = 40;
    private static final Identifier IN_WORLD_BAR = Identifier.fromNamespaceAndPath(ToroHealth.MODID, "in_world_bar");
    private static final TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES).getSprite(IN_WORLD_BAR);
    private static final TextureRenderState submittable = new TextureRenderState();

    public static void render(Entity entity, Camera camera, float tickDelta, int light, EntityRenderDispatcher entityRenderManager) {
        if (!shouldRender(entity, entityRenderManager)) {
            return;
        }

        Vec3 cameraPos = camera.position();
        EntityRenderState entityRenderState = entityRenderManager.extractEntity(entity, tickDelta);
        EntityRenderer<Entity, EntityRenderState> entityRenderer = (EntityRenderer<Entity, EntityRenderState>) entityRenderManager.getRenderer(entityRenderState);
        Vec3 vec3d = entityRenderer.getRenderOffset(entityRenderState);
        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX()) - cameraPos.x + vec3d.x();
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY()) - cameraPos.y + vec3d.y();
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ()) - cameraPos.z + vec3d.z();
        Vec3 labelPos = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(tickDelta));
        if (labelPos == null) {
            float f = entity.getBbHeight();
            labelPos = new Vec3(0.0, f, 0.0);
        }
        Vector3f vector3f = new Vector3f((float) -BAR_WIDTH / 2, -5, 0.0F).rotate(camera.rotation()).mul(SIZE).add((float) (x + labelPos.x), (float) (y + labelPos.y + 0.7), (float) (z + labelPos.z));

        renderHealthBar((LivingEntity)entity, vector3f.x, vector3f.y, vector3f.z, new Quaternionf(camera.rotation()), light, tickDelta);
    }

    private static void renderHealthBar(LivingEntity entity, float x, float y, float z, Quaternionf quaternionf,int light, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        Relation relation = EntityUtil.getRelation(entity);
        int color = relation.equals(Relation.FOE) ? ToroHealthConfig.CONFIG.barColor.foeColor.get() : ToroHealthConfig.CONFIG.barColor.friendColor.get();
        int color2 = relation.equals(Relation.FOE) ? ToroHealthConfig.CONFIG.barColor.foeColorSecondary.get() : ToroHealthConfig.CONFIG.barColor.friendColorSecondary.get();
        color = color | (0xFF << 24);
        color2 = color2 | (0xFF << 24);
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(Mth.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();

        int width = Math.min(Mth.ceil(percent * 41.0f), BAR_WIDTH);
        int width2 = Math.min(Mth.ceil(percent2 * 41.0f), BAR_WIDTH);

        Vector3f shift = new Vector3f(0f, 0f, 0.1f).rotate(quaternionf).mul(SIZE);

        if (40 > width && 40 > width2) {
            renderBar(x, y, z, DARK_GRAY, BAR_WIDTH, light, quaternionf);
        }
        if (width2 > width) {
            renderBar(x + shift.x, y + shift.y, z + shift.z, color2, width2, light, quaternionf);
        }
        if (width > 0) {
            renderBar(x + 2 * shift.x, y + 2 * shift.y, z + 2 * shift.z, color, width, light, quaternionf);
        }
    }


    private static void renderBar(float x, float y, float z, int color, int width, int light, Quaternionf rotation) {
        float u1 = sprite.getU0();
        float u2 = Mth.lerp((float) width / BAR_WIDTH, sprite.getU0(), sprite.getU1());
        float v1 = sprite.getV0();
        float v2 = sprite.getV1();

        submittable.add(SingleQuadParticle.Layer.TRANSLUCENT, x, y, z, (float) width, 5f, rotation.x, rotation.y, rotation.z, rotation.w, SIZE, u1, u2, v1, v2, color, light);
    }


    private static boolean shouldRender(Entity entity, EntityRenderDispatcher entityRenderManager) {
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.get().equals(ToroHealthConfig.InWorldBarVisibilityMode.NONE)) {
            return false;
        }
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.get().equals(ToroHealthConfig.InWorldBarVisibilityMode.WHEN_HOLDING_WEAPON) && !ToroHealth.isHoldingWeapon()) {
            return false;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        if (entityRenderManager.distanceToSqr(entity) > ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarDistanceSquared) {
            return false;
        }
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.onlyWhenHurt.get() && livingEntity.getHealth() >= livingEntity.getMaxHealth()) {
            return false;
        }
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.onlyWhenLookingAt.get() && ToroHealth.getTargetedEntity() != entity) {
            return false;
        }
        return EntityUtil.showHealthBar(entity, Minecraft.getInstance().player);
    }

    public static TextureRenderState getSubmittable() {
        return submittable;
    }
}
