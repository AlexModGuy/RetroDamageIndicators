package com.github.alexmodguy.retrodamageindicators;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = "retrodamageindicators", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RetroDamageIndicators {
    public static final String MODID = "retrodamageindicators";
    private static final ResourceLocation DAMAGE_INDICATOR_TEXTURE = new ResourceLocation(MODID, "textures/gui/damage_indicator.png");
    private static final ResourceLocation DAMAGE_INDICATOR_BACKGROUND_TEXTURE = new ResourceLocation(MODID, "textures/gui/damage_indicator_background.png");
    private static final ResourceLocation DAMAGE_INDICATOR_HEALTH_TEXTURE = new ResourceLocation(MODID, "textures/gui/damage_indicator_health.png");
    private static final Quaternionf ENTITY_ROTATION = (new Quaternionf()).rotationXYZ((float) Math.toRadians(30), (float) Math.toRadians(130), (float) Math.PI);
    private static LivingEntity damageIndicatorEntity;
    private static MobTypes currentMobType = MobTypes.UNKNOWN;
    private static int resetDamageIndicatorEntityIn = 0;
    private static boolean renderModelOnly;


    @SubscribeEvent
    public static void onPreRenderGuiElement(RenderGuiOverlayEvent.Pre event) {
        if (Config.INSTANCE.hudIndicatorEnabled.get()) {
            if (event.getOverlay().id().equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id()) && damageIndicatorEntity != null) {
                float entityHealth = Math.min(damageIndicatorEntity.getHealth(), damageIndicatorEntity.getMaxHealth());
                float entityMaxHealth = damageIndicatorEntity.getMaxHealth();
                float healthRatio = entityMaxHealth <= 0.0F ? 0.0F : entityHealth / entityMaxHealth;
                float scale = Config.INSTANCE.hudIndicatorSize.get().floatValue();
                int xOffset = Config.INSTANCE.hudIndicatorAlignLeft.get() ? Config.INSTANCE.hudIndicatorPositionX.get() : event.getWindow().getGuiScaledWidth() - (int) (208 * scale) - Config.INSTANCE.hudIndicatorPositionX.get();
                int yOffset = Config.INSTANCE.hudIndicatorAlignTop.get() ? Config.INSTANCE.hudIndicatorPositionY.get() : event.getWindow().getGuiScaledHeight() - (int) (78 * scale) - Config.INSTANCE.hudIndicatorPositionY.get();
                if (Minecraft.getInstance().gui instanceof ForgeGui forgeGui) {
                    int bossBars = forgeGui.getBossOverlay().events.size();
                    if (Config.INSTANCE.hudIndicatorAlignTop.get()) {
                        if (bossBars > 0) {
                            yOffset += Math.min(event.getWindow().getGuiScaledHeight() / 3, 12 + 19 * bossBars);
                        }
                        if (!Config.INSTANCE.hudIndicatorAlignLeft.get()) {
                            int potionsActive = 0;
                            for (MobEffectInstance mobEffectInstance : Minecraft.getInstance().player.getActiveEffects()) {
                                if (mobEffectInstance.showIcon()) {
                                    potionsActive++;
                                }
                            }
                            yOffset += Math.min(potionsActive, 2) * 24;
                        }
                    }
                }
                float backgroundOpacity = Config.INSTANCE.hudIndicatorBackgroundOpacity.get().floatValue();
                int relativeHealthbarX = 81;
                int relativeHealthbarY = 25;
                int healthbarHeight = 18;
                int healthbarMaxWidth = 124;
                int currentHealthbarWidth = (int) Math.round(healthbarMaxWidth * healthRatio);
                PoseStack poseStack = event.getGuiGraphics().pose();
                poseStack.pushPose();
                poseStack.translate(xOffset, yOffset - 0.5F, 0);
                poseStack.scale(scale, scale, scale);

                // upper half entity render box scissor coords.
                int scissorBox1MinX = 16;
                int scissorBox1MinY = 4;
                int scissorBox1MaxX = 73;
                int scissorBox1MaxY = 49;
                // lower half entity render box scissor coords.
                int scissorBox2MinX = 28;
                int scissorBox2MinY = 49;
                int scissorBox2MaxX = 73;
                int scissorBox2MaxY = 61;
                int entityX = 45;
                int entityY = 56;

                //render the first half of the entity (above y = 49)
                event.getGuiGraphics().enableScissor(xOffset + Math.round(scale * scissorBox1MinX), yOffset + Math.round(scale * scissorBox1MinY), xOffset + Math.round(scale * scissorBox1MaxX), yOffset + Math.round(scale * scissorBox1MaxY));
                if (damageIndicatorEntity != null) {
                    float biggestEntityDimension = Math.max(damageIndicatorEntity.getBbWidth() * 1.2F + 0.3F, damageIndicatorEntity.getBbHeight() * 0.9F) * 0.85F;
                    float renderScale = Config.INSTANCE.hudEntitySize.get().floatValue();
                    if ((double) biggestEntityDimension > 0.5D) {
                        renderScale /= biggestEntityDimension;
                    }
                    renderEntityInGui(event.getGuiGraphics(), entityX, entityY, renderScale, ENTITY_ROTATION, damageIndicatorEntity, event.getPartialTick());
                }
                event.getGuiGraphics().disableScissor();
                //render the second half of the entity (below y = 49)
                event.getGuiGraphics().enableScissor(xOffset + Math.round(scale * scissorBox2MinX), yOffset + Math.round(scale * scissorBox2MinY), xOffset + Math.round(scale * scissorBox2MaxX), yOffset + Math.round(scale * scissorBox2MaxY));
                if (damageIndicatorEntity != null) {
                    float biggestEntityDimension = Math.max(damageIndicatorEntity.getBbWidth() * 1.2F + 0.3F, damageIndicatorEntity.getBbHeight() * 0.9F) * 0.85F;
                    float renderScale = Config.INSTANCE.hudEntitySize.get().floatValue();
                    if ((double) biggestEntityDimension > 0.5D) {
                        renderScale /= biggestEntityDimension;
                    }
                    renderEntityInGui(event.getGuiGraphics(), entityX, entityY, renderScale, ENTITY_ROTATION, damageIndicatorEntity, event.getPartialTick());
                }
                event.getGuiGraphics().disableScissor();

                poseStack.pushPose();
                poseStack.translate(0, 0, -200);

                //background render
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, backgroundOpacity);
                event.getGuiGraphics().blit(DAMAGE_INDICATOR_BACKGROUND_TEXTURE, 0, 0, 50, 0, 0, 208, 78, 256, 256);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);



                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
                //foreground render
                event.getGuiGraphics().blit(DAMAGE_INDICATOR_TEXTURE, 0, 0, 50, 0, 0, 208, 78, 256, 256);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                // mob type render
                int relativeMobTypeX = 5;
                int relativeMobTypeY = 55;
                event.getGuiGraphics().blit(currentMobType.getTexture(), relativeMobTypeX, relativeMobTypeY, 50, 0, 0, 18, 18, 18, 18);

                //health render
                int healthbarVOffset = Config.INSTANCE.colorblindHealthBar.get() ? 36 : 0;
                event.getGuiGraphics().blit(DAMAGE_INDICATOR_HEALTH_TEXTURE, relativeHealthbarX, relativeHealthbarY, 50, 0, healthbarVOffset + 18, healthbarMaxWidth, healthbarHeight, 256, 256);
                event.getGuiGraphics().blit(DAMAGE_INDICATOR_HEALTH_TEXTURE, relativeHealthbarX, relativeHealthbarY, 50, 0, healthbarVOffset, currentHealthbarWidth, healthbarHeight, 256, 256);

                poseStack.popPose();

                //health text
                String healthText;
                float healthOffsetX = 136;
                float healthOffsetY = 30;
                String healthDivisor;
                int firstHalfWidth;
                if (Config.INSTANCE.healthSeperator.get()) {
                    healthDivisor = " | ";
                } else {
                    healthDivisor = "/";
                    healthOffsetX += 4;
                }
                if (Config.INSTANCE.healthDecimals.get()) {
                    healthText = roundHealth(entityHealth) + healthDivisor + roundHealth(entityMaxHealth);
                    firstHalfWidth = Minecraft.getInstance().font.width("" + roundHealth(entityHealth));
                } else {
                    healthText = (int) entityHealth + healthDivisor + (int) entityMaxHealth;
                    firstHalfWidth = Minecraft.getInstance().font.width("" + (int) entityHealth);
                }
                Component healthComponent = Component.literal(healthText);
                int healthWidth = Minecraft.getInstance().font.width(healthComponent);
                float healthScale = Math.min(88F / (float) healthWidth, 1.35F);
                int healthColor = 0XFFFFFF;
                int healthOutlineColor = 0;

                poseStack.pushPose();
                poseStack.translate(healthOffsetX, healthOffsetY, 0);
                poseStack.scale(healthScale, healthScale, 1);
                poseStack.translate(-firstHalfWidth, 0, -50);
                if (Config.INSTANCE.hudHealthTextOutline.get()) {
                    Minecraft.getInstance().font.drawInBatch8xOutline(healthComponent.getVisualOrderText(), 0.0F, 0.0F, healthColor, healthOutlineColor, poseStack.last().pose(), event.getGuiGraphics().bufferSource(), 15728880);
                } else {
                    Minecraft.getInstance().font.drawInBatch(healthComponent.getVisualOrderText(), 0.0F, 0.0F, healthColor, true, poseStack.last().pose(), event.getGuiGraphics().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
                }
                poseStack.popPose();

                //name text
                Component nameComponent = damageIndicatorEntity.getDisplayName();
                int nameWidth = Minecraft.getInstance().font.width(nameComponent);
                float nameScale = Math.min(113F / (float) nameWidth, 1.25F);
                float nameOffsetX = 138.5F;
                float nameOffsetY = 6.5F;
                int nameColor = 0XFFFFFF;
                int nameOutlineColor = 0;

                poseStack.pushPose();
                poseStack.translate(nameOffsetX, nameOffsetY, 0);
                poseStack.scale(nameScale, nameScale, 1);
                poseStack.translate(-nameWidth / 2F, 0, -50);
                if (Config.INSTANCE.hudNameTextOutline.get()) {
                    Minecraft.getInstance().font.drawInBatch8xOutline(nameComponent.getVisualOrderText(), 0.0F, 0.0F, nameColor, nameOutlineColor, poseStack.last().pose(), event.getGuiGraphics().bufferSource(), 15728880);
                } else {
                    Minecraft.getInstance().font.drawInBatch(nameComponent.getVisualOrderText(), 0.0F, 0.0F, nameColor, true, poseStack.last().pose(), event.getGuiGraphics().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
                }
                poseStack.popPose();

                poseStack.popPose();
            }

        }
    }

    public static void renderEntityInGui(GuiGraphics guiGraphics, int xPos, int yPos, float scale, Quaternionf rotation, Entity entity, float partialTicks) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((double) xPos, (double) yPos, -60.0D);
        guiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling(scale, scale, (-scale)));
        guiGraphics.pose().mulPose(rotation);

        Vector3f light0 = new Vector3f(1, -1.0F, -1.0F).normalize();
        Vector3f light1 = new Vector3f(-1, 1.0F, 1.0F).normalize();
        RenderSystem.setShaderLights(light0, light1);
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderdispatcher.setRenderShadow(false);
        if (renderModelOnly && entityrenderdispatcher.getRenderer(entity) instanceof LivingEntityRenderer livingEntityRenderer) {
            guiGraphics.pose().translate(0, 1.5F, 0.0D);
            guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
            RenderType renderType = livingEntityRenderer.getModel().renderType(livingEntityRenderer.getTextureLocation(entity));
            livingEntityRenderer.getModel().renderToBuffer(guiGraphics.pose(), guiGraphics.bufferSource().getBuffer(renderType), 15728880, LivingEntityRenderer.getOverlayCoords((LivingEntity) entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            float f = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
            if (entity instanceof LivingEntity living) {
                float f1 = living.yBodyRotO + (living.yBodyRot - living.yBodyRotO) * partialTicks;
                guiGraphics.pose().mulPose(Axis.YN.rotationDegrees(-f1));
            } else {
                guiGraphics.pose().mulPose(Axis.YN.rotationDegrees(-f));
            }
            RenderSystem.runAsFancy(() -> {
                entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880);
            });
        }
        guiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    public static float roundHealth(float entityHealth) {
        return (float) (Math.round(entityHealth * 5) / 5D);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase == TickEvent.Phase.START && Minecraft.getInstance().cameraEntity != null) {
            double maxPickDistance = Config.INSTANCE.maxDistance.get();
            double pickDistance = maxPickDistance;
            Vec3 vec3 = Minecraft.getInstance().cameraEntity.getEyePosition(Minecraft.getInstance().getPartialTick());
            HitResult hitResult = Minecraft.getInstance().cameraEntity.pick(pickDistance, Minecraft.getInstance().getPartialTick(), false);
            LivingEntity found = null;
            if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                pickDistance = hitResult.getLocation().distanceToSqr(vec3);
            }
            Vec3 vec31 = Minecraft.getInstance().cameraEntity.getViewVector(1.0F);
            Vec3 vec32 = vec3.add(vec31.x * maxPickDistance, vec31.y * maxPickDistance, vec31.z * maxPickDistance);
            AABB aabb = Minecraft.getInstance().cameraEntity.getBoundingBox().expandTowards(vec31.scale(maxPickDistance)).inflate(3.0D, 3.0D, 3.0D);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(Minecraft.getInstance().cameraEntity, vec3, vec32, aabb, (lookingAt) -> {
                return !lookingAt.isSpectator() && lookingAt.isPickable();
            }, pickDistance);
            if (entityhitresult != null) {
                Vec3 vec33 = entityhitresult.getLocation();
                Entity entity = entityhitresult.getEntity();
                double d2 = vec3.distanceToSqr(vec33);
                if (d2 < pickDistance) {
                    if (entity instanceof LivingEntity living && living.isAlive() && !(living instanceof ArmorStand)) {
                        found = (LivingEntity) entity;
                    } else if (entity instanceof PartEntity<?> partEntity && partEntity.getParent() instanceof LivingEntity living) {
                        found = living;
                    }
                }
            }
            if (found != null) {
                damageIndicatorEntity = found;
                currentMobType = MobTypes.getTypeFor(found);
                resetDamageIndicatorEntityIn = Config.INSTANCE.hudLingerTime.get();
                renderModelOnly = Config.INSTANCE.oldRenderEntities.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(found.getType()).toString());
            } else if (resetDamageIndicatorEntityIn-- < 0) {
                damageIndicatorEntity = null;
                resetDamageIndicatorEntityIn = 0;
            }
        }
    }

    public static void spawnHurtParticles(Entity entity, float damage) {
        double x = entity.getRandomX(1.0D);
        double y = entity.getEyeY();
        double z = entity.getRandomZ(1.0D);
        Minecraft.getInstance().particleEngine.add(new DamageIndicatorParticle(Minecraft.getInstance().level, x, y, z, Math.abs(damage), damage > 0));
    }
}