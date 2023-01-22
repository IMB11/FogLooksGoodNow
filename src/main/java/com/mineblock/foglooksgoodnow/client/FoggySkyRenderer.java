package com.mineblock.foglooksgoodnow.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;

public class FoggySkyRenderer {
    public static void renderSky(ClientLevel level, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (FogManager.shouldRenderCaveFog()) {
            FogManager densityManager = FogManager.getDensityManager();
            
            Vec3 fogColor = FogManager.getCaveFogColor();
            float darkness = densityManager.darkness.get(partialTick);
            float undergroundFactor = 1 - Mth.lerp(darkness, densityManager.getUndergroundFactor(partialTick), 1.0F);
            undergroundFactor *= undergroundFactor * undergroundFactor * undergroundFactor;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Gets the current fog color, simply based off the biome.
            float timeOfDay = Mth.clamp(Mth.cos(level.getTimeOfDay(partialTick) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
            BiomeManager biomemanager = level.getBiomeManager();
            Vec3 samplePos = camera.getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
            Vec3 skyFogColor = CubicSampler.gaussianSampleVec3(samplePos, (x, y, z) -> level.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(x, y, z).value().getFogColor()), timeOfDay));
            
            float radius = 5.0F;
            renderCone(poseStack, bufferbuilder, 32, true, radius, -30.0F, 
                    (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor,
                    0.0F, (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor);
            renderCone(poseStack, bufferbuilder, 32, false, radius, 30.0F, 
                    (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor * 0.2F,
                    0.0F, (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor);

            RenderSystem.depthMask(true);
        }
    }

    private static void renderCone(PoseStack poseStack, BufferBuilder bufferBuilder, int resolution, boolean normal, float radius, float topVertexHeight, float topR, float topG, float topB, float topA, float bottomVertexHeight, float bottomR, float bottomG, float bottomB, float bottomA) {
        Matrix4f matrix = poseStack.last().pose();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, 0.0F, topVertexHeight, 0.0F).color(topR, topG, topB, topA).endVertex();
        for(int vertex = 0; vertex <= resolution; ++vertex) {
            float angle = (float)vertex * ((float)Math.PI * 2F) / ((float)resolution);
            float x = Mth.sin(angle) * radius;
            float z = Mth.cos(angle) * radius;

            bufferBuilder.vertex(matrix, x, bottomVertexHeight, normal ? z : -z).color(bottomR, bottomG, bottomB, bottomA).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}