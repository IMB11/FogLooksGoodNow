package com.mineblock11.foglooksgoodnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FLG implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FogLooksGood");
    public static com.mineblock11.foglooksgoodnow.client.FLGConfig CONFIG = com.mineblock11.foglooksgoodnow.client.FLGConfig.createAndLoad();
    @Override
    public void onInitializeClient() {

        CONFIG.subscribeToDefaultRainFogStart(col -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));
        CONFIG.subscribeToBiomeFogs(pairs -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));
        CONFIG.subscribeToCaveFogColor(col -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));
        CONFIG.subscribeToCaveFogDensity(col -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));
        CONFIG.subscribeToUseCaveFog(col -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));
        CONFIG.subscribeToDefaultFogDensity(col -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));
        CONFIG.subscribeToDefaultFogStart(col -> FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig));

        FogManager.getDensityManagerOptional().ifPresent(FogManager::initializeConfig);
    }
}
