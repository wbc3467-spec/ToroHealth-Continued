package net.kairost.torohealth.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {

    public enum Relation {
        FRIEND, FOE, UNKNOWN
    }

    public static Relation getRelation(Entity entity) {
        if (entity instanceof Enemy) {
            return Relation.FOE;
        } else if (entity instanceof AgeableMob) {
            return Relation.FRIEND;
        } else if (entity instanceof WaterAnimal) {
            return Relation.FRIEND;
        } else if (entity instanceof AmbientCreature) {
            return Relation.FRIEND;
        } else {
            return Relation.UNKNOWN;
        }
    }

    public static boolean showHealthBar(Entity entity, Player player) {
        return entity instanceof LivingEntity
            && !(entity instanceof ArmorStand)
            && entity != player
            && !entity.isVehicle()
            && isDetectable(entity, player);
    }

    public static boolean isDetectable(Entity entity, Player player) {
        return !entity.isInvisible()
            && !entity.isInvisibleTo(player)
            && !entity.isSpectator();
    }

    public static boolean isFloating(LivingEntity entity) {
        if (entity.isInWater() && !entity.onGround())
            return true;

        // air, FlyingEntity
        if (entity instanceof Parrot parrot && parrot.isFlying())
            return true;

        if (entity instanceof Bat bat && bat.isResting())
            return true;

        if (entity instanceof Phantom || entity instanceof Bee || entity instanceof Vex || entity instanceof Allay || entity instanceof Ghast || entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof HappyGhast)
            return true;

        return false;
    }

    public static double getSquaredDistanceToCamera(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            Vec3 vec3d = player.getEyePosition(0f);
            return entity.position().distanceToSqr(vec3d);
        } else {
            return 0d;
        }
    }
}
