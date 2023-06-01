package com.mineblock11.foglooksgoodnow.client;

import com.mojang.datafixers.util.Pair;
import io.wispforest.owo.config.annotation.*;

import java.util.List;

@Config(name = "fog-looks-good-now", wrapperName = "FLGConfig")
@Modmenu(modId = "fog-looks-good-now")

public class FLGConfigWrapper {
    @ExcludeFromScreen
    @Hook
    public List<Pair<String, FogManager.BiomeFogDensity>> biomeFogs = List.of(new Pair<>("minecraft:the_end", new FogManager.BiomeFogDensity(0.5f, 0.8f, 3355443)));

    @RangeConstraint(min = 0, max = 1)
    @Hook
    public double defaultFogStart = 0.0D;
    @RangeConstraint(min = 0, max = 1)
    @Hook
    public double defaultFogDensity = 1.0D;
    @Hook
    public boolean useCaveFog = true;
    @RangeConstraint(min = 0, max = 1)
    @Hook
    public double caveFogDensity = 0.8D;
    @Hook
    public int caveFogColor = 3355443;
}