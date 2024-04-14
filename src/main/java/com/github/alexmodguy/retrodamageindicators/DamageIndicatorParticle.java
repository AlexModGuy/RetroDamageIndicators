package com.github.alexmodguy.retrodamageindicators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Optional;

public class DamageIndicatorParticle extends Particle {

    private static final ParticleGroup GROUP = new ParticleGroup(1000);
    private final Component damageString;
    private final boolean heal;
    private float scale;
    private float prevScale;

    protected DamageIndicatorParticle(ClientLevel clientLevel, double x, double y, double z, double damageAmount, boolean heal) {
        super(clientLevel, x, y, z);
        this.lifetime = 15 + clientLevel.random.nextInt(5);
        String text;
        if(Config.INSTANCE.healthDecimals.get()){
            text = String.valueOf(RetroDamageIndicators.roundHealth((float) damageAmount)).replace(".0", "");
        }else{
            text = "" + (int)damageAmount;
        }
        this.damageString = Component.literal(text);
        this.heal = heal;
        this.scale = 1.0F;
        this.yd = 0.2F + Math.random() * 0.2F;
        this.gravity = 1.3F;
    }

    @Override
    public void tick(){
        super.tick();
        float ageScaled = age / (float) lifetime;
        this.prevScale = scale;
        this.scale = 1.0F - ageScaled;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = camera.getPosition();
        double x = (float) (Mth.lerp((double) partialTicks, this.xo, this.x));
        double y = (float) (Mth.lerp((double) partialTicks, this.yo, this.y));
        double z = (float) (Mth.lerp((double) partialTicks, this.zo, this.z));
        int color = heal ? 0X00FF00 : 0XFF0000;
        int colorOutline = heal ? 0X003300 : 0X330000;
        float scale = this.getScale(partialTicks) * 0.035F;
        PoseStack posestack = new PoseStack();
        posestack.pushPose();
        posestack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
        posestack.mulPose(camera.rotation());
        posestack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        float f = (float)(-Minecraft.getInstance().font.width(damageString) / 2);
        posestack.scale(scale, scale, scale);
        posestack.translate(0.0F, -2.0F, 0.0F);
        if(Config.INSTANCE.damageParticleOutline.get()){
            Minecraft.getInstance().font.drawInBatch8xOutline(damageString.getVisualOrderText(), f, 0.0F, color, colorOutline, posestack.last().pose(), multibuffersource$buffersource, 15728880);
        }else{
            Minecraft.getInstance().font.drawInBatch(damageString.getVisualOrderText(), f, 0.0F, color, false, posestack.last().pose(), multibuffersource$buffersource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
        multibuffersource$buffersource.endBatch();
        posestack.popPose();
    }

    private float getScale(float partialTicks) {
        return prevScale + (scale - prevScale) * partialTicks;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.of(GROUP);
    }
}
