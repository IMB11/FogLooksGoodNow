package com.mineblock11.foglooksgoodnow;

import com.mineblock11.foglooksgoodnow.config.FLGConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.session.telemetry.WorldLoadedEvent;
import net.minecraft.world.WorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FogLooksGoodNow implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FogLooksGoodNow");
    @Override
    public void onInitializeClient() {
        FLGConfig.load();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FogManager.instance().setToConfig());
    }
}
