package com.mineblock11.foglooksgoodnow.mixin;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.tab.TabManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = YACLScreen.class, remap = false)
public class YACLMixin {
    /*? if >1.20.3 {*/
    @Shadow @Final public TabManager tabManager;

    @Shadow private boolean pendingChanges;

    @Inject(method = "finishOrSave", at = @At(value = "INVOKE", target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), cancellable = false)
    public void YACL_FIX(CallbackInfo ci) {
        pendingChanges = false;
        if (tabManager.getCurrentTab() instanceof YACLScreen.CategoryTab categoryTab) {
            categoryTab.updateButtons();
        }
    }
    /*?}*/
}
