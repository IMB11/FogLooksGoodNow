package com.mineblock11.foglooksgoodnow;

import com.mineblock11.foglooksgoodnow.config.FLGConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FogLooksGoodNow implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FogLooksGoodNow");
    @Override
    public void onInitializeClient() {
        FLGConfig.load();
        FogManager.instance();
    }
}
