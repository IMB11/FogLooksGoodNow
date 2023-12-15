package com.mineblock11.foglooksgoodnow.compat;

import com.mineblock11.foglooksgoodnow.config.FLGConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuEntrypoint implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FLGConfig.getInstance().generateScreen(parent);
    }
}
