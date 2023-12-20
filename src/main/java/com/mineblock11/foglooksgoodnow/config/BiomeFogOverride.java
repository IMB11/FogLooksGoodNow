package com.mineblock11.foglooksgoodnow.config;

import com.mineblock11.foglooksgoodnow.config.adapters.ColorAdapter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.awt.*;

public record BiomeFogOverride(Identifier biome, float fogStart, float fogStartRain, float fogDensity, float fogDensityRain, Color caveFogColor, float caveFogDensity) {
    public static Codec<BiomeFogOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("biome").forGetter(BiomeFogOverride::biome),
            Codec.FLOAT.fieldOf("fogStart").forGetter(BiomeFogOverride::fogStart),
            Codec.FLOAT.fieldOf("fogStartRain").forGetter(BiomeFogOverride::fogStartRain),
            Codec.FLOAT.fieldOf("fogDensity").forGetter(BiomeFogOverride::fogDensity),
            Codec.FLOAT.fieldOf("fogDensityRain").forGetter(BiomeFogOverride::fogDensityRain),
            Codec.STRING.fieldOf("caveFogColor").forGetter(BiomeFogOverride::getCaveFogColorAsHex),
            Codec.FLOAT.fieldOf("caveFogDensity").forGetter(BiomeFogOverride::caveFogDensity)
    ).apply(instance, (biome, fogStart, fogStartRain, fogDensity, fogDensityRain, caveFogColor, caveFogMultiplier) -> new BiomeFogOverride(biome, fogStart, fogStartRain, fogDensity, fogDensityRain, ColorAdapter.fromHex(caveFogColor), caveFogMultiplier)));

    public String getCaveFogColorAsHex() {
        return String.format("#%02x%02x%02x%02x", caveFogColor.getRed(), caveFogColor.getGreen(), caveFogColor.getBlue(), caveFogColor.getAlpha());
    }
};