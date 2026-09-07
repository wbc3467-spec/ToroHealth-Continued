package net.kairost.torohealth;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import org.jetbrains.annotations.Nullable;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.kairost.torohealth.config.ModConfig;
import net.kairost.torohealth.client.gui.ToroHealthHud;
import net.kairost.torohealth.client.particle.HealthChangeParticle;
import net.kairost.torohealth.client.particle.TextParticleRenderer;
import net.kairost.torohealth.client.util.HoldingWeaponUpdater;


public class ToroHealth implements ClientModInitializer {
    public static final String MODID = "torohealth";
    public static final SimpleParticleType HEALTH_CHANGE = FabricParticleTypes.simple();
    private static ModConfig config;
    public static ToroHealthHud toroHealthHud = null;
    public static TextParticleRenderer textParticleRenderer;
    private static boolean holdingWeapon = false;
    private static LivingEntity targetedEntity;
    @Override
    public void onInitializeClient() {
        // set config
        ModConfig.init();
        // toroHealthHud
        toroHealthHud = new ToroHealthHud(Minecraft.getInstance());
        // textParticleRenderer
        textParticleRenderer = new TextParticleRenderer(Minecraft.getInstance());

        ConfigHolder<ModConfig> holder =
            AutoConfig.getConfigHolder(ModConfig.class);

        holder.registerSaveListener((h, c) -> {
            c.postLoad();
            return InteractionResult.SUCCESS;
        });

        config = ModConfig.INSTANCE;

        //toroHealth Particle
        Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            Identifier.fromNamespaceAndPath(MODID, "health_change"),
            HEALTH_CHANGE
        );

        ParticleProviderRegistry.getInstance().register(
            HEALTH_CHANGE,
            HealthChangeParticle.HealthChangeFactory::new
        );

        //tick update logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }
            HoldingWeaponUpdater.update();
            ToroHealth.toroHealthHud.tick();
        });

        //hud
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath("kairost", "torohealth_hud_overlay"), (context, tickCounter) -> {
            if (config.enabled && config.hudOptions.showHUD) {
                toroHealthHud.render(context, tickCounter);
            }
            }
        );
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static @Nullable LivingEntity getTargetedEntity() {
        return targetedEntity;
    }

    public static void setTargetedEntity(@Nullable LivingEntity entity) {
        targetedEntity = entity;
    }

    public static void setHoldingWeapon(boolean bl) {
        holdingWeapon = bl;
    }

    public static boolean isHoldingWeapon() {
        return holdingWeapon;
    }
}
