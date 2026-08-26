package net.kairost.torohealth.client.gui;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ModConfig.FrameStyle;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;
import net.kairost.torohealth.mixin.accessor.WitherEntityAccessor;

public class ToroHealthHud {
    public static final Identifier CONTAINER = Identifier.fromNamespaceAndPath("minecraft", "hud/heart/container");
    public static final Identifier FULL = Identifier.fromNamespaceAndPath("minecraft", "hud/heart/full");
    private static final Identifier ARMOR_FULL = Identifier.fromNamespaceAndPath("minecraft", "hud/armor_full");
    private static final Identifier TOROHEALTH_BARS_TEXTURE = Identifier.fromNamespaceAndPath(ToroHealth.MODID, "textures/gui/bars.png");
    private static final Identifier TOROHEALTH_FRAME_TEXTURE = Identifier.fromNamespaceAndPath(ToroHealth.MODID, "textures/gui/frame.png");
    private static final int DARK_GRAY = 0x808080;
    private static final int LIGHT_GRAY = 0xe0e0e0;
    private static final int FRAME_SIZE = 42;
    private static final float ENTITY_RENDER_HEIGHT = 32f;
    private static final float ENTITY_RENDER_WIDTH = 24f;
    private static final float ENTITY_RENDER_SCALE = 32f;
    private static final int INFO_Y_BASE = 2;
    private static final int INFO_X_BASE = 2;
    private static final int INFO_SPACING = 4;
    private static final int BAR_Y = 12;
    private static final int BAR_SIZE = 130;
    private static final int HEALTH_CHANGE_Y = 18;
    private final Minecraft client;
    private LivingEntity entity;
    private int age;
    private float entityX;
    private float entityY;
    private float entityScale;
    private boolean at_left;
    private boolean at_top;

    public ToroHealthHud(Minecraft client) {
        this.client = client;
    }

    public void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        if (!ToroHealth.getConfig().enabled || !ToroHealth.getConfig().hudOptions.showHUD) {
            return;
        }
        if (entity == null) {
            return;
        }
        if (entity.isRemoved()) {
            return;
        }
        if (client.player == null || !EntityUtil.showHealthBar(entity, client.player)) {
            return;
        }
        if (ToroHealth.getConfig().hudOptions.onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }

        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
        context.pose().pushMatrix();
        float x = determineX();
        float y = determineY();
        int scale = ToroHealth.getConfig().hudOptions.hudScale;
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        if (ToroHealth.getConfig().hudOptions.showEntity) {
            this.renderFrame(context);
            context.pose().translate((this.at_left ? 1 : -1) * (FRAME_SIZE + 2), (this.at_top ? 1 : -1) * (INFO_Y_BASE + (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.HEAVY)? 2 : 0)));

        }

        // draw entity info
        this.renderInfo(context, tickDelta);
        context.pose().popMatrix();

        if (ToroHealth.getConfig().hudOptions.showEntity) {
            drawEntity(context, (int) (x + scale * (this.entityX - 2 * FRAME_SIZE + (this.at_left ? 0 : -FRAME_SIZE))), (int) (y + scale * (this.entityY - 2 * FRAME_SIZE + (this.at_top ? 0 : -FRAME_SIZE))), (int) (x + scale * (this.entityX + 2 * FRAME_SIZE + (this.at_left ? 0 : -FRAME_SIZE))), (int) (y + scale * (this.entityY + 2 * FRAME_SIZE + (this.at_top ? 0 : -FRAME_SIZE))), this.entityScale * scale, 0, (this.at_left ? -80 : 80), -20, entity, tickDelta);
        }
    }

    private float determineX() {
        float x = ToroHealth.getConfig().hudOptions.hudXPosition;
        float wScreen = this.client.getWindow().getGuiScaledWidth();

        return switch (ToroHealth.getConfig().hudOptions.anchorPoint) {
            case BOTTOM_LEFT, TOP_LEFT: {
                this.at_left = true;
                yield x;
            }
            case BOTTOM_RIGHT, TOP_RIGHT: {
                this.at_left = false;
                yield wScreen + x;
            }
        };
    }

    private float determineY() {
        float y = ToroHealth.getConfig().hudOptions.hudYPosition;
        float hScreen = client.getWindow().getGuiScaledHeight();

        return switch (ToroHealth.getConfig().hudOptions.anchorPoint) {
            case TOP_LEFT, TOP_RIGHT: {
                this.at_top = true;
                yield y;
            }
            case BOTTOM_LEFT, BOTTOM_RIGHT: {
                this.at_top = false;
                yield hScreen + y;
            }
        };
    }

    public void tick()  {
        if (this.entity != null) {
            setEntityRenderPos();
            age++;
        }
    }

    public void setEntity(LivingEntity entity) {
        if (entity != null) {
            this.age = 0;
            if (entity != this.entity) {
                setEntityWork(entity);
            }
        }

        if (entity == null && age > ToroHealth.getConfig().hudOptions.hudHideDelay) {
            setEntityWork(null);
        }
    }

    private void setEntityWork(LivingEntity entity)  {
        this.entity = entity;
        if  (entity !=  null) {
            if (entity instanceof EnderDragon) {
                this.entityScale = 2 * ENTITY_RENDER_HEIGHT / entity.getBbHeight();
            }
            else {
                float scale = this.entity.getAgeScale();
                float height = this.entity.getBbHeight() / scale;
                float width = this.entity.getBbWidth() / scale;
                this.entityScale = Math.min(ENTITY_RENDER_HEIGHT / height, ENTITY_RENDER_WIDTH / width);

                // restrict entity scale
                if (this.entityScale > ENTITY_RENDER_SCALE) {
                    this.entityScale = ENTITY_RENDER_SCALE;
                }
                else if (this.entityScale > ENTITY_RENDER_SCALE / 2) {
                    // unchange
                }
                else if (this.entityScale > ENTITY_RENDER_SCALE / 2.5 ) {
                    this.entityScale = ENTITY_RENDER_SCALE / 2;
                }
                else {
                    this.entityScale = 5 * this.entityScale / 4;
                }
            }

            setEntityRenderPos();
        }
        else {
            this.entityScale = 0;
            this.entityX = 0;
            this.entityY = 0;
        }
    }


    private void setEntityRenderPos() {
        assert this.entity != null;
        if (this.entityX == 0) {
            this.entityX = (float) FRAME_SIZE / 2;
        }
        if (this.entityY == 0) {
            // default
            this.entityY = (float) FRAME_SIZE / 2 + ENTITY_RENDER_HEIGHT / 2;
        }
        if (this.entity instanceof Ghast) {
            this.entityY = (float) FRAME_SIZE / 2 + entity.getBbHeight() * entityScale * 3 / 8 ;
        }
        else if (this.entity instanceof EnderDragon) {
            this.entityY = (float) FRAME_SIZE / 2 + entity.getBbHeight() * this.entityScale / 4;
        }
        else if (this.entity instanceof Shulker shulker) {
            switch (shulker.getAttachFace()){
                case DOWN:
                    this.entityY = (float) FRAME_SIZE / 2 + ENTITY_RENDER_HEIGHT / 2;
                    break;
                case UP:
                    this.entityY = (float) FRAME_SIZE / 2 - ENTITY_RENDER_HEIGHT / 2 + entity.getBbHeight() * entityScale;
                    break;
                case NORTH, SOUTH, EAST, WEST:
                    this.entityY = (float) FRAME_SIZE / 2 + entity.getBbHeight() * this.entityScale / 2;
                    break;
            }
        }
        else if (this.entity instanceof Villager villager && villager.isSleeping())
            this.entityY = (float) FRAME_SIZE / 2 + entity.getBbHeight() * this.entityScale / 2;
        else if (this.entity instanceof Player player && player.isSleeping())
            this.entityY = (float) FRAME_SIZE / 2 + entity.getBbHeight() * this.entityScale / 2;
        else if (this.entity instanceof Bat bat && !bat.isResting())
            this.entityY = (float) FRAME_SIZE / 2 - ENTITY_RENDER_HEIGHT / 2 + entity.getBbHeight() * entityScale;
        else if (this.entity instanceof Spider spider && spider.onClimbable())
            this.entityY = (float) FRAME_SIZE / 2 + this.entity.getBbHeight() * this.entityScale / 2;
        else if (this.entity.isPassenger() || EntityUtil.isFloating(this.entity) || this.entity.hasEffect(MobEffects.LEVITATION))
            this.entityY = (float) FRAME_SIZE / 2 + entity.getBbHeight() * this.entityScale / 2;
        else if (this.entity.onGround())
            this.entityY = (float) FRAME_SIZE / 2 + ENTITY_RENDER_HEIGHT / 2;
    }


    private void renderFrame(GuiGraphicsExtractor context) {
        boolean light_style = (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.LIGHT));
        int h = 42;
        int w = light_style ? 42 : 179;
        int x = this.at_left ? 0 : -w;
        int y = this.at_top ? 0 : -h;
        int u = light_style ? 0 : 42;
        int v = (this.at_top ? 0 : 42) + (this.at_left ? 0 : 84);
        context.blit(RenderPipelines.GUI_TEXTURED, TOROHEALTH_FRAME_TEXTURE, x, y, u, v, w, h, 256, 256);
    }


    private void renderInfo(GuiGraphicsExtractor context, float tickDelta) {
        // render bar
        this.renderHealthBar(context, this.entity, (this.at_left ? 0 : -BAR_SIZE), (this.at_top ? BAR_Y : -(BAR_Y + 5)), tickDelta);
        int x_pos_scalar = this.at_left ? 1 : -1;
        int y_pos_scalar = this.at_top ? 1 : -1;

        int xOffset = x_pos_scalar * INFO_X_BASE;

        // name
        String name = this.entity.getDisplayName().getString();
        context.text(this.client.font, name, xOffset + (this.at_left ? 0 : -this.client.font.width(name)), (this.at_top ? 1 : -8), 0xFFFFFFFF);
        xOffset += x_pos_scalar * (this.client.font.width(name) + INFO_SPACING);


        // health
        int healthMax = Mth.ceil(this.entity.getMaxHealth());
        int healthCurrent = Mth.clamp(
            Mth.ceil(this.entity.getHealth()),
            0,
            healthMax
        );
        String healthText = healthCurrent + "/" + healthMax;
        int healthTextY = this.at_top ? 1 : -8;
        if (this.at_left) {
            renderHeartIcon(context, xOffset, healthTextY - 1);

            xOffset += 10;

            context.text(this.client.font, healthText, xOffset, healthTextY, 0xFFFFFFFF);
            xOffset +=this.client.font.width(healthText) + INFO_SPACING;
        } else {
            context.text(this.client.font, healthText, xOffset - this.client.font.width(healthText), healthTextY, 0xFFFFFFFF);

            xOffset -= (this.client.font.width(healthText) + 1);

            renderHeartIcon(context, xOffset - 9, (healthTextY - 1));
            xOffset -= (9 + INFO_SPACING);
        }

        // armor
        int armor = this.entity.getArmorValue();
        if (armor > 0) {
            String armorText = Integer.toString(armor);
            int armorTextY = this.at_top ? 1 : -8;
            if (this.at_left) {
                renderArmorIcon(context, xOffset, armorTextY - 1);
                xOffset += 10;
                context.text(this.client.font, armorText, xOffset, armorTextY, 0xFFFFFFFF);
            } else {
                context.text(this.client.font, armorText, xOffset - this.client.font.width(armorText), armorTextY, 0xFFFFFFFF);
                xOffset -= (this.client.font.width(Integer.toString(entity.getArmorValue())) + 1);
                renderArmorIcon(context, xOffset - 9, armorTextY - 1);
            }
        }

        // render health change
        this.renderHealthChangeText(context, entity, (this.at_left ? 1 : -1) * BAR_SIZE, y_pos_scalar * HEALTH_CHANGE_Y);
    }

    private void renderHeartIcon(GuiGraphicsExtractor context, int x, int y) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, CONTAINER, x, y, 9, 9);
        context.blitSprite(RenderPipelines.GUI_TEXTURED, FULL, x, y, 9, 9);
    }

    private void renderArmorIcon(GuiGraphicsExtractor context, int x, int y) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_FULL, x, y, 9, 9);
    }

    private void renderHealthChangeText(GuiGraphicsExtractor context, LivingEntity entity, int x, int y) {
        int healthChange;
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        if (state == null) {
            return;
        }
        healthChange = switch (ToroHealth.getConfig().hudOptions.healthChangeType) {
            case LAST -> state.healthChangeLast;
            case CUMULATIVE -> state.healthChangeCumulate;
            default -> 0;
        };
        int color = (healthChange > 0 ? ToroHealth.getConfig().particleOptions.healColor : ToroHealth.getConfig().particleOptions.damageColor) | 0xFF000000;
        if (healthChange != 0) {
            String text = Integer.toString(Math.abs(healthChange));
            context.text(this.client.font, text, x + (this.at_left ? -this.client.font.width(text) : 2), (this.at_top ? y : (y - 8)), color);
        }
    }

    // draw a health Bar composed of 3 layers in InGameHud.
    private void renderHealthBar(GuiGraphicsExtractor context, LivingEntity entity, int x, int y, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        if (state == null) {
            return;
        }
        EntityUtil.Relation relation = EntityUtil.getRelation(entity);

        int color = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColor : ToroHealth.getConfig().barColor.friendColor;
        int color2 = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColorSecondary : ToroHealth.getConfig().barColor.friendColorSecondary;
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(Mth.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();
        int width = Mth.ceil(percent * (BAR_SIZE + 1));
        int width2 = Mth.ceil(percent2 * (BAR_SIZE + 1));
        if (BAR_SIZE > width && BAR_SIZE > width2) {
            this.renderBar(context, x, y, BAR_SIZE, DARK_GRAY);
        }
        if (width2 > width) {
            this.renderBar(context, x, y, width2, color2);
        }
        if (width > 0) {
            this.renderBar(context, x, y, width, color);
        }
    }

    // this method draws a single bar in InGameHud
    private void renderBar(GuiGraphicsExtractor context, int x, int y, int width, int color) {
        int color_argb = color | 0xFF000000;
        int shift = this.at_left ? 0 : (130 - width);
        context.blit(RenderPipelines.GUI_TEXTURED, TOROHEALTH_BARS_TEXTURE, x + shift, y, shift, 6 * 2 * 5 + 5, width, 5, 256, 256, color_argb);
    }

    //modified from vanilla InventoryScreen.drawEntity
    private static void drawEntity(
        final GuiGraphicsExtractor graphics,
        final int x0,
        final int y0,
        final int x1,
        final int y1,
        final float size,
        final float offsetY,
        final float mouseX,
        final float mouseY,
        final LivingEntity entity,
        float tickDelta
    ) {
        float xAngle = (float)Math.atan(mouseX / 40.0F);
        float yAngle = (float)Math.atan(mouseY / 40.0F);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);
        EntityRenderState renderState;
        if (entity instanceof WitherBoss wither) {
            WitherEntityAccessor witherEntityAccessor = (WitherEntityAccessor) wither;
            float[] sideHeadYaws = witherEntityAccessor.torohealth$getSideHeadYaws();
            float[] lastSideHeadYaws = witherEntityAccessor.torohealth$getLastSideHeadYaws();
            float i = entity.yBodyRot;
            float[] m = sideHeadYaws.clone();
            float[] n = lastSideHeadYaws.clone();
            sideHeadYaws[0] = 180.0f + xAngle * 20.0f + sideHeadYaws[0] - i;
            sideHeadYaws[1] = 180.0f + xAngle * 20.0f + sideHeadYaws[1] - i;
            lastSideHeadYaws[0] = 180.0f + xAngle * 20.0f + lastSideHeadYaws[0] - i;
            lastSideHeadYaws[1] = 180.0f + xAngle * 20.0f + lastSideHeadYaws[1] - i;
            renderState = extractRenderState(entity, tickDelta);
            System.arraycopy(m, 0, sideHeadYaws, 0, m.length);
            System.arraycopy(n, 0, lastSideHeadYaws, 0, n.length);
        } else if (entity instanceof EnderDragon enderDragon) {
            EndCrystal endCrystal = enderDragon.nearestCrystal;
            enderDragon.nearestCrystal = null;
            renderState = extractRenderState(entity, tickDelta);
            enderDragon.nearestCrystal = endCrystal;
        } else {
            renderState = extractRenderState(entity, tickDelta);
        }

        if (renderState instanceof LivingEntityRenderState livingRenderState) {
            livingRenderState.bodyRot = 180.0F + xAngle * 20.0F;
            if (entity instanceof AbstractNautilus) {
                livingRenderState.bodyRot += 180.0F;
            }

            livingRenderState.boundingBoxWidth = livingRenderState.boundingBoxWidth / livingRenderState.scale;
            livingRenderState.boundingBoxHeight = livingRenderState.boundingBoxHeight / livingRenderState.scale;
            livingRenderState.scale = 1.0F;
        }

        Vector3f translation = new Vector3f(0.0F, 0.0F, 0.0F);
        graphics.entity(renderState, size, translation, rotation, xRotation, x0, y0, x1, y1);
    }

    //copied from InventoryScreen.extractRenderState
    private static EntityRenderState extractRenderState(final LivingEntity entity, float tickDelta) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, tickDelta);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }
}
