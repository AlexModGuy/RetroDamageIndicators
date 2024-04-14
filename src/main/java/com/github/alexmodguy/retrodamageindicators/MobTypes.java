package com.github.alexmodguy.retrodamageindicators;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeEntityTypeTagsProvider;

import java.util.Locale;

public enum MobTypes {
    PLAYER,
    UNDEAD,
    UNDEAD_ANIMAL,
    ANIMAL,
    WATER_ANIMAL,
    AMBIENT,
    MONSTER,
    WATER_MONSTER,
    ARTHROPOD,
    ARTHROPOD_MONSTER,
    WATER_ARTHROPOD,
    GOLEM,
    VILLAGER,
    ILLAGER,
    UNKNOWN,
    BOSS;

    private final ResourceLocation texture;
    MobTypes(){
        texture = new ResourceLocation("retrodamageindicators:textures/gui/mob_types/" + name().toLowerCase(Locale.ROOT) + ".png");
    }

    public static MobTypes getTypeFor(Entity entity){
        if(entity instanceof Player){
            return PLAYER;
        }
        if(entity instanceof LivingEntity living){
            if(living.getType().is(Tags.EntityTypes.BOSSES)){
                return BOSS;
            }
            if(living.getMobType() == MobType.WATER){
                return living instanceof Enemy ? WATER_MONSTER : WATER_ANIMAL;
            }else if(living.getMobType() == MobType.UNDEAD){
                return living instanceof Enemy ? UNDEAD : UNDEAD_ANIMAL;
            }else if(living.getMobType() == MobType.ARTHROPOD){
                return living instanceof WaterAnimal || living.canBreatheUnderwater() ? WATER_ARTHROPOD : living instanceof Enemy ? ARTHROPOD_MONSTER : ARTHROPOD;
            }else if(living.getMobType() == MobType.ILLAGER){
                return ILLAGER;
            }
            if(living instanceof AbstractGolem){
                return GOLEM;
            }
            if(living instanceof Npc){
                return VILLAGER;
            }
            if(living instanceof Enemy){
                return MONSTER;
            }
            if(living instanceof AmbientCreature){
                return AMBIENT;
            }
            if(living instanceof Mob){
                return ANIMAL;
            }
        }
        return UNKNOWN;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
