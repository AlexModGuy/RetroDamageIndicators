package com.github.alexmodguy.retrodamageindicators.mixins;

import com.github.alexmodguy.retrodamageindicators.Config;
import com.github.alexmodguy.retrodamageindicators.RetroDamageIndicators;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow @Final private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract float getHealth();

    private float lastTrackedHealth = 0;

    @Inject(
            method = {"Lnet/minecraft/world/entity/LivingEntity;onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V"},
            remap = true,
            at = @At(value = "HEAD")
    )
    public void retroDamageIndicators_onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor, CallbackInfo ci) {
        if(entityDataAccessor.equals(DATA_HEALTH_ID)){
            if(level().isClientSide && Config.INSTANCE.damageParticlesEnabled.get() && lastTrackedHealth != this.getHealth()){
                float difference = this.getHealth() - lastTrackedHealth;
                RetroDamageIndicators.spawnHurtParticles(this, difference);
                lastTrackedHealth = this.getHealth();
            }
        }
    }
}
