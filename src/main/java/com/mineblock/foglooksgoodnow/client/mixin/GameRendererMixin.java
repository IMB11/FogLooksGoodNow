package com.mineblock.foglooksgoodnow.client.mixin;

import com.mineblock.foglooksgoodnow.client.FogManager;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo info) {
        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.tick()));
    }
}