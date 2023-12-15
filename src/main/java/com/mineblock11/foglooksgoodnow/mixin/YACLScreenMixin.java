package com.mineblock11.foglooksgoodnow.mixin;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = YACLScreen.class)
public class YACLScreenMixin extends Screen {
    @Shadow @Final public YetAnotherConfigLib config;

    protected YACLScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(DrawContext graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        assert this.client != null;
        if(this.client.world != null && this.title.contains(Text.of("Fog Looks Good Now"))) {
            ci.cancel();
            super.render(graphics, mouseX, mouseY, delta);
        }
    }
}
