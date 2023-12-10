package com.mineblock11.foglooksgoodnow.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;

public class IrisShadersCompat {
    public static boolean isUsingShaders() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            return  IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }
}
