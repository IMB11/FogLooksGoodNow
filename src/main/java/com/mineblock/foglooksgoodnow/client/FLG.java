package com.mineblock.foglooksgoodnow.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FLG implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FogLooksGood");
    public static com.mineblock.foglooksgoodnow.client.FLGConfig CONFIG = com.mineblock.foglooksgoodnow.client.FLGConfig.createAndLoad();
    @Override
    public void onInitializeClient() {
        FogEvents.RENDER_FOG.register((fogMode, fogType, camera, v, v1, v2, v3, fogShape, fogData) -> {
            if (camera.getFluidInCamera() == FogType.NONE) {
                FogManager densityManager = FogManager.getDensityManager();

                float undergroundFogMultiplier = 1.0F;
                if (FogManager.shouldRenderCaveFog()) {
                    undergroundFogMultiplier = (float)  Mth.lerp(densityManager.getUndergroundFactor(v), densityManager.caveFogMultiplier, 1.0F);
                    float darkness = densityManager.darkness.get(v);
                    undergroundFogMultiplier = Mth.lerp(darkness, undergroundFogMultiplier, 1.0F);
                }

                RenderSystem.setShaderFogStart(v1 * densityManager.fogStart.get(v));
                RenderSystem.setShaderFogEnd(v1 * densityManager.fogEnd.get(v) * undergroundFogMultiplier);
                RenderSystem.setShaderFogShape(FogShape.SPHERE);
            }
            return false;
        });
        FogEvents.SET_COLOR.register((colorData, v) -> {
            if (FogManager.shouldRenderCaveFog()) {
                FogManager densityManager = FogManager.getDensityManager();
                Vec3 fogColor = FogManager.getCaveFogColor();

                float undergroundFactor = 1 - densityManager.getUndergroundFactor(v);
                colorData.setRed((float) Mth.lerp(undergroundFactor, colorData.getRed(), fogColor.x * colorData.getRed()));
                colorData.setGreen((float) Mth.lerp(undergroundFactor, colorData.getGreen(), fogColor.y * colorData.getGreen()));
                colorData.setBlue((float) Mth.lerp(undergroundFactor, colorData.getBlue(), fogColor.z * colorData.getBlue()));
            }
        });

        CONFIG.subscribeToBiomeFogs(pairs -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToCaveFogColor(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToCaveFogDensity(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToUseCaveFog(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToDefaultFogDensity(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToDefaultFogStart(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));

        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig()));
    }
}
