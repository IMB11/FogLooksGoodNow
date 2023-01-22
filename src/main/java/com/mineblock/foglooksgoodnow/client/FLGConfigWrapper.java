package com.mineblock.foglooksgoodnow.client;

import com.mojang.datafixers.util.Pair;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;

import java.util.ArrayList;
import java.util.List;

@Config(name = "fog-looks-good-now", wrapperName = "FLGConfig")
@Modmenu(modId = "fog-looks-good-now")

public class FLGConfigWrapper {
    @ExcludeFromScreen
    @Hook
    public List<Pair<String, FogManager.BiomeFogDensity>> biomeFogs = new ArrayList<>();
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