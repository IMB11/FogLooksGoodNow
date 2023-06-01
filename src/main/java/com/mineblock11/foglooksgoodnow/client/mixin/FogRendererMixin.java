package com.mineblock11.foglooksgoodnow.client.mixin;

import com.mineblock11.foglooksgoodnow.client.FogManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class FogRendererMixin {
    @Shadow private static float red;

    @Shadow private static float green;

    @Shadow private static float blue;

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false))
    private static void modifyFogColors(Args args, Camera camera, float partialTicks, ClientWorld level, int renderDistanceChunks, float bossColorModifier) {
        if (FogManager.shouldRenderCaveFog()) {
            FogManager densityManager = FogManager.getDensityManager();
            Vec3d fogColor = FogManager.getCaveFogColor();

            float undergroundFactor = 1 - densityManager.getUndergroundFactor(partialTicks);
            red = (float) MathHelper.lerp(undergroundFactor, red, fogColor.x * red);
            green = (float) MathHelper.lerp(undergroundFactor, green, fogColor.y * green);
            blue = (float) MathHelper.lerp(undergroundFactor, blue, fogColor.z * blue);
        }
    }
    @Inject(method = "applyFog", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fogRenderEvent(Camera camera, BackgroundRenderer.FogType fogMode, float viewDistance, boolean thickFog, float partialTick, CallbackInfo ci, CameraSubmersionType fogType, Entity entity, BackgroundRenderer.FogData fogData) {
        if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
            FogManager densityManager = FogManager.getDensityManager();

            float undergroundFogMultiplier = 1.0F;
            if (FogManager.shouldRenderCaveFog()) {
                undergroundFogMultiplier = (float)  MathHelper.lerp(densityManager.getUndergroundFactor(partialTick), densityManager.caveFogMultiplier, 1.0F);
                float darkness = densityManager.darkness.get(partialTick);
                undergroundFogMultiplier = MathHelper.lerp(darkness, undergroundFogMultiplier, 1.0F);
            }

            RenderSystem.setShaderFogStart(viewDistance * densityManager.fogStart.get(partialTick));
            RenderSystem.setShaderFogEnd(viewDistance * densityManager.fogEnd.get(partialTick) * undergroundFogMultiplier);
            RenderSystem.setShaderFogShape(FogShape.SPHERE);
        }
    }
}
