package com.mineblock.foglooksgoodnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FLG implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FogLooksGood");
    public static com.mineblock.foglooksgoodnow.client.FLGConfig CONFIG = com.mineblock.foglooksgoodnow.client.FLGConfig.createAndLoad();
    @Override
    public void onInitializeClient() {

        CONFIG.subscribeToBiomeFogs(pairs -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToCaveFogColor(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToCaveFogDensity(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToUseCaveFog(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToDefaultFogDensity(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));
        CONFIG.subscribeToDefaultFogStart(col -> FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig())));

        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig()));
    }
}
