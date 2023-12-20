package com.mineblock11.foglooksgoodnow.mixin;

import com.mineblock11.foglooksgoodnow.FogLooksGoodNow;
import com.mineblock11.foglooksgoodnow.FogManager;
import com.mineblock11.foglooksgoodnow.render.CaveFogRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo info) {
        FogManager.instance();
        FogLooksGoodNow.LOGGER.info("Initialized Fog Manager");
    }

    @Inject(method = "close()V", at = @At("TAIL"))
    private void close(CallbackInfo info) {
        FogManager.instance().close();
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("TAIL"))
    public void renderSky(MatrixStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        CaveFogRenderer.renderCaveFog(mc.world, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
    }
}