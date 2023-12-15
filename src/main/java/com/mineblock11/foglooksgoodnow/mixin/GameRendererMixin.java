package com.mineblock11.foglooksgoodnow.mixin;

import com.mineblock11.foglooksgoodnow.FogManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo info) {
        FogManager.getInstanceOptional().ifPresent(FogManager::tick);
    }
}