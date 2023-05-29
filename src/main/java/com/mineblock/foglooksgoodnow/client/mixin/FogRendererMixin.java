package com.mineblock.foglooksgoodnow.client.mixin;

import com.mineblock.foglooksgoodnow.client.FogManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Shadow private static float fogRed;

    @Shadow private static float fogGreen;

    @Shadow private static float fogBlue;

    @ModifyArgs(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false))
    private static void modifyFogColors(Args args, Camera camera, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier) {
        if (FogManager.shouldRenderCaveFog()) {
            FogManager densityManager = FogManager.getDensityManager();
            Vec3 fogColor = FogManager.getCaveFogColor();

            float undergroundFactor = 1 - densityManager.getUndergroundFactor(partialTicks);
            fogRed = (float) Mth.lerp(undergroundFactor, fogRed, fogColor.x * fogRed);
            fogGreen = (float) Mth.lerp(undergroundFactor, fogGreen, fogColor.y * fogGreen);
            fogBlue = (float) Mth.lerp(undergroundFactor, fogBlue, fogColor.z * fogBlue);
        }
    }
    @Inject(method = "setupFog", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fogRenderEvent(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float partialTick, CallbackInfo ci, FogType fogType, Entity entity, FogRenderer.FogData fogData) {
        if (camera.getFluidInCamera() == FogType.NONE) {
            FogManager densityManager = FogManager.getDensityManager();

            float undergroundFogMultiplier = 1.0F;
            if (FogManager.shouldRenderCaveFog()) {
                undergroundFogMultiplier = (float)  Mth.lerp(densityManager.getUndergroundFactor(partialTick), densityManager.caveFogMultiplier, 1.0F);
                float darkness = densityManager.darkness.get(partialTick);
                undergroundFogMultiplier = Mth.lerp(darkness, undergroundFogMultiplier, 1.0F);
            }

            RenderSystem.setShaderFogStart(viewDistance * densityManager.fogStart.get(partialTick));
            RenderSystem.setShaderFogEnd(viewDistance * densityManager.fogEnd.get(partialTick) * undergroundFogMultiplier);
            RenderSystem.setShaderFogShape(FogShape.SPHERE);
        }
    }
}
