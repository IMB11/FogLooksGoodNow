package com.mineblock11.foglooksgoodnow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mineblock11.foglooksgoodnow.FogManager;
import com.mineblock11.foglooksgoodnow.config.FLGConfig;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class FogRendererMixin {
    @Shadow private static float red;

    @Shadow private static float green;

    @Shadow private static float blue;

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false))
    private static void modifyFogColors(Args args, Camera camera, float partialTicks, ClientWorld level, int renderDistanceChunks, float bossColorModifier) {
        if(FLGConfig.get().disableAll) return;
        if (FogManager.shouldRenderCaveFog()) {
            FogManager fogManager = FogManager.instance();
            Vec3d fogColor = FogManager.getCaveFogColor();

            float undergroundFactor = 1 - fogManager.getUndergroundFactor(partialTicks);
            red = (float) MathHelper.lerp(undergroundFactor, red, fogColor.x * red);
            green = (float) MathHelper.lerp(undergroundFactor, green, fogColor.y * green);
            blue = (float) MathHelper.lerp(undergroundFactor, blue, fogColor.z * blue);
        }
    }
    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false, shift = At.Shift.BEFORE))
    private static void fogRenderEvent(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci, @Local BackgroundRenderer.FogData fogData) {
        if(FLGConfig.get().disableAll) return;
        if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
            FogManager fogManager = FogManager.instance();

            float undergroundFogMultiplier = 1.0F;
            if (FogManager.shouldRenderCaveFog()) {
                undergroundFogMultiplier = (float)  MathHelper.lerp(fogManager.getUndergroundFactor(tickDelta), fogManager.caveFogMultiplier, 1.0F);
                float darkness = fogManager.darkness.get(tickDelta);
                undergroundFogMultiplier = MathHelper.lerp(darkness, undergroundFogMultiplier, 1.0F);
            }

            fogData.fogStart = viewDistance * fogManager.fogStart.get(tickDelta);
            fogData.fogEnd = viewDistance * (fogManager.fogEnd.get(tickDelta) * fogManager.fogStartRain.get(tickDelta)) * undergroundFogMultiplier;
            fogData.fogShape = FogShape.SPHERE;
        }
    }
}
